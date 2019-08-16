package com.perkins.activity.service

import org.activiti.engine.delegate.{BpmnError, DelegateExecution, JavaDelegate}

class ErrorEventService extends JavaDelegate {
  override def execute(execution: DelegateExecution): Unit = {
    println("call ErrorEventService")
    throw new BpmnError("call ErrorEventService error")
  }
}

class SubEventService extends JavaDelegate {
  override def execute(execution: DelegateExecution): Unit = {
    println("call SubEventService")
    //TODO 如何在这里获取runService 来触发信号事件
    //    throw new BpmnError("call SubEventService error")
  }
}

class SignalBoundaryService extends JavaDelegate {
  override def execute(execution: DelegateExecution): Unit = {
    println("call SignalBoundaryService")
    //    throw new BpmnError("call SubEventService error")
  }
}

class TimerBoundaryService extends JavaDelegate {
  override def execute(execution: DelegateExecution): Unit = {
    println("call TimerBoundaryService")
    //    throw new BpmnError("call SubEventService error")
  }
}