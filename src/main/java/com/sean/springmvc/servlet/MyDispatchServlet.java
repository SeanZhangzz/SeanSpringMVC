package com.sean.springmvc.servlet;

import com.sean.springmvc.annotation.*;
import com.sean.springmvc.handler.HandlerAdapterService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@SeanScanPackage("com.sean.springmvc")
public class MyDispatchServlet extends HttpServlet {

    private static final String basePackage = "com.sean.springmvc";

    // 类名（首字母小写）到其全路径的映射
    private Map<String, Class> beansClassMap = new HashMap<String, Class>();

    // 类到实例对象的映射
    private Map<String, Object> beansMap = new HashMap<String, Object>();

    // 请求路径到方法的映射
    private Map<String, Method> handlerMapping = new HashMap<String, Method>();


    @Override
    public void init() throws ServletException {

        System.out.println("MyDispatchServlet init...");

        //扫描所有类 (获取到每个带SeanController/SeanService注解的名称和类的对照关系，放入Map中)
        scanPackage(basePackage);

        //实例化所有的类,放入缓存池中
        initBeans();

        //解析bean的属性并注入（基于beansMap）
        initBeanDependecy();

        //扫描controller中的方法
        scanHandler();
    }

    private void scanPackage(String packageName) {
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File urlFile = new File(url.getFile().replaceFirst("/","").replaceAll("%20"," "));
        for (File file : urlFile.listFiles()) {
            if (file.isDirectory()) {
                scanPackage(packageName + "." + file.getName());
            } else { //file
                try {
                    Class clazz = Class.forName(packageName + "." + file.getName().replace(".class", ""));
                    if (clazz.isAnnotationPresent(SeanService.class)) {
                        SeanService seanService = (SeanService) clazz.getAnnotation(SeanService.class);
                        beansClassMap.put(seanService.value(), clazz);
                    } else if (clazz.isAnnotationPresent(SeanController.class)) {
                        SeanController seanController = (SeanController) clazz.getAnnotation(SeanController.class);
                        beansClassMap.put(seanController.value(), clazz);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initBeans() {
        if (beansClassMap.size() <= 0) {
            return;
        }

        for (Map.Entry<String, Class> beanClass : beansClassMap.entrySet()) {
            Class clazz = beanClass.getValue();
            String key = null;

            if (clazz.isAnnotationPresent(SeanController.class)) {
                SeanRequestMapping seanRequestMapping = (SeanRequestMapping) clazz.getAnnotation(SeanRequestMapping.class);
                key = seanRequestMapping.value();
            } else if (clazz.isAnnotationPresent(SeanService.class)) {
                SeanService seanService = (SeanService) clazz.getAnnotation(SeanService.class);
                key = seanService.value();
            } else {
                continue;
            }

            try {
                Object instance = clazz.newInstance();
                beansMap.put(key, instance);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

    private void initBeanDependecy() {
        if (beansMap.size() <= 0) {
            return;
        }

        for (Map.Entry<String, Object> beanClass : beansMap.entrySet()) {
            Object targetInstance = beanClass.getValue();
            Class clazz = targetInstance.getClass();
            Field[] fields = clazz.getDeclaredFields();
            if (fields == null || fields.length <= 0) {
                continue;
            }

            // 给bean的属性注入依赖
            // 在springmvc中，所有controller都需要在启动时实例化，连带着所有的service、dao也需要？？？
            for (Field field : fields) {
                if (field.isAnnotationPresent(SeanAutowired.class)) {
                    SeanAutowired seanAutowired = field.getAnnotation(SeanAutowired.class);
                    Object fieldInstance = beansMap.get(seanAutowired.value()); //待注入的属性实例对象

                    try {
                        field.setAccessible(true); //打开私有变量的赋值权限
                        field.set(targetInstance, fieldInstance);
                        field.setAccessible(false);

                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                }
            }

        }

    }

    private void scanHandler() {
        if (beansClassMap.size() <= 0) {
            return;
        }

        for (Map.Entry<String, Class> beanClass : beansClassMap.entrySet()) {
            Class clazz = beanClass.getValue();
            if (clazz.isAnnotationPresent(SeanController.class)) {
                String basePath = ((SeanRequestMapping) clazz.getAnnotation(SeanRequestMapping.class)).value();
                for (Method method : clazz.getMethods()) {
                    if (method.isAnnotationPresent(SeanRequestMapping.class)) {
                        SeanRequestMapping seanRequestMapping = method.getAnnotation(SeanRequestMapping.class);
                        String methodPath = seanRequestMapping.value();
                        handlerMapping.put(basePath + methodPath, method);
                    }
                }
            }
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        doPost(req, resp);
    }

    @Override // 分发请求
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String requestURI = req.getRequestURI(); // /mvcdemo/dept/insertUser
        String contextPath = req.getContextPath(); // /mvcdemo

        String path = requestURI.replace(contextPath,"");
        Method method = handlerMapping.get(path);
        Object controller = beansMap.get("/" + path.split("/")[1]); // key="/dept"
        //Object[] args = new Object[]{"sean","hello"};
        // 处理参数
        HandlerAdapterService ha = (HandlerAdapterService) beansMap.get("customHandlerAdapter");
        Object[] args = ha.handle(req, resp, method, beansMap);
        try {
            Object o = method.invoke(controller, args);
            resp.getWriter().println(o); // 输出内容
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
