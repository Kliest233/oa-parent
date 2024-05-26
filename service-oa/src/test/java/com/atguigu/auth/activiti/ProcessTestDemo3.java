package com.atguigu.auth.activiti;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class ProcessTestDemo3 {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    //1 部署流程定义 uel-value
    @Test
    public void deployProcess(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("process/jiaban04.bpmn20.xml")
                .name("加班申请流程04").deploy();
        System.out.println(deployment.getId());
        System.out.println(deployment.getName());
    }

    //启动流程示例
    @Test
    public void startProcess(){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("jiaban04");
        System.out.println(processInstance.getProcessDefinitionId());
        System.out.println(processInstance.getId());
    }

    //2 查询组任务
    @Test
    public void findGroupTaskList(){
        List<Task> list = taskService.createTaskQuery()
                .taskCandidateUser("tom01").list();
        for(Task task:list){
            System.out.println("----------------------------");
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    //3 拾取组任务
    @Test
    public void claimTask(){
        Task task = taskService.createTaskQuery()
                .taskCandidateUser("tom01").singleResult();
        if (task!=null){
            taskService.claim(task.getId(),"tom01");
            System.out.println("任务拾取成功");

        }
    }

    //4 查询个人待办任务
    @Test
    public void findTaskList(){
        List<Task> list = taskService.createTaskQuery().taskAssignee("tom01").list();
        for (Task task :list) {
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    //5 办理个人任务
    @Test
    public void completTask() {
        Task task = taskService.createTaskQuery()
                .taskAssignee("tom01")  //要查询的负责人
                .singleResult();//返回一条

        //完成任务
        taskService.complete(task.getId());
    }






}
