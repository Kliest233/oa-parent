package com.atguigu.process.controller;


import com.atguigu.common.result.Result;
import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.process.service.OaProcessTemplateService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 审批模板 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2024-05-14
 */
@RestController
@RequestMapping("/admin/process/processTemplate")
public class OaProcessTemplateController {

    @Autowired
    private OaProcessTemplateService processTemplateService;

    @ApiOperation("上传流程定义")
    @PostMapping("/uploadProcessDefinition")
    public Result uploadProcessDefinition(MultipartFile file) throws FileNotFoundException {
        //获取classes目录位置
        String path = new File(ResourceUtils.getURL("classpath:").getPath()).getAbsolutePath();
        //设置上传文件夹
        File tempFile = new File(path + "/processes/");
        if (!tempFile.exists()){
            tempFile.mkdirs();
        }
        //创建空文件 实现文件写入
        String filename = file.getOriginalFilename();
        File zipFile = new File(path + "/processes/" + filename);
        //保存文件
        try {
            file.transferTo(zipFile);
        } catch (IOException e) {
            return Result.fail();
        }

        Map<String,Object> map=new HashMap<>();
        map.put("processDefinitionPath","processes/"+filename);
        map.put("processDefinitionKey",filename.substring(0,filename.lastIndexOf(".")));
        return Result.ok(map);

    }

    //@PreAuthorize("hasAuthority('bnt.processTemplate.publish')")
    @ApiOperation(value = "发布")
    @GetMapping("/publish/{id}")
    public Result publish(@PathVariable Long id) {
        processTemplateService.publish(id);
        return Result.ok();
    }

    @ApiOperation("获取分页审批模板数据")
    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable Long page,@PathVariable Long limit){
        Page<ProcessTemplate> pageParam = new Page<>();
        //分页查询审批模板 把审批类型对应名称查询
        IPage<ProcessTemplate> pageModel=processTemplateService.selectPageProcessTemplate(pageParam);

        return Result.ok(pageModel);
    }

    //@PreAuthorize("hasAuthority('bnt.processTemplate.list')")
    @ApiOperation(value = "获取")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        ProcessTemplate processTemplate = processTemplateService.getById(id);
        return Result.ok(processTemplate);
    }

    //@PreAuthorize("hasAuthority('bnt.processTemplate.templateSet')")
    @ApiOperation(value = "新增")
    @PostMapping("save")
    public Result save(@RequestBody ProcessTemplate processTemplate) {
        processTemplateService.save(processTemplate);
        return Result.ok();
    }

    //@PreAuthorize("hasAuthority('bnt.processTemplate.templateSet')")
    @ApiOperation(value = "修改")
    @PutMapping("update")
    public Result updateById(@RequestBody ProcessTemplate processTemplate) {
        processTemplateService.updateById(processTemplate);
        return Result.ok();
    }

    //@PreAuthorize("hasAuthority('bnt.processTemplate.remove')")
    @ApiOperation(value = "删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        processTemplateService.removeById(id);
        return Result.ok();
    }
}

