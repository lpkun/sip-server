package top.skyyemu.sip.listeners.request;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import gov.nist.javax.sip.RequestEventExt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.skyyemu.sip.annotate.SipRequest;
import top.skyyemu.sip.config.SipProperties;
import top.skyyemu.sip.dto.SipAccountLoginDto;
import top.skyyemu.sip.entity.SipUser;
import top.skyyemu.sip.enums.SipMethodEnum;
import top.skyyemu.sip.service.SipAccountService;

import javax.sip.*;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

@Component
@Slf4j
@SipRequest(method = SipMethodEnum.REGISTER)
public class RegisterRequestProcessor implements SipRequestProcessor {

    @Autowired
    private HeaderFactory headerFactory;
    @Autowired
    private MessageFactory messageFactory;
    @Autowired
    private SipProperties sipProperties;
    @Autowired
    private SipAccountService sipAccountService;

    @Override
    @Async
    public void process(RequestEvent event) throws SipException, InvalidArgumentException, ParseException {
        //转换请求事件为扩展事件
        if (event instanceof RequestEventExt) {
            RequestEventExt evtExt = (RequestEventExt) event;
            //获取请求对象设备id
            String deviceId = getDeviceIdByRequest(evtExt.getRequest());
            //获取sip协议提供者和工厂
            SipProvider sipProvider = (SipProvider) event.getSource();

            if (StrUtil.isEmpty(deviceId)) {
                log.error("【error:】{}", "设备id为空");
            }
            if (isAuthorizationPass(event.getRequest())) {
                doSuccess(event, sipProvider);
            } else if (isRegisterWithoutAuth(evtExt.getRequest())) {
                doUnAuthorized401(event, sipProvider, deviceId);
            } else {
                doLoginFail403(event, sipProvider);
            }
        }
    }

    /**
     * 获取当前请求的设备id
     */
    private String getDeviceIdByRequest(Request request) {
        ToHeader toHead = (ToHeader) request.getHeader(ToHeader.NAME);
        SipURI toUri = (SipURI) toHead.getAddress().getURI();
        return toUri.getUser();
    }

    /**
     * 有Auth信息，一般在第二次Register的时候，这个时候会带着第一次服务端返回的Digest信息
     */
    private boolean isRegisterWithoutAuth(Request request) {
        int expires = request.getExpires().getExpires();
        AuthorizationHeader authorizationHeader = (AuthorizationHeader) request.getHeader(AuthorizationHeader.NAME);
        return expires > 0 && authorizationHeader == null;
    }

    /**
     * 组装登录成功200的Response
     */
    private void doSuccess(RequestEvent requestEvent, SipProvider sipProvider) throws ParseException, SipException, InvalidArgumentException {
        Request request = requestEvent.getRequest();
        Response response = messageFactory.createResponse(Response.OK, request);
        response.addHeader(headerFactory.createAllowHeader("ACK, BYE, CANCEL, INFO, INVITE, MESSAGE, NOTIFY, OPTIONS, PRACK, REFER, REGISTER, SUBSCRIBE"));
        response.addHeader(headerFactory.createExpiresHeader(120));
        FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
        response.addHeader(headerFactory.createContactHeader(fromHeader.getAddress()));

        ServerTransaction serverTransactionId = requestEvent.getServerTransaction() == null ? sipProvider.getNewServerTransaction(request) : requestEvent.getServerTransaction();
//        System.out.println("响应response = " + response);
        serverTransactionId.sendResponse(response);

    }

    /**
     * 响应未鉴权401
     */
    private void doUnAuthorized401(RequestEvent requestEvent, SipProvider sipProvider, String deviceId) throws ParseException, SipException, InvalidArgumentException {
        Response response;
        response = messageFactory.createResponse(Response.UNAUTHORIZED, requestEvent.getRequest());

        String callId = getCallIdFromRequest(requestEvent.getRequest());
        String nonce = DigestUtil.md5Hex(callId + deviceId);
        WWWAuthenticateHeader wwwAuthenticateHeader = headerFactory.createWWWAuthenticateHeader("Digest realm=\"" + sipProperties.getDomain() + "\",nonce=\"" + nonce + "\",algorithm=MD5");
        response.setHeader(wwwAuthenticateHeader);
        ServerTransaction serverTransactionId = requestEvent.getServerTransaction() == null ? sipProvider.getNewServerTransaction(requestEvent.getRequest()) : requestEvent.getServerTransaction();
        serverTransactionId.sendResponse(response);
    }


    /**
     * 组装登录失败403的Response
     */
    private void doLoginFail403(RequestEvent requestEvent, SipProvider sipProvider) throws ParseException, SipException, InvalidArgumentException {
        Request request = requestEvent.getRequest();
        Response response = messageFactory.createResponse(Response.FORBIDDEN, request);
        DateHeader dateHeader = headerFactory.createDateHeader(Calendar.getInstance());
        response.addHeader(dateHeader);
        ServerTransaction serverTransactionId = requestEvent.getServerTransaction() == null ? sipProvider.getNewServerTransaction(request) : requestEvent.getServerTransaction();
        serverTransactionId.sendResponse(response);
    }

    /**
     * 是否校验鉴权通过
     *
     * @return true 通过
     */
    private boolean isAuthorizationPass(Request request) {
        if (isRegisterWithoutAuth(request)) {
            return false;
        }
        AuthorizationHeader authorizationHeader = (AuthorizationHeader) request.getHeader(AuthorizationHeader.NAME);
        if (authorizationHeader == null) {
            return false;
        }
        String username = authorizationHeader.getUsername();
        String realm = authorizationHeader.getRealm();
        String nonce = authorizationHeader.getNonce();
        URI uri = authorizationHeader.getURI();
        String res = authorizationHeader.getResponse();
        String algorithm = authorizationHeader.getAlgorithm();
        SipUser sipUser = sipAccountService.getByAccount(username);
        if (ObjectUtil.isNull(username) ||
                ObjectUtil.isNull(sipUser) ||
                ObjectUtil.isNull(realm) ||
                ObjectUtil.isNull(nonce) ||
                ObjectUtil.isNull(uri) ||
                ObjectUtil.isNull(res)
        ) {
            log.error("Authorization信息不全，无法认证。");
        } else {
            // 比较Authorization信息正确性
            String A1 = DigestUtil.md5Hex(username + ":" + realm + ":" + sipUser.getPassword());
            String A2 = DigestUtil.md5Hex("REGISTER" + ":" + uri);
            String resStr = DigestUtil.md5Hex(A1 + ":" + nonce + ":" + A2);
            if (resStr.equals(res)) {
                //记录登录信息
                FromHeader head = (FromHeader) request.getHeader(FromHeader.NAME);
                CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
                ContactHeader contactHeader = (ContactHeader) request.getHeader(ContactHeader.NAME);

                SipAccountLoginDto userInfo = new SipAccountLoginDto();
                userInfo.setCallId(callIdHeader.getCallId());
                userInfo.setUsername(username);
                userInfo.setUri(head.getAddress().getURI());
                userInfo.setRealm(realm);
                userInfo.setLoginTime(new Date());
                userInfo.setContactUri(contactHeader.getAddress().getURI());
                sipAccountService.updateStatus(sipUser.getAccount(), 1);
                return true;
            }
        }
        return false;

    }

    /**
     * 由本地设备(Client)生成，全局唯一，每次呼叫这个值唯一不变
     */
    private String getCallIdFromRequest(Request request) {
        CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
        return callIdHeader.getCallId();
    }
}
