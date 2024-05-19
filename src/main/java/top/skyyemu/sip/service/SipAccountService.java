package top.skyyemu.sip.service;

import top.skyyemu.sip.entity.SipUser;

/**
 * @Description sip账号业务类
 * @Author LPkun
 * @Date 2024/5/19 18:09
 */
public interface SipAccountService {

    /**
     * 更新状态
     *
     * @param account 账号
     * @param status  状态：（0=离线，1在线）
     */
    void updateStatus(String account, Integer status);

    /**
     * 根据账号获取sip账号信息
     *
     * @param account 账号
     * @return SipUser
     */
    SipUser getByAccount(String account);

}
