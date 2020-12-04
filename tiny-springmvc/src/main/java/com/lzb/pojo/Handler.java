package com.lzb.pojo;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author : LZB
 * @Date : 2020/11/30
 * @Description :用来映射controller上的注解对应的方法
 */
public class Handler {
    /**
     * 方法对应的controller对象
     * method.invoke(controller,args);
     */
    private Object controller;

    /**
     * 注解对应的method
     */
    private Method method;

    /**
     * 请求路径
     */
    private String url;

    /**
     * method 对应的参数顺序
     */
    private Map<String,Integer> paramIndexMapping = new HashMap<>();


    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Integer> getParamIndexMapping() {
        return paramIndexMapping;
    }

    public void setParamIndexMapping(Map<String, Integer> paramIndexMapping) {
        this.paramIndexMapping = paramIndexMapping;
    }
}
