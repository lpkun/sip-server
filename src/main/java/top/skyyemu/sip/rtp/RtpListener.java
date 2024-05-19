package top.skyyemu.sip.rtp;

/**
 * @Description rtp会话的监听者（做广播的时候用到）
 * @Author LPkun
 * @Date 2024/5/19 18:44
 */
public interface RtpListener {

    /**
     * 已包含请求头 12 个字节
     *
     * @param packageData 多媒体数据包
     */
    void receivedRtpPacket(byte[] packageData);

}
