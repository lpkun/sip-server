package top.skyyemu.sip.service;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Service;
import top.skyyemu.sip.entity.SipUser;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description sip账号业务类
 * @Author LPkun
 * @Date 2024/5/19 18:13
 */
@Service
public class SipAccountServiceImpl implements SipAccountService {
    /**
     * sip账号
     * key = account（账号）
     */
    private static final Map<String, SipUser> SIP_USER_MAP = new HashMap<>();

    /**
     * 初始化账号信息
     */
    @PostConstruct
    public void init() {
        String accountStr = ResourceUtil.readStr("sipAccount.json", Charset.forName("UTF-8"));
        List<SipUser> sipUsers = JSONUtil.toList(accountStr, SipUser.class);
        sipUsers.forEach(su -> SIP_USER_MAP.put(su.getAccount(), su));
    }

    @Override
    public void updateStatus(String account, Integer status) {
        SipUser sipUser = SIP_USER_MAP.get(account);
        if (sipUser != null) {
            sipUser.setStatus(status);
        }
    }

    @Override
    public SipUser getByAccount(String account) {
        return SIP_USER_MAP.get(account);
    }
}
