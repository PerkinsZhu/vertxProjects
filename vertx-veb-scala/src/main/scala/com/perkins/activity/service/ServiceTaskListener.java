package com.perkins.activity.service;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class ServiceTaskListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("=============触发 ServiceTaskListener ===============");
        delegateTask.setAssignee("user-03");
    }
}
