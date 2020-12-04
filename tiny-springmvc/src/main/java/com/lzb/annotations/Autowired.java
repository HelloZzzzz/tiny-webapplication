package com.lzb.annotations;

import java.lang.annotation.*;

/**
 * @Author : LZB
 * @Date : 2020/11/30
 * @Description :默认注入方式
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
    String value() default "";
}
