package com.perkins.activity.test

import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.activiti.engine._
import org.activiti.engine.form.TaskFormData
import org.activiti.engine.runtime.ProcessInstance
import org.activiti.engine.task.{IdentityLink, Task}
import org.apache.commons.lang3.StringUtils
import org.joda.time.LocalDateTime
import org.junit.{Before, Test}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

class SimpleTest {
  val logger = LoggerFactory.getLogger(this.getClass)

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
    val fileName = "simple\\simple-01.bpmn"
    val de = rs.createDeployment
      .name("simpleProcess" + LocalDateTime.now().toString("YYYY-MM-DD HH:mm:ss"))
      .addClasspathResource(fileName)
      .deploy();
    println(de.getId)
    rs.createProcessDefinitionQuery().deploymentId(de.getId).list().forEach(i => println(i.getId))
  }

  /**
    * 77501
    * simple-01:1:77504
    * 90001
    * simple-01:2:90004
    *
    * 665001
    * simple-01:1:665004
    *
    * 685001
    * simple-01:2:685004
    *
    * 695001
    * simple-01:3:695004
    */
  val processDefinitionId = "simple-01:3:695004"

  @Test
  def startProcess(): Unit = {
    val instance = runs.startProcessInstanceById(processDefinitionId)
    println(instance.getDeploymentId)
    println(instance.getBusinessKey)
  }

  @Test
  def doTask(): Unit = {
    val loginUserId = "user-1-1"
    //    val loginUserId = "user-2-1"
    var task = ts.createTaskQuery().processDefinitionId(processDefinitionId).singleResult()
    val formData = fs.getTaskFormData(task.getId) // 用户需要填写的表达单
    println(s"${loginUserId}需要填写的表单：${new ObjectMapper().writeValueAsString(formData.getFormProperties)}")
    println(ts.getVariable(task.getId, "startTime"))
    println(ts.getVariable(task.getId, "endTime"))
    println(ts.getVariable(task.getId, "reason"))
    println(task.getAssignee())
    println(task.getOwner())
    ts.getIdentityLinksForTask(task.getId).forEach((link: IdentityLink) => {
      println(link.getGroupId)
      println(link.getUserId)
      println(link.getType)
      if (StringUtils.isNoneBlank(link.getGroupId)) {
        is.createUserQuery().memberOfGroup(link.getGroupId).list().forEach(user => {
          println(s"user ->${user.getId}")
        })
      }
    })
    /*  val formResult: java.util.HashMap[String, Object] = getFormResult(formData)
              ts.claim(task.getId, loginUserId) // 用户接受任务
      formResult.put("status", "ok")
      ts.complete(task.getId, formResult) // 申请人填写完申请，完成申请任务的处理*/
  }


  @Test
  def createTask(): Unit = {
    //    val task = ts.newTask("直接创建的task")
    //    ts.saveTask(task)
    ts.createTaskQuery().active().list().forEach((task: Task) => {
      println(task.getId)
      ts.deleteTask(task.getId, "直接删除任务")
    })

  }

  @Test
  def completeAllTask(): Unit = {
    runs.createProcessInstanceQuery().active().list().asScala.foreach(p => {
      runs.deleteProcessInstance(p.getProcessInstanceId, "")
    })
  }

  def completeTask(pdid: String): Unit = {
    //    val list = ts.createTaskQuery().processDefinitionId(pdid).list()
    val list = ts.createTaskQuery().list()
    if (list.isEmpty) return
    list.forEach(task => {
      println(s"${task.getId} ---  ${task.getName}")
      //      ts.complete(task.getId)
      ts.delegateTask(task.getId, "")
    })
    completeTask(pdid)
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

  @Test
  def getModule(): Unit = {
    rs.createModelQuery().list().forEach(m => {
      println(m.getName)
    })
    rs.createDeploymentQuery().list().forEach(d => {
      println("deployment" + d.getId)
      rs.createProcessDefinitionQuery().deploymentId(d.getId).list().forEach(i => {
        rs.addCandidateStarterUser(i.getId, "") //指定该流程允许哪些用户启动
        rs.addCandidateStarterGroup("", "") //指定某个流程只能由哪些组启动
        println(i.getId)
      })
    })
  }

  @Test
  def startProcess2(): Unit = {
    val instance = runs.startProcessInstanceById("process:1:11")
    println(instance.getDeploymentId)
    println(instance.getBusinessKey)
  }

  @Test
  def startTask(): Unit = {
    ts.createTaskQuery().list().forEach(t => {
      println(t.getId)
      ts.claim(t.getId, "user")
      ts.complete(t.getId)
    })
  }

  @Test
  def getDefinition(): Unit = {

    rs.createDeploymentQuery().list().forEach(a => {
      println(a.getId + a.getName + a.getKey)
    })
    rs.createProcessDefinitionQuery().list().forEach(a => {

    })
  }

}

