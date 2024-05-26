package com.atguigu.auth.controller;

import com.atguigu.auth.service.SysRoleService;
import com.atguigu.common.result.Result;
import com.atguigu.model.system.SysRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.atguigu.vo.system.SysRoleQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ClassName: SysRoleController
 * Package: com.atguigu.auth.controller
 * Description:
 *
 * @Author Klilest
 * @Create 2024/5/10 16:17
 * @Version 1.0
 */
@RestController
@RequestMapping("/admin/system/sysRole")
@Api(tags = "角色管理")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    //查询所有角色和当前用户所属角色
    @ApiOperation("获取角色")
    @GetMapping("/toAssign/{userId}")
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    public Result toAssign(@PathVariable Long userId){
        Map<String,Object> map = sysRoleService.findRoleDataByUserId(userId);
        return Result.ok(map);
    }

    @ApiOperation("为用户分配角色")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestBody AssginRoleVo assginRoleVo){
        sysRoleService.doAssign(assginRoleVo);
        return Result.ok();
    }


    @ApiOperation("查看所有角色")
    @GetMapping("/findAll")
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    public Result findAll(){
        List<SysRole> list = sysRoleService.list();
        return Result.ok(list);
    }

    @ApiOperation("条件分页查询用户")
    @GetMapping("{page}/{limit}")
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    public Result pageQueryRole(@PathVariable Long page, @PathVariable Long limit, SysRoleQueryVo sysRoleQueryVo){
        //page当前页 limit每页记录数 sysRoleQueryVo条件对象

        //1创建一个配置对象 传递分页相关参数
        Page<SysRole> pageParam = new Page<>(page, limit);
        // 2封装条件 判断条件是否为空 不为空进行封装
        LambdaQueryWrapper<SysRole> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        String roleName = sysRoleQueryVo.getRoleName();
        if (!StringUtils.isEmpty(roleName)){
            lambdaQueryWrapper.like(SysRole::getRoleName,roleName);
        }
        // 3调用方法实现
        IPage<SysRole> pageModel = sysRoleService.page(pageParam,lambdaQueryWrapper);
        return Result.ok(pageModel);
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.add')")
    @ApiOperation("添加角色")
    @PostMapping("save")
    public Result save(@RequestBody SysRole sysRole){
        boolean save = sysRoleService.save(sysRole);
        if (save){
            return Result.ok();
        }
        return Result.fail();
    }


    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("修改角色-根据id查询")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        SysRole role = sysRoleService.getById(id);
        return Result.ok(role);
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.update')")
    @ApiOperation("修改角色")
    @PostMapping("update")
    public Result update(@RequestBody SysRole sysRole){
        boolean isSuccess = sysRoleService.updateById(sysRole);
        if (isSuccess){
            return Result.ok();
        }
        return Result.fail();
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("根据id删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        boolean isSuccess = sysRoleService.removeById(id);
        if (isSuccess){
            return Result.ok();
        }
        return Result.fail();
    }

    @ApiOperation("批量删除")
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){
        boolean isSuccess = sysRoleService.removeByIds(idList);
        if (isSuccess){
            return Result.ok();
        }
        return Result.fail();
    }
}
