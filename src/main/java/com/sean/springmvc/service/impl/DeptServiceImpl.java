package com.sean.springmvc.service.impl;

import com.sean.springmvc.annotation.SeanService;
import com.sean.springmvc.service.DeptService;

@SeanService("deptService")
public class DeptServiceImpl implements DeptService {
    public String insert(String username, String dept) {
        System.out.println("username: " + username + ", dept: " + dept);
        return "username: " + username + ", dept: " + dept;
    }
}
