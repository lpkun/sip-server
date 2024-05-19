package top.skyyemu.sip.listeners.request;


import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.skyyemu.sip.annotate.SipRequest;
import top.skyyemu.sip.config.SipProperties;
import top.skyyemu.sip.enums.SipMethodEnum;
import top.skyyemu.sip.rtp.RtpSession;
import top.skyyemu.sip.utils.SipUtil;

import javax.sdp.Connection;
import javax.sdp.MediaDescription;
import javax.sdp.SessionDescription;
import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Description 呼叫（邀请）
 * @Author LPkun
 * @Date 2024/4/9 14:37
 */
@Component
@Slf4j
@SipRequest(method = SipMethodEnum.INVITE)
public class InviteRequestProcessor implements SipRequestProcessor {

    @Autowired
    private HeaderFactory headerFactory;
    @Autowired
    private MessageFactory messageFactory;
    @Autowired
    private AddressFactory addressFactory;
    @Autowired
    private SipProperties sipProperties;

    @Override
    public void process(RequestEvent requestEvent) throws Exception {
//        //获取sip协议提供者和工厂
//        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
//
//        Request request = requestEvent.getRequest();
//        String callId = ((CallIdHeader) request.getHeader(CallIdHeader.NAME)).getCallId();
//        SipInviteProcessor sipInviteProcessor = InviteProcessorMap.getInviteProcessor(callId);
//        if (ObjectUtil.isNotNull(sipInviteProcessor)) {
//            sipInviteProcessor.setMessage(request);
//            return;
//        }
//        Response response = messageFactory.createResponse(Response.TRYING, request);
//        DateHeader dateHeader = headerFactory.createDateHeader(Calendar.getInstance());
//        response.addHeader(dateHeader);
//        ServerTransaction serverTransaction = requestEvent.getServerTransaction() == null
//                ? sipProvider.getNewServerTransaction(request) : requestEvent.getServerTransaction();
//        serverTransaction.sendResponse(response);
//
//        //解析媒体流
//        SessionDescription sessionDescription = new SDPAnnounceParser(new String((byte[]) request.getContent(), StandardCharsets.UTF_8)).parse();
//        Vector mediaDescriptions = sessionDescription.getMediaDescriptions(false);
//        MediaDescription mediaDescription1 = (MediaDescription) mediaDescriptions.get(0);
//        Connection connection = mediaDescription1.getConnection() == null ? sessionDescription.getConnection() : mediaDescription1.getConnection();
//
//        SipInviteProcessor sipServerProcessor = new SipServerProcessor(
//                callId,
//                serverTransaction,
//                request,
//                new RtpSession(InetAddress.getByName(sipProperties.getMonitorIp()), InetAddress.getByName(connection.getAddress()), mediaDescription1.getMedia().getMediaPort())
//        );
//        System.out.println("初始邀请：" + JSONUtil.toJsonPrettyStr(sipServerProcessor.getRtpSession()));
//        InviteProcessorMap.addInviteProcessor(sipServerProcessor.getCallId(), sipServerProcessor);
//
//        //邀请通话
//        FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
//        ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);
//        SipInviteProcessor clientProcessor = invite(fromHeader.getAddress(), toHeader.getAddress(), sessionDescription);
//        clientProcessor.setSipInviteProcessor(sipServerProcessor);
//
//        sipServerProcessor.setSipInviteProcessor(clientProcessor);
    }

//    /**
//     * 邀请客户端通电话
//     */
//    public SipInviteProcessor invite(Address fromAddress, Address toAddress, SessionDescription sessionDescription) throws Exception {
//        //发起人
//        String fromUser = ((SipURI) fromAddress.getURI()).getUser();
//        SipURI sipURI = addressFactory.createSipURI(fromUser, sipProperties.getMonitorIp());
//        sipURI.setPort(sipProperties.getPort());
//        fromAddress = addressFactory.createAddress(sipURI);
//
//        //被邀请人
//        SipURI toUri = (SipURI) toAddress.getURI();
//        String toUser = toUri.getUser();
//        UserLoginInfo userLoginInfo = userMap.getUserLoginInfo(toUser);
//        toAddress = addressFactory.createAddress(userLoginInfo.getContactUri());
//        String fromTag = UUID.randomUUID().toString().substring(0, 8);
//        FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, fromTag);
//        ToHeader toHeader = headerFactory.createToHeader(toAddress, null);
//
//        // 创建 INVITE 请求
//        Request inviteRequest = messageFactory.createRequest(
//                toAddress.getURI(),
//                Request.INVITE,
//                headerFactory.createCallIdHeader(IdUtil.simpleUUID()),
//                headerFactory.createCSeqHeader(1L, Request.INVITE),
//                fromHeader,
//                toHeader,
//                Collections.singletonList(headerFactory.createViaHeader(sipProperties.getMonitorIp(), sipProperties.getPort(), "UDP", IdUtil.simpleUUID())),
//                headerFactory.createMaxForwardsHeader(70));
//        inviteRequest.addHeader(headerFactory.createContactHeader(fromAddress));
//
//        RtpSession rtpSession = new RtpSession(InetAddress.getByName(sipProperties.getMonitorIp()), null, null);
//
//        //拿第一个通话请求的过来改着用
//        SessionDescription sdp = SipUtil.changeIpAndPort(sessionDescription.toString(), rtpSession.getServerAddress().getHostAddress(), rtpSession.getServerPort());
//        inviteRequest.setContent(sdp.toString(), headerFactory.createContentTypeHeader("application", "sdp"));
//
//        // ... 发送请求 ...
//        // 创建客户端事务并发送请求
//        SipProvider sipProvider = SpringUtil.getBean("udpSipProvider", SipProvider.class);
//        ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(inviteRequest);
//        clientTransaction.sendRequest();
//        System.out.println("服务器发起的通讯邀请：" + inviteRequest);
//
//
//        //后续请求多交给处理器处理
//        SipInviteProcessor clientProcessor = new SipClientProcessor(
//                ((CallIdHeader) inviteRequest.getHeader(CallIdHeader.NAME)).getCallId(),
//                clientTransaction,
//                inviteRequest,
//                rtpSession
//        );
//
//        System.out.println("发起邀请SipInviteProcessor邀请：" + JSONUtil.toJsonPrettyStr(clientProcessor.getRtpSession()));
//        InviteProcessorMap.addInviteProcessor(clientProcessor.getCallId(), clientProcessor);
//        return clientProcessor;
//    }


}
