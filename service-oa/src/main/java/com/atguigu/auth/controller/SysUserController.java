package com.atguigu.auth.controller;


import com.atguigu.auth.service.SysUserService;
import com.atguigu.common.result.Result;
import com.atguigu.common.utils.MD5;
import com.atguigu.model.system.SysUser;
import com.atguigu.vo.system.SysUserQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2024-05-12
 */
@Api(tags = "用户管理接口")
@RestController
@RequestMapping("/admin/system/sysUser")
@CrossOrigin
public class SysUserController {

    @Autowired
    private SysUserService service;

    @ApiOperation("更新状态")
    @GetMapping("updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable Long id,@PathVariable Integer status){
        service.updateStatus(id,status);
        return Result.ok();
    }


    @ApiOperation("用户条件分页查询")
    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable Long page, @PathVariable Long limit, SysUserQueryVo userQueryVo){
        //创建配置对象
        Page<SysUser> pageParam=new Page<>(page,limit);
        //封装条件 判断条件值不为空
        LambdaQueryWrapper<SysUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        String userName = userQueryVo.getKeyword();
        String createTimeBegin = userQueryVo.getCreateTimeBegin();
        String createTimeEnd = userQueryVo.getCreateTimeEnd();
        if (!StringUtils.isEmpty(userName)){
            lambdaQueryWrapper.like(SysUser::getUsername,userName);
        }
        if (!StringUtils.isEmpty(createTimeBegin)){
            lambdaQueryWrapper.ge(SysUser::getCreateTime,createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)){
            lambdaQueryWrapper.le(SysUser::getCreateTime,createTimeEnd);
        }
        //调用mp方法分页查询
        IPage<SysUser> pageModel = service.page(pageParam,lambdaQueryWrapper);

        return Result.ok(pageModel);
    }


    @ApiOperation(value = "获取用户")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        SysUser user = service.getById(id);
        return Result.ok(user);
    }

    @ApiOperation(value = "保存用户")
    @PostMapping("save")
    public Result save(@RequestBody SysUser user) {
        //对密码加密
        String password = user.getPassword();
        String passwordMD5 = MD5.encrypt(password);
        user.setPassword(passwordMD5);
        service.save(user);
        return Result.ok();
    }

    @ApiOperation(value = "更新用户")
    @PutMapping("update")
    public Result updateById(@RequestBody SysUser user) {
        service.updateById(user);
        return Result.ok();
    }

    @ApiOperation(value = "删除用户")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }

}

