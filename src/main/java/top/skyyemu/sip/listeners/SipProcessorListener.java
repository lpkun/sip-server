package top.skyyemu.sip.listeners;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.skyyemu.sip.annotate.SipRequest;
import top.skyyemu.sip.annotate.SipResponse;
import top.skyyemu.sip.enums.SipMethodEnum;
import top.skyyemu.sip.listeners.request.SipRequestProcessor;
import top.skyyemu.sip.listeners.response.SipResponseProcessor;

import javax.sip.*;
import javax.sip.header.CSeqHeader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description Sip处理器侦听器
 * @Author LPkun
 * @Date 2024/4/30 16:58
 */
@Slf4j
@Component
public class SipProcessorListener implements SipListener {

    /**
     * 请求sip处理器集合
     */
    private static final Map<SipMethodEnum, SipRequestProcessor> REQUEST_PROCESSOR_MAP = new ConcurrentHashMap<>();

    /**
     * 响应sip处理器集合
     */
    private static final Map<SipMethodEnum, SipResponseProcessor> RESPONSE_PROCESSOR_MAP = new ConcurrentHashMap<>();


    /**
     * 构造函数
     *
     * @param sipRequestProcessors 必须注入实现 SipRequestProcessor 的之类，否则创建这个类没有什么用
     */
    public SipProcessorListener(List<SipRequestProcessor> sipRequestProcessors, List<SipResponseProcessor> sipResponseProcessors) {
        for (SipRequestProcessor sipRequestProcessor : sipRequestProcessors) {
            SipRequest annotation = sipRequestProcessor.getClass().getAnnotation(SipRequest.class);
            if (ObjectUtil.isNull(annotation)) {
                throw new RuntimeException(sipRequestProcessor.getClass() + " 必须增加 @SipRequest 注解。");
            }
            SipMethodEnum method = annotation.method();
            if (REQUEST_PROCESSOR_MAP.containsKey(method)) {
                throw new RuntimeException("检测到" + annotation.method() + "重复，必须保持单例。");
            }
            REQUEST_PROCESSOR_MAP.put(method, sipRequestProcessor);
        }

        for (SipResponseProcessor sipResponseProcessor : sipResponseProcessors) {
            SipResponse annotation = sipResponseProcessor.getClass().getAnnotation(SipResponse.class);
            if (ObjectUtil.isNull(annotation)) {
                throw new RuntimeException(sipResponseProcessor.getClass() + " 必须增加 @SipRequest 注解。");
            }
            SipMethodEnum method = annotation.method();
            if (RESPONSE_PROCESSOR_MAP.containsKey(method)) {
                throw new RuntimeException("检测到" + annotation.method() + "重复，必须保持单例。");
            }
            RESPONSE_PROCESSOR_MAP.put(method, sipResponseProcessor);
        }
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {
        String method = requestEvent.getRequest().getMethod();
        SipRequestProcessor sipRequestProcessor = REQUEST_PROCESSOR_MAP.get(SipMethodEnum.getMethodEnumByName(method));
        if (Objects.isNull(sipRequestProcessor)) {
            log.error("请求【方法：】{}，不支持", method);
            return;
        }
        try {
            sipRequestProcessor.process(requestEvent);
        } catch (Exception e) {
            log.error("请求【方法：{}】，异常", method, e);
        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        CSeqHeader cSeqHeader = (CSeqHeader) responseEvent.getResponse().getHeader(CSeqHeader.NAME);
        String method = cSeqHeader.getMethod();
        SipResponseProcessor sipResponseProcessor = RESPONSE_PROCESSOR_MAP.get(SipMethodEnum.getMethodEnumByName(method));
        if (Objects.isNull(sipResponseProcessor)) {
            log.error("响应【方法：】{}，不支持,{}", method, responseEvent.getResponse());
            return;
        }
        try {
            sipResponseProcessor.process(responseEvent);
        } catch (Exception e) {
            log.error("响应【方法：{}】，异常", method, e);
        }
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        log.info("事务超时：{}", timeoutEvent);
    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {
        log.info("进程IO异常：{}", exceptionEvent);
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        log.info("进程事务已终止:{}", transactionTerminatedEvent);
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        log.info("进程对话框已终止:{}", dialogTerminatedEvent);
    }
}
