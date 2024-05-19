package top.skyyemu.sip.listeners.response;

import javax.sip.ResponseEvent;

/**
 * @Description 响应处理器接口
 * @Author LPkun
 * @Date 2024/5/19 17:51
 */
public interface SipResponseProcessor {
    /**
     * 处理方法
     *
     * @param evt 响应事件
     */
    void process(ResponseEvent evt) throws Exception;

}
