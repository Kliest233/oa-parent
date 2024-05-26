package com.atguigu.auth.activiti;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class ProcessTestDemo1 {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    //部署流程定义 uel-value
    @Test
    public void deployProcess(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("process/jiaban.bpmn20.xml")
                .name("加班申请流程").deploy();
        System.out.println(deployment.getId());
        System.out.println(deployment.getName());
    }

    //启动流程示例
    @Test
    public void startProcess(){
        //设置任务人
        Map<String,Object> map=new HashMap<>();
        map.put("assignee1","lucy");
        map.put("assignee2","mary");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("jiaban", map);
        System.out.println(processInstance.getProcessDefinitionId());
        System.out.println(processInstance.getId());
    }


    //部署流程定义 uel-method
    @Test
    public void deployProcess01(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("process/jiaban01.bpmn20.xml")
                .name("加班申请流程01").deploy();
        System.out.println(deployment.getId());
        System.out.println(deployment.getName());
    }

    //启动流程示例
    @Test
    public void startProcess01(){
        //设置任务人
        Map<String,Object> map=new HashMap<>();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("jiaban01", map);
        System.out.println(processInstance.getProcessDefinitionId());
        System.out.println(processInstance.getId());
    }

    //部署流程定义 listener
    @Test
    public void deployProcess02(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("process/jiaban02.bpmn20.xml")
                .name("加班申请流程02").deploy();
        System.out.println(deployment.getId());
        System.out.println(deployment.getName());
    }

    //启动流程示例
    @Test
    public void startProcess02(){
        //设置任务人
        Map<String,Object> map=new HashMap<>();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("jiaban02", map);
        System.out.println(processInstance.getProcessDefinitionId());
        System.out.println(processInstance.getId());
    }
}
