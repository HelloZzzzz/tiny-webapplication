package com.lzb.service.impl;

import com.lzb.service.DemoService;
import com.lzb.annotations.Service;


@Service
public class DemoServiceImpl implements DemoService {


    @Override
    public String get(String name) {
        return "service 实现类中的name参数：" + name;
    }
}
