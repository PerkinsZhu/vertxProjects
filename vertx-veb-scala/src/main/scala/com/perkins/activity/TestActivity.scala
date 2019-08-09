package com.perkins.activity

import java.io.File
import java.util
import java.util.{HashMap, Map}

import com.perkins.activity.service.AuthService

import scala.collection.JavaConverters._
import org.activiti.engine.task.Task
import org.activiti.engine.{FormService, HistoryService, ProcessEngine, ProcessEngineConfiguration, RepositoryService, RuntimeService, TaskService}
import org.joda.time.LocalDateTime
import org.junit.{Before, Test}

import scala.collection.mutable


class TestActivity {
  var engine: ProcessEngine = null
  var rs: RepositoryService = null
  var ts: TaskService = null
  var runs: RuntimeService = null
  var fs: FormService = null
  var efs: org.activiti.form.api.FormService = null
  var hs: HistoryService = null

  @Before
  def init(): Unit = {
    engine = ProcessEngineConfiguration.createProcessEngineConfigurationFromResourceDefault.buildProcessEngine()
    rs = engine.getRepositoryService
    ts = engine.getTaskService
    runs = engine.getRuntimeService
    fs = engine.getFormService
    efs = engine.getFormEngineFormService
    hs = engine.getHistoryService
  }

  @Test
  def testFindRes(): Unit = {
    rs.createDeploymentQuery.list.forEach(d => {
      println(d.getName)
    })
    println("============================")
    ts.createTaskQuery().list().forEach(i => {
      println(s"${i.getId}--> ${i.isSuspended}")
    })
  }

  @Test
  def claimTask() {
    //    ts.claim("35011", "test01")
    runs.createProcessInstanceQuery().list().forEach(i => {
      val id = i.getProcessDefinitionId
      ts.complete(id)
      println(i.getId + "--" + i.getName + "--" + i.getProcessVariables)
    })

  }

  import org.junit.Test

  @Test
  def findMyPersonalTask(): Unit = {
    val assignee = "test01"
    val list = ts.createTaskQuery() //创建任务查询对象
      //      .taskAssignee(assignee) //指定个人任务查询，指定办理人
      .list();

    if (list != null && list.size > 0) {
      import scala.collection.JavaConversions._
      for (task <- list) {
        System.out.println("任务ID:" + task.getId)
        System.out.println("任务名称:" + task.getName)
        System.out.println("任务的创建时间:" + task.getCreateTime)
        System.out.println("任务的办理人:" + task.getAssignee)
        System.out.println("流程实例ID：" + task.getProcessInstanceId)
        System.out.println("执行对象ID:" + task.getExecutionId)
        System.out.println("流程定义ID:" + task.getProcessDefinitionId)
        System.out.println("########################################################")

        /* if (task.getAssignee == "廉斌" || task.getAssignee == "许方镪") {
           ts.complete(task.getId)

           ts.deleteTask(task.getId)
         }*/
      }
    }
  }


  @Test
  def testTask(): Unit = {
    val task = ts.newTask()
    task.setAssignee("user-1")
    task.setOwner("ower-1")

    ts.createTaskQuery().taskAssignee("user-1").list().forEach(i => {
      println(i)
    })
  }

  @Test
  def changeAssignee(): Unit = {
    val task = ts.createTaskQuery().singleResult()
    ts.setAssignee(task.getId, "user-03")
    println(ts.createTaskQuery().singleResult().getAssignee)
  }

  @Test
  def startProcess(): Unit = {
    rs.createProcessDefinitionQuery().list().forEach(i => println(i.getId))
    val instance = runs.startProcessInstanceById("rebackProcess:2:37504")
    println(instance.getId)
  }

  @Test
  def claimTask2(): Unit = {
    ts.createTaskQuery().list().forEach(i => {

      println(s"${i.getId} --> ${i.getName}-->${i.getFormKey}")

      //      i.setOwner("USER-10")

      //      ts.claim(i.getId, "user-03")
      val taskFormData = fs.getTaskFormData(i.getId)

      taskFormData.getFormProperties.forEach(i => println(s"${i.getId} -->${i.getName}-->${i.getType}-->${i.getValue}"))

      i.getProcessVariables.forEach((a, b) => {
        println(s"$a--> $b")
      })
      i.getTaskLocalVariables.forEach((a, b) => {
        println(s"$a--> $b")
      })

      val pd = rs.getProcessDefinition(i.getProcessDefinitionId)


    })
    //ts.setOwner("15002","user-100")

    //ts.claim("15002","user-10")

  }


  @Test
  def showForm(): Unit = {
    ts.getTaskEvents("25006").forEach(t => {

    })
    val form = fs.getRenderedTaskForm("25006")
    println(form)
    val data = fs.getTaskFormKey("vacationProcess:3:2545", "vacation")
    println(data)
  }

  @Test
  def deployRecallProcess(): Unit = {

    //    rs.createDeployment.name("recallProcess" + LocalDateTime.now.toString("YYYY-MM-DD HH:mm:ss")).addClasspathResource("recallProcess.bpmn").deploy

    //    rs.createDeploymentQuery().list().forEach(i => {println(s"${i.getName}--${i.getId}")})
    /*ts.createTaskQuery().list().forEach(i => {
      println(i.getName + "--->" + i.getId)
    })
    println("--------")*/
    //    runs.createProcessInstanceQuery().list().forEach(i => println(s"${i.getId} -->${i.getName}"))
    /* var task = ts.createTaskQuery().taskId("40005").singleResult()
     val id = task.getId
     println(task.getName + "-->" + task.getTaskDefinitionKey)
     ts.claim(id, "user-01")
     ts.complete(id)

     task = ts.createTaskQuery().taskId("40005").singleResult()
     println(task.getName + "-->" + task.getTaskDefinitionKey)

     runs.signalEventReceived("contactChangeSignal")
 */
    //    runs.signalEventReceived("contactChangeSignal")
    ts.createTaskQuery().list().forEach(i => {
      println(i.getName + "--->" + i.getId + "-->" + i.getProcessInstanceId)
    })
    val task = ts.createTaskQuery().processInstanceId("40001").singleResult();
    val id = task.getId
    /*ts.claim("45003","user-03")
    ts.complete("45003")*/
    ts.createTaskQuery().list().forEach(i => {
      println(i.getName + "--->" + i.getId)
    })
    //    ts.claim(id,"user-03")

    import scala.collection.JavaConverters._
    val variables = new util.HashMap[String, AnyRef]
    variables.put("开始日期", "2019-05-06")
    variables.put("结束日期", "2019-05-06")
    ts.claim(id, "user-03")
    //    ts.complete(id, variables)
    ts.createTaskQuery().list().forEach(i => {
      println(i.getName + "--->" + i.getId)
      i.getTaskLocalVariables.forEach((a, b) => {
        println(s"${a} ======> ${b}")
      })
    })

  }


  @Test
  def showHistory(): Unit = {
    val id = "40001"
    hs.createHistoricActivityInstanceQuery().processInstanceId(id).list().forEach(i => {
      println(s"${i.getId} -->${i.getActivityId}--${i.getActivityName}--${i.getActivityType}---${i.getAssignee}")
    })
  }

  def showTask(i: Task): Unit = {
    println(s"${i.getId} -->${i.getName}--${i.getAssignee}--${i.getProcessVariables.toString}---${i.getTaskLocalVariables}--${i.getExecutionId}" +
      s"----${i.getTenantId}")
    i.getTaskLocalVariables.asScala.foreach(i => println(s"${i._1} -->${i._2}"))
  }

  @Test
  def manyTaskTestSign(): Unit = {
    val defId = "rebackProcess:3:147504"
    val ins1 = runs.startProcessInstanceById(defId)
    val ins2 = runs.startProcessInstanceById(defId)

    ts.createTaskQuery().processDefinitionId(defId).list().forEach(i => {
      showTask(i)
    })

    ts.createTaskQuery().processDefinitionId(defId).list().forEach(i => {
      ts.complete(i.getId)
    })
    var sign = true
    var temp = ""
    var processInstanceId = ""
    ts.createTaskQuery().processDefinitionId(defId).list().forEach(i => {
      showTask(i)
      if (sign) {
        processInstanceId = i.getProcessInstanceId
        sign = false
      }
    })


    //会对所有的task产生影响
    showAllTask()
    runs.createExecutionQuery()
      .signalEventSubscriptionName("contactChangeSignal")
      .processInstanceId(processInstanceId) // 搜索某个instance下面的某个execution
      .list().forEach(i => {
      println(s"*******************${i.getId} -->${i.getName}")
      runs.signalEventReceived("contactChangeSignal", i.getId)
    })


    showAllTask()
    println()
    ts.createTaskQuery().processDefinitionId(defId).list().forEach(i => {
      showTask(i)
    })
    ts.createTaskQuery().processDefinitionId(defId).list().forEach(i => {
      ts.complete(i.getId)
    })
    ts.createTaskQuery().processDefinitionId(defId).list().forEach(i => {
      showTask(i)
    })
    ts.createTaskQuery().processDefinitionId(defId).list().forEach(i => {
      ts.complete(i.getId)
    })
    println("----end-----")
    ts.createTaskQuery().processDefinitionId(defId).list().forEach(i => {
      showTask(i)
    })

  }


  @Test
  def testEx(): Unit = {
    runs.createExecutionQuery()
      .signalEventSubscriptionName("contactChangeSignal")
      .processInstanceId("162501")
      .list().forEach(i => {
      println(s"${i.getId} -->${i.getName}")
    })
  }

  @Test
  def sendSign(): Unit = {
    runs.signalEventReceived("contactChangeSignal", "132512")

  }

  @Test
  def showAllTask(): Unit = {
    println("********************")
    ts.createTaskQuery().list().asScala.toList.foreach(showTask(_))
    println("********************")
  }

  @Test
  def completeAllTask(): Unit = {
    doComplete(ts.createTaskQuery().list().asScala.toList)
  }


  def doComplete(list: List[Task]): Unit = {
    if (list.isEmpty) return
    list.foreach(i => {
      val vars = new HashMap[String, Object]()
      vars.put("authService", new AuthService())
      vars.put("days", new Integer(1))
      runs.setVariablesLocal(i.getExecutionId, vars)
      ts.complete(i.getId)
      ts.deleteTask(i.getId)
    })
    doComplete(ts.createTaskQuery().list().asScala.toList)
  }

  @Test
  def deployed(): Unit = {
    val fileName = "listenerTest.bpmn"
    val de = rs.createDeployment
      .name("listenerTest" + LocalDateTime.now().toString("YYYY-MM-DD HH:mm:ss"))
      .addClasspathResource(fileName).deploy();
    println(de.getId)
  }

  @Test
  def showAllHistory(): Unit = {
    hs.createHistoricProcessInstanceQuery().processInstanceId("172501").list().forEach(i => println(i))
  }

  @Test
  def changeAgen(): Unit = {
    ts.createTaskQuery().list().asScala.toList.foreach(i => {
      ts.claim(i.getId, "user-03")
    })
  }

  //usertask4

  @Test
  def singleTest(): Unit = {
    val defId = "rebackProcess:4:182504"
    val ins1 = runs.startProcessInstanceById(defId)

    ts.createTaskQuery().processDefinitionId(defId).list().forEach(i => {
      showTask(i)
    })

    ts.createTaskQuery().processDefinitionId(defId).list().forEach(i => {
      ts.complete(i.getId)
    })

    var sign = true
    var temp = ""
    var processInstanceId = ""
    ts.createTaskQuery().processDefinitionId(defId).list().forEach(i => {
      showTask(i)
      if (sign) {
        processInstanceId = i.getProcessInstanceId
        sign = false
      }
    })


    //会对所有的task产生影响
    showAllTask()
    runs.createExecutionQuery()
      .signalEventSubscriptionName("contactChangeSignal")
      .processInstanceId(processInstanceId) // 搜索某个instance下面的某个execution
      .list().forEach(i => {
      println(s"*******************${i.getId} -->${i.getName}")
      runs.signalEventReceived("contactChangeSignal", i.getId)
    })


    showAllTask()
    println()
    ts.createTaskQuery().processDefinitionId(defId).list().forEach(i => {
      showTask(i)
    })
    ts.createTaskQuery().processDefinitionId(defId).list().forEach(i => {
      ts.complete(i.getId)
    })
    ts.createTaskQuery().processDefinitionId(defId).list().forEach(i => {
      showTask(i)
    })
    ts.createTaskQuery().processDefinitionId(defId).list().forEach(i => {
      ts.complete(i.getId)
    })
    println("----end-----")
    ts.createTaskQuery().processDefinitionId(defId).list().forEach(i => {
      showTask(i)
    })

  }


  @Test
  def listenerTest(): Unit = {
    val processId = "myProcess:11:312504"
    /*    val vars = new HashMap[String, Object]()
        vars.put("authService", new AuthService())
        vars.put("days", new Integer(1))
        runs.startProcessInstanceById(processId, vars)*/
    ts.createTaskQuery().processDefinitionId(processId).list().forEach(i => showTask(i))

    ts.createTaskQuery().processDefinitionId(processId).list().forEach(i => {
      val vars = new HashMap[String, Object]()
      vars.put("days", new Integer(1))
      runs.setVariablesLocal(i.getExecutionId, vars)
      ts.setVariablesLocal(i.getId, vars)
      ts.claim(i.getId, "user-03")
      ts.complete(i.getId)
      runs.setVariablesLocal(i.getExecutionId, vars)
      ts.setVariablesLocal(i.getId, vars)
    })
    ts.createTaskQuery().processDefinitionId(processId).list().forEach(i => showTask(i))

    Thread.sleep(5000)

  }

  @Test
  def cancleTask(): Unit = {
    ts.createTaskQuery().list().forEach(i => {
      ts.complete(i.getId)
      ts.deleteTask(i.getId, true)
    })

  }

  @Test
  def deleteInstance(): Unit = {
    val list = runs.createProcessInstanceQuery().list()
    println(list.size())
    list.forEach(i => {
      runs.deleteProcessInstance(i.getProcessInstanceId, "error")
    })
  }


  @Test
  def callBack(): Unit = {
    // todo 考虑如何实现任务的回退？

  }

}

/*

object App {
  val engine = ProcessEngineConfiguration.createProcessEngineConfigurationFromResourceDefault.buildProcessEngine()
  val rs = engine.getRepositoryService

  def main(args: Array[String]): Unit = {

    val fileName = "reacallTest2.bpmn"

    val de = rs.createDeployment
      .name("reacallTest2" + LocalDateTime.now().toString("YYYY-MM-DD HH:mm:ss"))
      .addClasspathResource(fileName).deploy();
    println(de.getId)
    rs.createProcessDefinitionQuery().deploymentId(de.getId).list().forEach(i => println(i.getId))

  }
}
*/
