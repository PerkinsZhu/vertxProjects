package com.perkins;

import io.thekraken.grok.api.Grok;
import io.thekraken.grok.api.Match;
import io.thekraken.grok.api.exception.GrokException;
import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeatApp {


    @Test
    public void testDe() {
        //1.加载Activiti.cfg.xml配置文件对应的配置对象
        ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createProcessEngineConfigurationFromResourceDefault();

        //2.通过processEngineConfiguratoin对象获取ProcessEngine实例
        ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();
        Assert.assertNotNull(processEngine);

        //3.通过processEngine对象获取activiti的service
        RepositoryService repositoryService = processEngine.getRepositoryService();

        //4.通过RepositoryService发布流程到数据库
        repositoryService.createDeployment()
                .name("测试--LeaveProcess--" + LocalDateTime.now().toString("YYYY-MM-DD HH:mm:ss"))
                .addClasspathResource("LeaveProcess.bpmn").deploy();

        //5.通过processEngine对象获取activiti的RuntimeService
        RuntimeService runtimeService = processEngine.getRuntimeService();

        //将信息加入map,以便传入流程中
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("employeeName", "廉斌");
        variables.put("day", 10);

        //6.通过RuntimeService开启流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leaveProcess", variables);

        //7.流程往下走   通过TaskService获取流程任务
        TaskService taskService = processEngine.getTaskService();

        //8.查询当前任务
        List<Task> list = taskService.createTaskQuery().list();
        for (int i = 0; i < list.size(); i++) {
            Task task = list.get(i);
            System.out.println(task.getName() + "---" + task.getProcessInstanceId());
            System.out.println(task.toString());
        }
      /*  Assert.assertEquals("helloworld Task", task.getName());

        //完成任务  流程往下走
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().singleResult();
        Assert.assertEquals("world Task", task.getName());*/


    }

    private ProcessEngine processEngine = null;

    @Before
    public void init() {
        ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createProcessEngineConfigurationFromResourceDefault();
        processEngine = processEngineConfiguration.buildProcessEngine();
    }


    @Test
    public void testQj() {
        RuntimeService runtimeService = processEngine.getRuntimeService();

/*        //将信息加入map,以便传入流程中
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("开始日期", "2019-05-06");
        variables.put("结束日期", "2019-05-06");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("qingjialiucheng",variables);
        String processInstanceId = processInstance.getId();
        System.out.println("流程开启成功.......实例流程id:"+processInstanceId);*/

        String processInstanceId = "37501";
        ProcessInstance instance = processEngine.getRuntimeService().createProcessInstanceQuery()//获取查询对象
                .processInstanceId(processInstanceId).singleResult();

        //7.流程往下走   通过TaskService获取流程任务
        TaskService taskService = processEngine.getTaskService();

        //8.查询当前任务
        List<Task> list = taskService.createTaskQuery().list();
        for (int i = 0; i < list.size(); i++) {
            Task task = list.get(i);
            System.out.println(task.getName() + "---" + task.getProcessInstanceId());
            System.out.println(task.toString());
        }

        Task task = taskService.createTaskQuery()//创建查询对象
                .processInstanceId(processInstanceId)//通过流程实例id来查询当前任务
                .singleResult();//获取单个查询结果

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("开始日期", "2019-05-06");
        variables.put("结束日期", "2019-05-06");
        //完成任务
        processEngine.getTaskService().claim(task.getId(), "test01");
        processEngine.getTaskService().complete(task.getId(), variables);


        System.out.println(task.getClaimTime());

    }


    @Test
    public void listAllDeployment() {
        RepositoryService rs = processEngine.getRepositoryService();
        List<Deployment> list = rs.createDeploymentQuery().list();
        for (int i = 0; i < list.size(); i++) {
            Deployment de = list.get(i);
            System.out.println(de.getName());
            if (de.getName() == null) {
//                rs.deleteDeployment(de.getId());
            }
        }
    }

    @Test
    public void deplopyRecall() {
        //1.加载Activiti.cfg.xml配置文件对应的配置对象
        ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createProcessEngineConfigurationFromResourceDefault();

        //2.通过processEngineConfiguratoin对象获取ProcessEngine实例
        ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();
        Assert.assertNotNull(processEngine);

        //3.通过processEngine对象获取activiti的service
        RepositoryService repositoryService = processEngine.getRepositoryService();
        String fileName = "D:\\eclipseWorkSpace\\activitytest\\src\\main\\resources\\diagrams\\serviceTask.bpmn";
        //4.通过RepositoryService发布流程到数据库
        repositoryService.createDeployment()
                .name("serviceTask" + LocalDateTime.now().toString("YYYY-MM-DD HH:mm:ss"))
                .addClasspathResource(fileName).deploy();


    }


    @Test
    public void testGroy(){
        String path ="D:\\myProjects\\vertxProjects\\vertx-web-scala\\src\\main\\java\\com\\perkins\\patterns.txt";
        String pattern = "%{MONTH}\\s+%{MONTHDAY}\\s+%{TIME}\\s+%{YEAR}.*%{fromIP}";
        String message = "Mon Nov  9 06:47:33 2015; UDP; eth1; 461 bytes; from 88.150.240.169:tag-pm";
        String message2 = "[idc=shyc2,app=crm-b570ce,pod=crm-b570ce-7f47457794-52ndh,filename=api-gateway.log] 2019-09-03 11:52:50.349 [vert.x-eventloop-thread-1] WARN  io.its.api.MainGatewayVerticle - -> Start Api GateWay [SUCCESS], Listen Port:8080";
//        String pattern2 = "%{MY_DATESTAMP}";
//        String pattern2 = "%{MID}";
        String pattern2 = "%{MESSAGE}";
        Match match = null;
        try {
            Grok grok = new Grok();
            //添加patter配置文件,默认的grok的pattern是null
            grok.addPatternFromFile(path);
            //添加自定义pattern，当然%{IPV4}可以不用已有pattern，也可以自定义正则表达式
//            grok.addPattern("fromIP", "%{IPV4}");
            grok.compile(pattern2);
            match = grok.match(message2);
            match.captures();
            if (!match.isNull()) {
                System.out.println(match.toMap().toString());
                System.out.println(match.toJson().toString());
            } else {
                System.out.println("not match");
            }
        } catch (GrokException e) {
            e.printStackTrace();
            match = null;
        }
    }


}
