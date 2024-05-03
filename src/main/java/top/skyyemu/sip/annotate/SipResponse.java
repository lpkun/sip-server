package top.skyyemu.sip.annotate;


import top.skyyemu.sip.enums.SipMethodEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * @Description sip请求业务类的注解
 * @Author LPkun
 * @Date 2024/4/9 14:39
 */
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SipResponse {
    SipMethodEnum method();
}
