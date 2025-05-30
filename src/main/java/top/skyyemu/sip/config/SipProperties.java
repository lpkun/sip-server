package top.skyyemu.sip.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description SIP服务端配置信息
 * @Author LPkun
 * @Date 2024/4/30 16:49
 */
@ConfigurationProperties(prefix = "sip")
@Data
public class SipProperties {
    /**
     * sip服务的Ip，默认使用 0.0.0.0
     */
    private String monitorIp = "0.0.0.0";
    /**
     * 外网地址
     */
    private String outerIp;
    /**
     * sip服务的端口，默认使用5060
     */
    private Integer port;
    /**
     * 域名
     */
    private String domain;
    /**
     * sip账号生存时间，单位：分钟。最后一次心跳超过此时间则下线
     */
    private Long survivalTime;
    /**
     * rtp端口范围
     */
    private String rtpPortRange;
}
