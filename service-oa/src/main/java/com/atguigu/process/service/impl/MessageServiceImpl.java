package com.atguigu.process.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.model.process.Process;
import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.system.SysUser;
import com.atguigu.process.service.MessageService;
import com.atguigu.process.service.OaProcessService;
import com.atguigu.process.service.OaProcessTemplateService;
import com.atguigu.security.custom.LoginUserInfoHelper;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.checkerframework.checker.units.qual.A;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private OaProcessService processService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private OaProcessTemplateService processTemplateService;

    @Autowired
    private WxMpService wxMpService;

    @Override
    public void pushPendingMessage(Long processId, Long userId, String taskId) {
        //根据id查询流程信息
        Process process = processService.getById(processId);
        //根据userId查询要推送人信息
        SysUser sysUser = sysUserService.getById(userId);
        //查询模板信息
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        //查询提交审批人信息
        SysUser submitSysUser = sysUserService.getById(process.getUserId());
        //获取要给的消息人的openid
        String openId = sysUser.getOpenId();
        if (StringUtils.isEmpty(openId)){
            openId="omwf25izKON9dktgoy0dogqvnGhk";//测试所以写死默认值
        }
        //设置消息发送信息
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(openId) //给谁发消息
                .templateId("k-57cDEbkXrDCXglBCJhI_urwgPPEfHIbJWM4X7wYvg")
                .url("http://oaatguigu1234.v7.idcfengye.com/#/show/").build();//跳转的地址

        //设置模板参数值
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuffer content = new StringBuffer();
        for (Map.Entry entry : formShowData.entrySet()) {
            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }
        templateMessage.addData(new WxMpTemplateData("first", submitSysUser.getName()+"提交了"+processTemplate.getName()+"审批申请，请注意查看。", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        templateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));

        //发送
        try {
            String msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void pushProcessedMessage(Long processId, Long userId, Integer status) {
        Process process = processService.getById(processId);
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        SysUser sysUser = sysUserService.getById(userId);
        SysUser currentSysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        String openid = sysUser.getOpenId();
        if(StringUtils.isEmpty(openid)) {
            openid = "omwf25izKON9dktgoy0dogqvnGhk";
        }
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(openid)//要推送的用户openid
                .templateId("KSYpQAOYLdulp2hktbYcEYomFRnONgDlm9zZABsBzpw")//模板id
                .url("http://oa.atguigu.cn/#/show/"+processId+"/0")//点击模板消息要访问的网址
                .build();
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuffer content = new StringBuffer();
        for (Map.Entry entry : formShowData.entrySet()) {
            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }
        templateMessage.addData(new WxMpTemplateData("first", "你发起的"+processTemplate.getName()+"审批申请已经被处理了，请注意查看。", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword3", currentSysUser.getName(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword4", status == 1 ? "审批通过" : "审批拒绝", status == 1 ? "#009966" : "#FF0033"));
        templateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));
        try {
            String msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }

    }

}
