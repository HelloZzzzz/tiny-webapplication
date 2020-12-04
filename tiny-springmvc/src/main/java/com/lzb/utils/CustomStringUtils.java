package com.lzb.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @Author : LZB
 * @Date : 2020/11/30
 * @Description :
 */
public final class CustomStringUtils {
    private CustomStringUtils() {

    }


    public static String toLowerCaseFirstOne(String str) {
        if (StringUtils.isBlank(str) && Character.isLowerCase(str.charAt(0))) {
            return str;
        } else {
            return Character.toLowerCase(str.charAt(0)) + str.substring(1);
        }
    }


}
