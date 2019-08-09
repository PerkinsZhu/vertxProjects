package com.perkins.activity.service;

import java.io.Serializable;

public class AuthService implements Serializable {

    public String getUserAssignee = "suer-100";
    public AuthService() {

    }

    public String getGetUserAssignee() {
        return getUserAssignee;
    }

    public void setGetUserAssignee(String getUserAssignee) {
        this.getUserAssignee = getUserAssignee;
    }

    public String getUserAssignee() {
        System.out.println("--getUserAssignee--");
        return "user-100";
    }

public String getTarget(){

        return "usertask3";
}

    public void doTask() {
        System.out.println("----doTask---");
    }
}
