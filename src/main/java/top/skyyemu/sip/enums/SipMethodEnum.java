package top.skyyemu.sip.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description sip协议请求方法枚举类
 * @Author LPkun
 * @Date 2024/4/30 16:56
 */
@Getter
@AllArgsConstructor
public enum SipMethodEnum {
    /**
     * 注册联系消息
     */
    REGISTER("REGISTER", "注册联系消息"),
    /**
     * 发起会话请求
     */
    INVITE("INVITE", "发起会话请求"),
    /**
     * INVITE请求的响应的确认
     */
    ACK("ACK", "INVITE请求的响应的确认"),
    /**
     * 取消请求
     */
    CANCEL("CANCEL", "取消请求"),
    /**
     * 终结回话
     */
    BYE("BYE", "终结回话"),
    /**
     * 查询服务器能力
     */
    OPTIONS("OPTIONS", "查询服务器能力"),
    /**
     * 消息
     */
    MESSAGE("MESSAGE", "RFC3428对Sip协议的扩展,增加了MESSAGE方法。采用Pager Model进行通信,传递不超过1300字节的数据。");
    /**
     * 方法名
     */
    private final String name;
    /**
     * 描述
     */
    private final String describe;

    /**
     * sip协议请求方法枚举类MAP
     */
    private static final Map<String, SipMethodEnum> SIP_METHOD_ENUM_MAP = new HashMap<String, SipMethodEnum>() {{
        SipMethodEnum[] values = SipMethodEnum.values();
        for (SipMethodEnum sipMethodEnum : values) {
            put(sipMethodEnum.name, sipMethodEnum);
        }
    }};

    /**
     * 根据名称获取枚举
     *
     * @param eqName 枚举名称
     * @return 枚举类型
     */
    public static SipMethodEnum getMethodEnumByName(String eqName) {
        return SIP_METHOD_ENUM_MAP.get(eqName);
    }
}
