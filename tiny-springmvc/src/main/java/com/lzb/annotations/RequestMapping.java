package com.lzb.annotations;

import java.lang.annotation.*;

/**
 * @Author : LZB
 * @Date : 2020/11/30
 * @Description :
 */
@Documented
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    String value() default "";
}
