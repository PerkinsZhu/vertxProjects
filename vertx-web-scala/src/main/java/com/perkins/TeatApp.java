package com.perkins;

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

}
