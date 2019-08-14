package com.perkins.activity.vacation

import com.fasterxml.jackson.databind.ObjectMapper
import org.activiti.engine.{task, _}
import org.activiti.engine.form.TaskFormData
import org.activiti.engine.task.Task
import org.joda.time.LocalDateTime
import org.junit.{Before, Test}

import scala.collection.JavaConverters._

class VacationTest {
  var engine: ProcessEngine = null
  var rs: RepositoryService = null
  var ts: TaskService = null
  var runs: RuntimeService = null
  var fs: FormService = null
  var efs: org.activiti.form.api.FormService = null
  var hs: HistoryService = null
  var is: IdentityService = null

  @Before
  def init(): Unit = {
    engine = ProcessEngineConfiguration.createProcessEngineConfigurationFromResourceDefault.buildProcessEngine()
    rs = engine.getRepositoryService
    ts = engine.getTaskService
    runs = engine.getRuntimeService
    fs = engine.getFormService
    efs = engine.getFormEngineFormService
    hs = engine.getHistoryService
    is = engine.getIdentityService
  }

  @Test
  def deployed(): Unit = {
    val fileName = "vacation\\variableProcess-v3.bpmn"
    val de = rs.createDeployment
      .name("variableProcess-v3" + LocalDateTime.now().toString("YYYY-MM-DD HH:mm:ss"))
      .addClasspathResource(fileName).deploy();
    println(de.getId)
    rs.createProcessDefinitionQuery().deploymentId(de.getId).list().forEach(i => println(i.getId))
  }

  def getFormResult(formData: TaskFormData): java.util.HashMap[String, Object] = {
    val result = new java.util.HashMap[String, Object]()
    formData.getFormProperties.forEach(i => {
      println(s"待填写表达结构：${new ObjectMapper().writeValueAsString(i)}")
      val value: Any = i.getId match {
        case "startTime" => LocalDateTime.now().toString("yyyy-MM-dd HH:mm:ss")
        case "endTime" => LocalDateTime.now().toString("yyyy-MM-dd HH:mm:ss")
        case "reason" => "请假原因是因为……"
        case "days" => 5l
        case "result_step01" => true
        case "result_step02" => true
        case "result_step03" => true
        case "result_step04" => true
        case "realStartTime" => LocalDateTime.now().toString("yyyy-MM-dd HH:mm:ss")
      }
      result.put(i.getId, value.asInstanceOf[Object])
    })
    return result
  }

  val processDefinitionId = "vacationProcess:6:535004"
  val deploymentId = "535001"

  @Test
  def step01(): Unit = {
    /**
      * 535001
      * vacationProcess:6:535004
      */
    // 申请人开始请假并填写请假表单
    val loginUserId = "user-1-1"
    // 开始请假流程
    val instance = runs.startProcessInstanceById(processDefinitionId)
    // 页面获取表单字段，UI自动渲染，由用户自行填写
    var task = ts.createTaskQuery().processDefinitionId(processDefinitionId).singleResult()
    val formData = fs.getTaskFormData(task.getId)
    ts.claim(task.getId, loginUserId) // 用户接受任务
    // 模仿用户填写表单过程
    val formResult: java.util.HashMap[String, Object] = getFormResult(formData)
    runs.setVariables(task.getExecutionId, formResult) // 设置用户表单为全局便量，供各个节点查看
    ts.complete(task.getId, formResult) // 申请人填写完申请，完成申请任务的处理
    task = ts.createTaskQuery().processDefinitionId(processDefinitionId).singleResult()
    showTask(task)
  }

  /*  @Test
    def strp02(): Unit = {
      val loginUserId = "user-2-1"
      var task = ts.createTaskQuery().processDefinitionId(processDefinitionId).singleResult()
      showTask(task)
      ts.claim(task.getId, loginUserId) //项目负责人接单
      // 模拟项目负责人查看申请变量
      println(s"项目负责人查看请假数据：${runs.getVariables(task.getExecutionId)}")
      val formData = fs.getTaskFormData(task.getId) // 项目负责人需要填写的表达
      println(s"项目负责人需要填写的表单：${new ObjectMapper().writeValueAsString(formData.getFormProperties)}")
      val formResult: java.util.HashMap[String, Object] = getFormResult(formData)
      println(s"项目负责人审批结果：${formResult}")
  //    ts.setVariablesLocal(task.getId,formResult)
      runs.setVariables(task.getExecutionId, formResult) // 项目负责人审批结果设置为全局变量
      ts.complete(task.getId, formResult) //项目负责人处理结束
      showTask(task)
    }*/

  @Test
  def doTask(): Unit = {
    val loginUserId = "user-2-1" // 当前登录的用户
    var task = ts.createTaskQuery().processDefinitionId(processDefinitionId).singleResult()
    showTask(task)
    ts.claim(task.getId, loginUserId) //用户接单
    // 模拟用户查看申请数据
    println(s"${loginUserId}查看请假数据：${runs.getVariables(task.getExecutionId)}")
    val formData = fs.getTaskFormData(task.getId) // 用户需要填写的表达单
    println(s"${loginUserId}需要填写的表单：${new ObjectMapper().writeValueAsString(formData.getFormProperties)}")
    val formResult: java.util.HashMap[String, Object] = getFormResult(formData)
    println(s"${loginUserId}处理结果：${formResult}")
    runs.setVariables(task.getExecutionId, formResult) // 用户处理结果设置为全局变量
    ts.complete(task.getId, formResult) //用户处理结束
    task = ts.createTaskQuery().processDefinitionId(processDefinitionId).singleResult()
    showTask(task)

  }


  def showTask(i: Task): Unit = {
    println(s"当前task信息：\r\t===========================================================\r\t${i.getId} -->${i.getName}--${i.getOwner()}--${i.getAssignee}--${i.getDelegationState}--${i.getProcessVariables.toString}---${i.getTaskLocalVariables}--${i.getExecutionId}" +
      s"----${i.getTenantId}----${i.getProcessInstanceId}")
    i.getTaskLocalVariables.asScala.foreach(i => println(s"${i._1} -->${i._2}"))
    println("\r\t===========================================================\r\t")
  }

  @Test
  def getHistory(): Unit = {
    val instanceId = "557501"
    println("历史活动记录：")
    hs.createHistoricActivityInstanceQuery().processInstanceId(instanceId).list().forEach(i => {
      println(i)
    })
    println("历史流程实例：")
    hs.createHistoricProcessInstanceQuery().processInstanceId(instanceId).list().forEach(i => {
      println(i)
      println("历史流程变量：")
      hs.createHistoricVariableInstanceQuery().processInstanceId(i.getId).list().forEach(i => {
        println(i)
      })
    })
    println("历史流程变量详细记录：")
    hs.createHistoricDetailQuery().processInstanceId(instanceId).list().forEach(i => println(i))
  }

  @Test
  def otherTest(): Unit = {

    /*task.setDueDate(LocalDateTime.parse("2019-10-23 00:00:00").toDate()) // 设置任务到日日期
    task.setPriority(2) // 设置任务的优先级
    ts.saveTask(task)
*/
    // runs.setVariable()
    //TODO 这里可以设置下一节点的处理人，如果有代理逻辑，在此处可以设置代理人
    //    is.setAuthenticatedUserId() //TODO 确认该方法的目的是什么？
    //TODO 测试 局部变量和全局变量以及表单结果如何传递？ 表单结果要通过全局变量的机制进行传递吗？

  }


  @Test
  def variableTest(): Unit = {
    /**
      * 572501
      * myProcess:13:572504
      *
      */
    val proId = "myProcess:13:572504"
    //    val instance = runs.startProcessInstanceById(proId)
    var task = ts.createTaskQuery().processDefinitionId(proId).singleResult()
    showTask(task)
    val var1 = "var--01"
    val var2 = "var--02"
    val var3 = "var--03"
    val var4 = "var--04"
    /*
        ts.setVariable(task.getId, var1, var1) //可以夸task
        ts.setVariableLocal(task.getId, var2, var2) // 无法夸task传递
        runs.setVariable(task.getExecutionId, var3, var3)// 可以夸task 且包含设置的local参数
        runs.setVariableLocal(task.getExecutionId, var4, var4) // 可以夸task，估计不能夸execution 。
    */

    showVargs(task.getId, task.getExecutionId)

    println("=====complete  1======")
    ts.complete(task.getId)
    task = ts.createTaskQuery().processDefinitionId(proId).singleResult()
    showVargs(task.getId, task.getExecutionId)

    println("=====complete  2======")
    ts.complete(task.getId)
    task = ts.createTaskQuery().processDefinitionId(proId).singleResult()
    showVargs(task.getId, task.getExecutionId)

  }


  @Test
  def testInExecution(): Unit = {
    /**
      * 585001
      * myProcess:15:585004
      *
      * 测试结果：
      * 只要是setVariable，无论是通过ts还是runs设置的，在该实例销毁前每个节点都是可以访问的，即使夸execution也是可以的
      *     ts.setVariablesLocal设置的参数只能在该task complete 之情可以访问，之后就不可以访问
      *     runs.setVariablesLocal 只要在同一个execution上都可以访问到，夸execution无法访问
      * 也即是：
      * variable是全局的，整个instance生命周期都可以访问
      * variableLocal是局部的，其中ts.setVariableLocal 同一个task内访问。runs.VariableLocal 在同一个execution中可访问
      * 其中：
      * 对于runs.variableLocal 参数 ，主干节点携带的参数可以传递到分支流程中
      */

    /** 610001
      * variableProcess-v3:3:610003
      * */
    val pdid = "variableProcess-v3:3:610003"
    val instance = runs.startProcessInstanceById(pdid)
    showAndCompleteTask(pdid)
  }

  def showAndCompleteTask(pdid: String): Unit = {
    val list = ts.createTaskQuery().processDefinitionId(pdid).list()
    if (list.isEmpty) return
    list.forEach(task => {
      val tid = task.getId
      val tName = task.getName
      val exId = task.getExecutionId
      println(s"===============进入${tName}===================")
      println(s"ts.getVariables----->${ts.getVariables(tid)}")
      println(s"ts.getVariablesLocal----->${ts.getVariablesLocal(tid)}")
      println(s"runs.getVariables----->${runs.getVariables(exId)}")
      println(s"runs.getVariablesLocal----->${runs.getVariablesLocal(exId)}")
      val a = s"$tName--tsVar"
      ts.setVariable(tid, a, a)
      val b = s"$tName--tsVarL"
      ts.setVariableLocal(tid, b, b)
      val c = s"$tName--runsVar"
      runs.setVariable(exId, c, c)
      val d = s"$tName--runsVarL"
      runs.setVariableLocal(exId, d, d)
      println(s"*********************设置${tName} 参数后*********************")
      println(s"ts.getVariables----->${ts.getVariables(tid)}")
      println(s"ts.getVariablesLocal----->${ts.getVariablesLocal(tid)}")
      println(s"runs.getVariables----->${runs.getVariables(exId)}")
      println(s"runs.getVariablesLocal----->${runs.getVariablesLocal(exId)}")
      ts.complete(tid)
      println(s"===============结束${tName}===================")
    })
    showAndCompleteTask(pdid)
  }

  def showVargs(id: String, getExecutionId: String): Unit = {
    val var1 = "var--01"
    val var2 = "var--02"
    val var3 = "var--03"
    val var4 = "var--04"
    println(ts.getVariables(id))
    println(ts.getVariablesLocal(id))
    println(runs.getVariables(getExecutionId))
    println(runs.getVariablesLocal(getExecutionId))
  }
}
