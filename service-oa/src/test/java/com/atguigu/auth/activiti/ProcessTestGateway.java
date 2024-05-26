package com.atguigu.auth.activiti;

import com.atguigu.Main;
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
public class ProcessTestGateway {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    //1 部署流程定义 启动流程示例
    @Test
    public void deployProcess(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("process/qingjia002.bpmn20.xml")
                .name("完整请假申请流程").deploy();
        System.out.println(deployment.getId());
        System.out.println(deployment.getName());

        //设置请假天数
        Map<String,Object> map=new HashMap<>();
        map.put("day","2");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("qingjia002",map);
        System.out.println(processInstance.getProcessDefinitionId());
        System.out.println(processInstance.getId());
    }






    //查询个人待办任务
    @Test
    public void findTaskList(){
        List<Task> list = taskService.createTaskQuery().taskAssignee("personnel").list();
        for (Task task :list) {
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    //办理个人任务
    @Test
    public void completTask() {
        Task task = taskService.createTaskQuery()
                .taskAssignee("demanager")  //要查询的负责人
                .singleResult();

        //完成任务
        taskService.complete(task.getId());
    }






}
