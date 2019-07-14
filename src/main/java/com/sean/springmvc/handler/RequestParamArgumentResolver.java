package com.sean.springmvc.handler;

import com.sean.springmvc.annotation.SeanRequestParam;
import com.sean.springmvc.annotation.SeanService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@SeanService("requestParamArgumentResolver")
public class RequestParamArgumentResolver implements ArgumentResolver  {

    public boolean support(Class<?> type, int paramIndex, Method method) {
        // type = class java.lang.String
        // @SeanRequestParam("name")String name
        //获取当前方法的参数
        Annotation[][] an = method.getParameterAnnotations();
        Annotation[] paramAns = an[paramIndex];

        for (Annotation paramAn : paramAns) {
            //判断传进的paramAn.getClass()是不是 SeanRequestParam 类型
            if (SeanRequestParam.class.isAssignableFrom(paramAn.getClass())) {
                return true;
            }
        }

        return false;
    }

    public Object argumentResolver(HttpServletRequest request,
                                   HttpServletResponse response, Class<?> type, int paramIndex,
                                   Method method) {

        //获取当前方法的参数
        Annotation[][] an = method.getParameterAnnotations();
        Annotation[] paramAns = an[paramIndex];

        for (Annotation paramAn : paramAns) {
            //判断传进的paramAn.getClass()是不是 SeanRequestParam 类型
            if (SeanRequestParam.class.isAssignableFrom(paramAn.getClass())) {
                SeanRequestParam cr = (SeanRequestParam) paramAn;
                String value = cr.value();

                return request.getParameter(value);
            }
        }
        return null;
    }

}
