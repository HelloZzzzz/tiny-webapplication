package com.lzb.servlet;

import com.lzb.annotations.Autowired;
import com.lzb.annotations.Controller;
import com.lzb.annotations.RequestMapping;
import com.lzb.annotations.Service;
import com.lzb.config.SpringmvcConfig;
import com.lzb.pojo.Handler;
import com.lzb.utils.CustomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * @Author : LZB
 * @Date : 2020/11/27
 * @Description : 前端总控转发器
 */
public class DispatcherServlet extends HttpServlet {
    private Properties properties = new Properties();

    private SpringmvcConfig springmvcConfig = new SpringmvcConfig();


    /**
     * 缓存扫描到的类的全限定类名
     */
    private List<String> classNames = new ArrayList<>();


    /**
     * IOC
     */
    private Map<String, Object> servletSingletonObjects = new HashMap();


    /**
     * url 和 Handler 之间的映射关系
     */
    private List<Handler> handlerMapping = new ArrayList<>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //TODO filter.CharacterEncodingFilter
        req.setCharacterEncoding("UTF-8");

        Handler handler = getHandler(req);
        if (null == handler) {
            resp.getWriter().println("404 not found ");
            return;
        }

        //获取参数长度
        Object[] args = new Object[handler.getMethod().getParameterCount()];

        Map<String, String[]> parameterMap = req.getParameterMap();
        parameterMap.forEach((key, value) -> {


            // name=1&name=2   name [1,2]
            String s = StringUtils.join(value, ",");
            // 方法形参确实有该参数，找到它的索引位置，对应的把参数值放入paraValues
            Integer index = handler.getParamIndexMapping().get(key);
            try {
                //TODO filter.CharacterEncodingFilter
                args[index] = new String(s.getBytes("ISO8859-1"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            ;
        });


        Parameter[] parameters = handler.getMethod().getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.getType() == HttpServletRequest.class) {
                args[i] = req;
            }
            if (parameter.getType() == HttpServletResponse.class) {
                args[i] = resp;
            }
        }

        try {
            //TODO 视图解析器
            Object result = handler.getMethod().invoke(handler.getController(), args);
            //TODO filter.CharacterEncodingFilter
            resp.setContentType("text/html;charset=UTF-8");
            resp.getWriter().println(result);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void init(ServletConfig config) {

        //1、doLoadConfig 加载配置文件
        doLoadConfig(config);
        //2、doScan 扫描所有的包  @Controller注解对应的方法
        doScan();
        //3、doInstance 实例化
        doInstance();
        //4、doAutoWired 装配
        doAutoWired();
        //5、initHandlerMapping 建立url与Handler 的对应关系
        initHandlerMapping();

        System.out.println("初始化完成...");


    }

    private void doLoadConfig(ServletConfig config) {
        String contextConfigLocation = config.getInitParameter("contextConfigLocation");
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            //优先加载XML
            if (contextConfigLocation.endsWith(".xml")) {
                Document document = new SAXReader().read(resourceAsStream);
                //<configuation>
                Element rootElement = document.getRootElement();
                //<component-scan base-package="com.lzb" />
                List<Element> componentScan = rootElement.selectNodes("//component-scan");
                for (Element element : componentScan) {
                    String basePackage = element.attributeValue("base-package");
                    springmvcConfig.getBasePackage().add(basePackage);
                }
            } else if (contextConfigLocation.endsWith(".properties")) {
                properties.load(resourceAsStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void doScan(String scanPackage) {
        String scanPackagePath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + scanPackage.replaceAll("\\.", "/");
        File pack = new File(scanPackagePath);

        File[] files = pack.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                // 递归
                doScan(scanPackage + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = scanPackage + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }

        }
    }

    private void doScan() {
        if (0 != springmvcConfig.getBasePackage().size()) {
            for (String basePackage : springmvcConfig.getBasePackage()) {
                doScan(basePackage);
            }
        } else {
            doScan(properties.getProperty("scanPackage"));
        }
    }


    private void doInstance() {
        if (0 != classNames.size()) {
            //TODO 与spring整合
            try {
                for (String className : classNames) {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class)) {
                        //TODO 支持指定名
                        String name = CustomStringUtils.toLowerCaseFirstOne(clazz.getSimpleName());
                        Object o = clazz.newInstance();
                        servletSingletonObjects.put(name, o);
                    }

                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (int i = 0; i < interfaces.length; i++) {
                        Class<?> anInterface = interfaces[i];
                        // 以接口的全限定类名作为id放入
                        servletSingletonObjects.put(anInterface.getName(), clazz.newInstance());
                    }


                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void doAutoWired() {
        if (!servletSingletonObjects.isEmpty()) {
            servletSingletonObjects.forEach((key, value) -> {
                Field[] declaredFields = value.getClass().getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    if (declaredField.isAnnotationPresent(Autowired.class)) {
                        declaredField.setAccessible(true);
                        try {
                            declaredField.set(value, servletSingletonObjects.get(declaredField.getType().getName()));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private void initHandlerMapping() {
        if (!servletSingletonObjects.isEmpty()) {
            servletSingletonObjects.forEach((key, value) -> {
                Class<?> clazz = value.getClass();
                if (clazz.isAnnotationPresent(Controller.class)) {
                    String baseName = clazz.getAnnotation(RequestMapping.class).value();
                    for (Method declaredMethod : clazz.getDeclaredMethods()) {
                        if (declaredMethod.isAnnotationPresent(RequestMapping.class)) {
                            String url = declaredMethod.getAnnotation(RequestMapping.class).value();
                            Handler handler = new Handler();
                            //debug
                            handler.setController(value);
                            handler.setMethod(declaredMethod);
                            handler.setUrl(baseName + url);
                            Parameter[] parameters = declaredMethod.getParameters();
                            Map<String, Integer> map = new HashMap<>(10);
                            for (int i = 0; i < parameters.length; i++) {
                                // 为了方法调用 method.invoke(obj,args);
                                map.put(parameters[i].getName(), i);
                            }
                            handler.setParamIndexMapping(map);
                            handlerMapping.add(handler);


                        }
                    }

                }


            });
        }

    }


    private Handler getHandler(HttpServletRequest req) {
        if (!handlerMapping.isEmpty()) {
            String url = req.getRequestURI();
            for (Handler handler : handlerMapping) {
                if (url.equals(handler.getUrl())) {
                    return handler;
                }
            }
        }
        return null;
    }
}
