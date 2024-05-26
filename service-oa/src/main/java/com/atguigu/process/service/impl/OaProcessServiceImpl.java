package com.atguigu.process.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.model.process.Process;
import com.atguigu.model.process.ProcessRecord;
import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.system.SysUser;
import com.atguigu.process.mapper.OaProcessMapper;
import com.atguigu.process.service.MessageService;
import com.atguigu.process.service.OaProcessRecordService;
import com.atguigu.process.service.OaProcessService;
import com.atguigu.process.service.OaProcessTemplateService;
import com.atguigu.security.custom.LoginUserInfoHelper;
import com.atguigu.vo.process.ApprovalVo;
import com.atguigu.vo.process.ProcessFormVo;
import com.atguigu.vo.process.ProcessQueryVo;
import com.atguigu.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.activiti.bpmn.model.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.zip.ZipInputStream;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2024-05-15
 */
@Service
public class OaProcessServiceImpl extends ServiceImpl<OaProcessMapper, Process> implements OaProcessService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private OaProcessRecordService processRecordService;

    @Autowired
    private OaProcessTemplateService processTemplateService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private MessageService messageService;

    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> pageModel=baseMapper.selectPage(pageParam,processQueryVo);
        return pageModel;
    }

    @Override
    public void deployByZip(String deployPath) {
        InputStream inputStream=
                this.getClass().getClassLoader().getResourceAsStream(deployPath);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        //部署
        Deployment deployment = repositoryService.createDeployment()
                .addZipInputStream(zipInputStream).deploy();
    }

    @Override
    public void startUp(ProcessFormVo processFormVo) {
        //根据用户id获取用户信息
        SysUser sysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        //根据审批模板ai查出模板信息
        ProcessTemplate processTemplate = processTemplateService.getById(processFormVo.getProcessTemplateId());
        //保存提交的审批信息到业务表
        Process process = new Process();
        BeanUtils.copyProperties(processFormVo,process);//复制值
        String workNo = System.currentTimeMillis() + "";
        process.setProcessCode(workNo);
        process.setUserId(LoginUserInfoHelper.getUserId());
        process.setFormValues(processFormVo.getFormValues());
        process.setTitle(sysUser.getName() + "发起" + processTemplate.getName() + "申请");
        process.setStatus(1);
        baseMapper.insert(process);

        //流程定义key
        String processDefinitionKey = processTemplate.getProcessDefinitionKey();
        //业务key processId
        String businessKey = String.valueOf(process.getId());
        //流程参数 form表单json数据 转换map集合
        String formValues = processFormVo.getFormValues();
        JSONObject jsonObject = JSON.parseObject(formValues);
        JSONObject formData = jsonObject.getJSONObject("formData");
        //遍历formData得到内容 封装map集合
        Map<String,Object> map=new HashMap<>();
        for(Map.Entry<String,Object> entry:formData.entrySet()){
            map.put(entry.getKey(),entry.getValue());
        }
        Map<String,Object> variables=new HashMap<>();
        variables.put("data",map);
        //启动流程示例
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, map);
        //查询下一个审批人
            //审批人可能有多个
        List<Task> tasks=this.getCurrentTaskList(processInstance.getId());
        List<String> nameList=new ArrayList<>();
        for (Task task:tasks){
            String assigneeName = task.getAssignee();
            SysUser user = sysUserService.getUserByUserName(assigneeName);
            String name = user.getName();
            nameList.add(name);
            //推送消息

        }

        process.setProcessInstanceId(processInstance.getId());//更新示例id
        process.setDescription("等待"+ StringUtils.join(nameList.toArray(),",") +"审批");
        //业务和流程关联
        baseMapper.updateById(process);
        processRecordService.record(process.getId(),1,"发起申请");
    }

    @Override
    public IPage<ProcessVo> findPending(Page<Process> pageParam) {
        //封装查询条件 根据当前登录的用户名称
        TaskQuery taskQuery = taskService.createTaskQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername())
                .orderByTaskCreateTime()
                .desc();
        //调用方法分页条件查询 返回list集合
        int begin = (int)((pageParam.getCurrent() - 1) * pageParam.getSize());
        int size = (int) (pageParam.getSize());
        List<Task> taskList = taskQuery.listPage(begin, size);//1开始位置 2每页记录数
        long totalCount = taskQuery.count();
        //封装返回list集合数据 到list<ProcessVo>
        List<ProcessVo> processVoList=new ArrayList<>();
        for(Task task:taskList){
            //从task获取流程实例id
            String processInstanceId = task.getProcessInstanceId();
            //根据流程示例id获取实例对象
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            //从流程实例对象获取业务key 就是processId
            String businessKey = processInstance.getBusinessKey();
            if(businessKey==null){
                continue;
            }
            //根据业务key获取Process对象
            long processId = Long.parseLong(businessKey);
            Process process = baseMapper.selectById(processId);
            //转成VO
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process,processVo);
            processVo.setTaskId(task.getId());
            //放到集合
            processVoList.add(processVo);
        }
        //封装返回IPage对象

        IPage<ProcessVo> page=new Page<ProcessVo>(pageParam.getCurrent(),pageParam.getSize(),totalCount);
        page.setRecords(processVoList);
        return page;
    }

    @Override
    public Map<String, Object> show(Long id) {
        //根据流程id获取流程信息Process
        Process process = baseMapper.selectById(id);
        //根据流程id获取流程记录信息
        LambdaQueryWrapper<ProcessRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessRecord::getProcessId,id);
        List<ProcessRecord> list = processRecordService.list(wrapper);
        //根据模板id查询模板信息
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        //判断当前用户是否能审批 能看到不一定能审批 不能重复审批
        boolean isApprove=false;
        String username = LoginUserInfoHelper.getUsername();
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());

        for (Task task:taskList){
            //判断任务审批人是否是当前用户
            if(task.getAssignee().equals(username)){
                isApprove=true;
                break;
            }
        }
        //查询数据封装到集合
        Map<String,Object> map=new HashMap<>();
        map.put("isApprove",isApprove);
        map.put("process", process);
        map.put("processRecordList", list);
        map.put("processTemplate", processTemplate);
        return map;
    }

    @Override
    public void approve(ApprovalVo approvalVo) {
        //从vo获取任务id 根据任务id获取流程变量
        String taskId = approvalVo.getTaskId();
        Map<String, Object> variables = taskService.getVariables(taskId);
        for (Map.Entry<String, Object> entry:variables.entrySet()){
            System.out.println(entry.getKey()+" "+entry.getValue());
        }
        //判断审批状态值
        if (approvalVo.getStatus()==1){
            //状态值=1 通过
            taskService.complete(taskId);
        }else {
            //=-1 驳回 流程结束
            this.endTask(taskId);
        }
        //记录审批相关过程信息
        String description=approvalVo.getStatus()==1?"通过":"驳回";
        processRecordService.record(approvalVo.getProcessId(),approvalVo.getStatus(),description);
        //查询下一个审批人 更新流程表process记录
        Process process = baseMapper.selectById(approvalVo.getProcessId());
        //查询任务
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if(!CollectionUtils.isEmpty(taskList)){
            List<String> assigneeList=new ArrayList<>();
            for (Task task:taskList){
                String assignee = task.getAssignee();
                SysUser sysUser = sysUserService.getUserByUserName(assignee);
                assigneeList.add(sysUser.getName());
                //公众号消息推送
            }

            process.setDescription("等待" + StringUtils.join(assigneeList.toArray(), ",") + "审批");
            process.setStatus(1);
        }else {
            if(approvalVo.getStatus().intValue() == 1) {
                process.setDescription("审批完成（同意）");
                process.setStatus(2);
            } else {
                process.setDescription("审批完成（拒绝）");
                process.setStatus(-1);
            }
        }
        baseMapper.updateById(process);
    }

    @Override
    public IPage<ProcessVo> findProcessed(Page<Process> pageParam) {
        //封装查询条件
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername()).finished().orderByTaskCreateTime().desc();
        //调用方法条件分页查询 返回list集合
        int begin=(int)((pageParam.getCurrent()-1)*pageParam.getSize());
        int size=(int)pageParam.getSize();
        List<HistoricTaskInstance> list = query.listPage(begin, size);
        long totalCount = query.count();
        //遍历返回list集合 封装list<ProcessVo>
        List<ProcessVo> processVoList=new ArrayList<>();
        for (HistoricTaskInstance item:list){
            //获取流程实例id
            String processInstanceId = item.getProcessInstanceId();
            //根据id查询获取process信息
            LambdaQueryWrapper<Process> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Process::getProcessInstanceId,processInstanceId);
            Process process = baseMapper.selectOne(wrapper);
            //转换
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process,processVo);
            processVoList.add(processVo);
        }

        //IPage封装分页查询所有数据
        IPage<ProcessVo> pageModel=new Page<>(pageParam.getCurrent(),pageParam.getSize(),totalCount);
        pageModel.setRecords(processVoList);
        return pageModel;
    }

    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam) {
        ProcessQueryVo processQueryVo = new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> page = baseMapper.selectPage(pageParam, processQueryVo);
        return page;
    }

    private void endTask(String taskId) {
        //根据任务id获取任务对象 task
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        //获取流程定义模型 bpmnmodel
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        //获取结束流向节点
        List<EndEvent> endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        if (CollectionUtils.isEmpty(endEventList)){
            return;
        }
        FlowNode endFlowNode = (FlowNode) endEventList.get(0);
        //当前流向节点
        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());
        //临时保存当前活动原始方向
        List originalSequenceFlowList = new ArrayList<>();
        originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
        //清理当前流动方向
        currentFlowNode.getOutgoingFlows().clear();
        //创建新流向
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlowId");
        newSequenceFlow.setSourceFlowElement(currentFlowNode);
        newSequenceFlow.setTargetFlowElement(endFlowNode);
        List newSequenceFlowList = new ArrayList<>();
        newSequenceFlowList.add(newSequenceFlow);
        //  当前节点指向新的方向
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);

        //  完成当前任务
        taskService.complete(task.getId());
    }

    private List<Task> getCurrentTaskList(String id) {
        List<Task> list = taskService.createTaskQuery().processDefinitionId(id).list();
        return list;
    }


}
