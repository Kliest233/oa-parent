package com.atguigu.auth.service.impl;

import com.atguigu.auth.service.SysMenuService;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.model.system.SysUser;
import com.atguigu.security.custom.CustomUser;
import com.atguigu.security.custom.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysMenuService sysMenuService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser=sysUserService.getUserByUserName(username);
        if(null == sysUser) {
            throw new UsernameNotFoundException("用户名不存在！");
        }

        if(sysUser.getStatus().intValue() == 0) {
            throw new RuntimeException("账号已停用");
        }

        //根据id查询用户权限
        List<String> persList = sysMenuService.findUserPersByUserId(sysUser.getId());
        //用于封装最后数据
        List<SimpleGrantedAuthority> authorityList=new ArrayList<>();
        //查询list集合遍历
        for(String pers:persList){
            authorityList.add(new SimpleGrantedAuthority(pers.trim()));
        }

        return new CustomUser(sysUser, authorityList);
    }
}
