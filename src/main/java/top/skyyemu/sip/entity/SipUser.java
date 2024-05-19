package top.skyyemu.sip.entity;

import lombok.Data;

/**
 * @Description sip账号实体类
 * @Author LPkun
 * @Date 2024/5/19 18:15
 */
@Data
public class SipUser {
    /**
     * 账号
     */
    private String account;
    /**
     * 密码
     */
    private String password;
    /**
     * 状态：（0=离线，1在线）
     */
    private Integer status;
}
