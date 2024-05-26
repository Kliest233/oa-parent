package com.atguigu.auth.activiti;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest
public class ProcessTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    //全部流程挂起
    @Test
    public void suspendProcessInstance(){
        //获取流程定义的对象
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("请假").singleResult();
        //判断状态
        if(processDefinition.isSuspended()){
            //挂起状态就激活
            //id 是否激活 时间点
            repositoryService.activateProcessDefinitionById(processDefinition.getId(),true,null);
        }else {
            //激活状态就挂起
            repositoryService.suspendProcessDefinitionById(processDefinition.getId(),true,null);
        }

    }


    //创建流程实例 指定BusinessKey
    @Test
    public void startUpProcessAddBusinessKey(){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("请假", "1001");

        System.out.println(processInstance.getBusinessKey());
    }

    @Test
    public void findCompletedTask(){
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee("zhangsan").finished().list();

        for (HistoricTaskInstance historicTaskInstance : list) {
            System.out.println("流程实例id：" + historicTaskInstance.getProcessInstanceId());
            System.out.println("任务id：" + historicTaskInstance.getId());
            System.out.println("任务负责人：" + historicTaskInstance.getAssignee());
            System.out.println("任务名称：" + historicTaskInstance.getName());
        }
    }

    @Test
    public void completeTask(){
        //查询并返回一条需处理任务
        Task task = taskService.createTaskQuery().taskAssignee("zhangsan")
                .singleResult();
        //完成任务 参数是任务id
        taskService.complete(task.getId());

    }

    @Test
    public void findTaskList(){
        List<Task> list = taskService.createTaskQuery().taskAssignee("zhangsan").list();
        for (Task task :list) {
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    @Test
    public void startProcess(){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("请假");
        System.out.println(processInstance.getProcessDefinitionId());
        System.out.println(processInstance.getId());
        System.out.println(processInstance.getActivityId());
    }


    //请假申请流程 单个文件部署
    @Test
    public void deployProcess(){
        Deployment deploy = repositoryService.createDeployment().addClasspathResource("process/qingjia.bpmn20.xml")
                //.addZipInputStream("") 使用压缩包的api
                .addClasspathResource("process/qingjia.png")
                .name("请假申请流程")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());
    }

}
