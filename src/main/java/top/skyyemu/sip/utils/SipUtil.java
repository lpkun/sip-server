package top.skyyemu.sip.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import gov.nist.javax.sdp.fields.AttributeField;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;
import lombok.extern.slf4j.Slf4j;
import top.skyyemu.sip.config.SipProperties;
import top.skyyemu.sip.dto.SdpInfoDto;
import top.skyyemu.sip.enums.VoiceTypeEnum;

import javax.sdp.*;
import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.Message;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;

/**
 * @Description 请求响应工具
 * @Author LPkun
 * @Date 2024/4/15 14:03
 */
@Slf4j
public class SipUtil {

    private static MessageFactory MESSAGE_FACTORY;
    private static HeaderFactory HEADER_FACTORY;
    private static SipProperties SIP_PROPERTIES;
    private static AddressFactory ADDRESS_FACTORY;
    private static SipProvider SIP_PROVIDER;

    static {
        try {
            MESSAGE_FACTORY = SpringUtil.getBean(MessageFactory.class);
            HEADER_FACTORY = SpringUtil.getBean(HeaderFactory.class);
            SIP_PROPERTIES = SpringUtil.getBean(SipProperties.class);
            ADDRESS_FACTORY = SpringUtil.getBean(AddressFactory.class);
            SIP_PROVIDER = SpringUtil.getBean("udpSipProvider", SipProvider.class);
        } catch (Exception e) {
            log.error("初始化 {} 失败了：{}", SipUtil.class, e.getMessage());
        }
    }


    /**
     * 修改SDP的 ip 以及端口号
     *
     * @param sdp    sdp原文字符串
     * @param toIp   修改后的ip
     * @param toPort 修改后的端口哈
     * @return 修改后的SDP
     */
    public static SessionDescription changeIpAndPort(String sdp, String toIp, Integer toPort) throws ParseException, SdpException {
        SessionDescription sessionDescription = new SDPAnnounceParser(sdp).parse();
        Origin origin = sessionDescription.getOrigin();
        origin.setSessionId(System.currentTimeMillis());
        origin.setSessionVersion(System.currentTimeMillis());
        origin.setAddress(toIp);

        sessionDescription.getSessionName().setValue("bdsystem");

        Vector mediaDescriptions = sessionDescription.getMediaDescriptions(false);
        for (Object o : mediaDescriptions) {
            if (!(o instanceof MediaDescription) || ObjectUtil.isNull(o)) {
                continue;
            }
            MediaDescription mediaDescription = (MediaDescription) o;
            //改成服务端的ip地址（后续接收到RTP数据在中转给其他人）
            Connection connection = mediaDescription.getConnection() == null ? sessionDescription.getConnection() : mediaDescription.getConnection();
            connection.setAddress(toIp);
            //改成服务器的临时RTP端口（后续接收到RTP数据在中转给其他人）
            mediaDescription.getMedia().setMediaPort(toPort);
        }
        return sessionDescription;
    }

    /**
     * 生成SDP
     *
     * @param ip           未来将通过此IP通讯RTP
     * @param port         未来将通过此端口通讯RTP
     * @param voiceType    语音类型仅支持 VoiceTypeEnum.PCMA || VoiceTypeEnum.PCMU 类型
     * @param samplingRate 采样率（可空，默认一般多使用8000）
     */
    public static SessionDescription generateSdp(String ip, Integer port, VoiceTypeEnum voiceType, Integer samplingRate) throws Exception {
        if (voiceType != VoiceTypeEnum.PCMA && voiceType != VoiceTypeEnum.PCMU) {
            throw new RuntimeException("仅支持 " + VoiceTypeEnum.PCMA + VoiceTypeEnum.PCMU + "类型。");
        }
        samplingRate = ObjectUtil.isNull(samplingRate) ? 8000 : samplingRate;

        Random random = new Random();
        StringBuilder sb = new StringBuilder("v=0").append("\r\n");
        sb.append("s=bd3System").append("\r\n");

        sb.append("o=- ").append(random.nextInt(Integer.MAX_VALUE)).append(" ")
                .append(random.nextInt(Integer.MAX_VALUE)).append(" IN IP4 ").append(ip).append("\r\n");

        sb.append("t=0 0").append("\r\n");
        //告诉呼叫端使用audio语音、这个端口port、RTP、这个格式PCMA或PCMU，发数据给过来
        sb.append("m=audio ").append(port).append(" RTP/AVP ").append(voiceType == VoiceTypeEnum.PCMA ? 8 : 0).append("\r\n");
        //通过这个IP发过来
        sb.append("c=IN IP4 ").append(ip).append("\r\n");

        if (voiceType == VoiceTypeEnum.PCMA) {
            sb.append("a=rtpmap:8 PCMA/").append(samplingRate).append("\r\n");
        } else {
            sb.append("a=rtpmap:0 PCMU/").append(samplingRate).append("\r\n");
        }

        //端点愿意接收数据和发送数据
        sb.append("a=sendrecv");
        return new SDPAnnounceParser(sb.toString()).parse();
    }


    /**
     * 解析SDP协议内容
     *
     * @param sdpByte sdp字节
     * @return 只返回关心的数据 SdpInfoDto
     */
    public static SdpInfoDto sdpParse(byte[] sdpByte) throws Exception {
        String sdpStr = new String(sdpByte, StandardCharsets.UTF_8);
        return sdpParse(sdpStr);
    }

    /**
     * 解析SDP协议内容
     *
     * @param sdpStr sdp报文
     * @return 只返回关心的数据 SdpInfoDto
     */
    public static SdpInfoDto sdpParse(String sdpStr) throws Exception {
        SessionDescription parsedDescription = new SDPAnnounceParser(sdpStr).parse();
        MediaDescription mediaDescription = (MediaDescription) parsedDescription.getMediaDescriptions(false).get(0);
        Connection connection = mediaDescription.getConnection() == null ? parsedDescription.getConnection() : mediaDescription.getConnection();
        SdpInfoDto sdpInfo = new SdpInfoDto();
        //解析SDP接收推流的RTP ip地址
        sdpInfo.setIp(connection.getAddress());
        //解析SDP接收推流的RTP端口
        sdpInfo.setPort(mediaDescription.getMedia().getMediaPort());

        //这里只获取第一个有效载荷类型
        String mediaFormat = (String) mediaDescription.getMedia().getMediaFormats(false).get(0);
        String rtpmap = mediaDescription.getAttribute(SdpConstants.RTPMAP);
        String[] s = rtpmap.split(" ");
        if (s[0].equals(mediaFormat)) {
            String[] split = s[1].split("/");
            //第一个就是了那就直接赋值然后返回
            sdpInfo.setVoiceType(VoiceTypeEnum.valueOf(split[0]));
            sdpInfo.setSamplingRate(Integer.parseInt(split[1]));
            return sdpInfo;
        }

        //第一个不是那就获取 rtpmap 其他的试试
        Vector attributes = mediaDescription.getAttributes(false);
        for (Object attribute : attributes) {
            AttributeField attributeField = (AttributeField) attribute;
            if (!SdpConstants.RTPMAP.equals(attributeField.getName())) {
                continue;
            }
            String value = attributeField.getValue();
            s = value.split(" ");
            if (s[0].equals(mediaFormat)) {
                String[] split = s[1].split("/");
                sdpInfo.setVoiceType(VoiceTypeEnum.valueOf(split[0]));
                sdpInfo.setSamplingRate(Integer.parseInt(split[1]));
                break;
            }
        }
        return sdpInfo;
    }

    /**
     * 获取请求头/响应头的 from 账户名
     *
     * @param message 请求头/响应头
     * @return 请求头/响应头 账号
     */
    public static String getUserByFromHeader(Message message) {
        FromHeader fromHeader = (FromHeader) message.getHeader(FromHeader.NAME);
        Address address = fromHeader.getAddress();
        SipURI sipURI = (SipURI) address.getURI();
        return sipURI.getUser();
    }

    //------------------------------------------------------------------------------------------------------------------
    // 响应 start↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
    //------------------------------------------------------------------------------------------------------------------

    /**
     * 发送 TRYING 响应
     *
     * @param request           请求事件
     * @param serverTransaction 请求的服务端事务
     */
    public static void responseTrying(Request request, ServerTransaction serverTransaction)
            throws ParseException, SipException, InvalidArgumentException {
        Response response = MESSAGE_FACTORY.createResponse(Response.TRYING, request);
        DateHeader dateHeader = HEADER_FACTORY.createDateHeader(Calendar.getInstance());
        response.addHeader(dateHeader);
        serverTransaction.sendResponse(response);
    }

    /**
     * 发送忙碌中，拒接接听
     *
     * @param request           请求
     * @param serverTransaction 请求的服务端事务
     */
    public static void responseBusyHere(Request request, ServerTransaction serverTransaction)
            throws ParseException, SipException, InvalidArgumentException {
        Response response = MESSAGE_FACTORY.createResponse(Response.BUSY_HERE, request);
        serverTransaction.sendResponse(response);
    }

    //响应 end ---------------------------------------------------------------------------------------------------------


    //------------------------------------------------------------------------------------------------------------------
    // 请求 start↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
    //------------------------------------------------------------------------------------------------------------------

    /**
     * 邀请客户端通电话
     *
     * @param requestURI         被邀请人URI
     * @param fromUsername       发起人的昵称
     * @param fromIp             发起人IP
     * @param fromPort           发起人端口
     * @param sessionDescription sdp
     * @return 呼叫ID
     * @throws Exception 异常
     */
    public static String requestInvite(URI requestURI, String fromUsername, String fromIp, Integer fromPort, SessionDescription sessionDescription) throws Exception {
        String fromTag = UUID.randomUUID().toString().substring(0, 8);
        Address fromAddress = ADDRESS_FACTORY.createAddress("sip:" + fromUsername + "@" + fromIp + ":" + fromPort);
        FromHeader fromHeader = HEADER_FACTORY.createFromHeader(fromAddress, fromTag);
        ToHeader toHeader = HEADER_FACTORY.createToHeader(ADDRESS_FACTORY.createAddress(requestURI), null);

        // 创建 INVITE 请求
        Request inviteRequest = MESSAGE_FACTORY.createRequest(
                requestURI,
                Request.INVITE,
                HEADER_FACTORY.createCallIdHeader(IdUtil.simpleUUID()),
                HEADER_FACTORY.createCSeqHeader(1L, Request.INVITE),
                fromHeader,
                toHeader,
                Collections.singletonList(HEADER_FACTORY.createViaHeader(SIP_PROPERTIES.getMonitorIp(), SIP_PROPERTIES.getPort(), "UDP", IdUtil.simpleUUID())),
                HEADER_FACTORY.createMaxForwardsHeader(70));
        inviteRequest.addHeader(HEADER_FACTORY.createContactHeader(fromAddress));

        inviteRequest.setContent(sessionDescription.toString(), HEADER_FACTORY.createContentTypeHeader("application", "sdp"));

        // 创建客户端事务并发送请求
        ClientTransaction clientTransaction = SIP_PROVIDER.getNewClientTransaction(inviteRequest);
        clientTransaction.sendRequest();

        return ((CallIdHeader) inviteRequest.getHeader(CallIdHeader.NAME)).getCallId();
    }

    //请求 end ---------------------------------------------------------------------------------------------------------

}
