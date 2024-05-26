package com.atguigu.process.service.impl;


import com.atguigu.model.process.Process;
import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.process.ProcessType;
import com.atguigu.process.mapper.OaProcessTypeMapper;
import com.atguigu.process.service.OaProcessTemplateService;
import com.atguigu.process.service.OaProcessTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2024-05-14
 */
@Service
public class OaProcessTypeServiceImpl extends ServiceImpl<OaProcessTypeMapper, ProcessType> implements OaProcessTypeService {

    @Autowired
    private OaProcessTemplateService processTemplateService;

    @Override
    public List<ProcessType> findProcessType() {
        //查询所有审批分类
        List<ProcessType> processTypeList = baseMapper.selectList(null);
        //遍历返回所有审批分类list集合
        for(ProcessType type:processTypeList){
            //得到每个审批分类 根据审批分类id查询对应审批模板
            Long id = type.getId();
            LambdaQueryWrapper<ProcessTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProcessTemplate::getProcessTypeId,id);
            List<ProcessTemplate> list = processTemplateService.list(wrapper);
            //根据审批分类id查询对应审批模板数据 封装到每个审批分类对象
            type.setProcessTemplateList(list);
        }

        return processTypeList;

    }
}
