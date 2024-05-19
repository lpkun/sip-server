package top.skyyemu.sip.dto;

import lombok.Data;

import javax.sip.address.URI;
import java.util.Date;

/**
 * @Description sip账号登录信息
 * @Author LPkun
 * @Date 2024/5/19 17:59
 */
@Data
public class SipAccountLoginDto {
    /**
     * callId 表示一个登陆端
     */
    private String callId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 注册时的from
     */
    private URI uri;
    /**
     * 域名
     */
    private String realm;
    /**
     * 登陆时间
     */
    private Date loginTime;
    /**
     * 联系地址
     */
    private URI contactUri;
}
