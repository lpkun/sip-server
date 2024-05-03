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
     * sip服务的端口，默认使用5060
     */
    private Integer port;
    /**
     * 域名
     */
    private String domain;
}
