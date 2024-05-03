package top.skyyemu.sip.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sip.*;

/**
 * @Description Sip处理器侦听器
 * @Author LPkun
 * @Date 2024/4/30 16:58
 */
@Slf4j
@Component
public class SipProcessorListener implements SipListener {

    @Override
    public void processRequest(RequestEvent requestEvent) {

    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {

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
