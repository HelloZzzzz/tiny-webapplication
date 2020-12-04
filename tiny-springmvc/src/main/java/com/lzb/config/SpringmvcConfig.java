package com.lzb.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author : LZB
 * @Date : 2020/12/3
 * @Description : 映射配置文件
 */
public class SpringmvcConfig {

    private List<String> basePackage = new ArrayList<>();

    public List<String> getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(List<String> basePackage) {
        this.basePackage = basePackage;
    }
}
