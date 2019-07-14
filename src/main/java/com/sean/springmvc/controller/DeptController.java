package com.sean.springmvc.controller;

import com.sean.springmvc.annotation.SeanAutowired;
import com.sean.springmvc.annotation.SeanController;
import com.sean.springmvc.annotation.SeanRequestMapping;
import com.sean.springmvc.annotation.SeanRequestParam;
import com.sean.springmvc.service.DeptService;

@SeanController("deptController")
@SeanRequestMapping("/dept")
public class DeptController {

    @SeanAutowired("deptService")
    private DeptService deptService;

    @SeanRequestMapping("/insertUser.do")
    public String insertUser(@SeanRequestParam("username")String username, @SeanRequestParam("dept") String dept) {
        return deptService.insert(username, dept);
    }

}
