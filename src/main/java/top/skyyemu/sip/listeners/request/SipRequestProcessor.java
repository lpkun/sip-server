package top.skyyemu.sip.listeners.request;

import javax.sip.RequestEvent;

/**
 * @Description 请求处理器接口
 * @Author LPkun
 * @Date 2024/5/19 17:50
 */
public interface SipRequestProcessor {
    /**
     * 处理方法
     *
     * @param event 请求事件
     */
    void process(RequestEvent event) throws Exception;
}
