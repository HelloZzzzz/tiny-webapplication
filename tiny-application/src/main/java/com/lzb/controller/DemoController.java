package com.lzb.controller;

import com.lzb.service.DemoService;
import com.lzb.annotations.Autowired;
import com.lzb.annotations.Controller;
import com.lzb.annotations.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/demo")
public class DemoController {


    @Autowired
    private DemoService demoService;


    /**
     * URL: /demo/query?name=lisi
     * @param request
     * @param response
     * @param name
     * @return
     */
    @RequestMapping("/query")
    public String query(HttpServletRequest request, HttpServletResponse response, String name) {
        return demoService.get(name);
    }

    @RequestMapping("/name")
    public String name(String name) {
        return demoService.get(name);
    }


}
