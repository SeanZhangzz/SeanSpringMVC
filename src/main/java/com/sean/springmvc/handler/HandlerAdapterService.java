package com.sean.springmvc.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;

public interface HandlerAdapterService {

    public Object[] handle(HttpServletRequest req, HttpServletResponse resp,
                           Method method, Map<String, Object> beans);
}
