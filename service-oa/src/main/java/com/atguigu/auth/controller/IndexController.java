package com.atguigu.auth.controller;

import com.atguigu.auth.service.SysMenuService;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.common.jwt.JwtHelper;
import com.atguigu.common.result.Result;
import com.atguigu.common.utils.MD5;
import com.atguigu.config.exception.GuiguException;
import com.atguigu.model.system.SysUser;
import com.atguigu.vo.system.LoginVo;
import com.atguigu.vo.system.RouterVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName: IndexController
 * Package: com.atguigu.auth.controller
 * Description:
 *
 * @Author Klilest
 * @Create 2024/5/11 21:07
 * @Version 1.0
 */

@Api(tags = "后台登录管理")
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysMenuService sysMenuService;

    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo){
        //Map<String,Object> map=new HashMap<>();
        //map.put("token","admin-token");
        //return Result.ok();

        //获取输入的用户名和密码
        String username = loginVo.getUsername();
        //根据用户名查数据库
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername,username);
        SysUser sysUser = sysUserService.getOne(wrapper);
        //信息是否存在
        if (sysUser==null){
            throw new GuiguException(201,"用户不存在");
        }
        //判断密码
        String passwordDB = sysUser.getPassword();
        String password = MD5.encrypt(loginVo.getPassword());
        if (!password.equals(passwordDB)){
            throw new GuiguException(201,"密码错误");
        }

        //判断用户是否被禁用
        if(sysUser.getStatus().intValue()==0){
            throw new GuiguException(201,"用户已被禁用");
        }
        //使用jwt根据用户id和用户名称生成token串
        String token = JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());
        //返回
        Map<String,Object> map=new HashMap<>();
        map.put("token",token);
        return Result.ok(map);
    }

    @GetMapping("info")
    public Result info(HttpServletRequest request) {
        //从请求头获取token
        String token = request.getHeader("header");
        //从token字符串取出用户id
        Long userId = JwtHelper.getUserId(token);
        //根据id查数据库
        SysUser sysUser = sysUserService.getById(userId);
        //根据id获取用户可以操作的菜单列表 查询数据库动态构建路由结构进行显示
        List<RouterVo> routerList=sysMenuService.findUserMenuListByUserId(userId);
        //根据id获取用户可操作按钮列表
        List<String> permsList=sysMenuService.findUserPersByUserId(userId);
        //返回数据
        Map<String, Object> map = new HashMap<>();
        map.put("roles",sysUser.getRoleList());
        map.put("name",sysUser.getName());
        map.put("avatar","https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
        //返回用户可以操作的菜单
        map.put("routers",routerList);
        //返回用户可以操作的按钮
        map.put("buttons",permsList);
        return Result.ok(map);
    }
    /**
     * 退出
     * @return
     */
    @PostMapping("logout")
    public Result logout(){
        return Result.ok();
    }

}
