package com.atguigu.auth;


import com.atguigu.auth.mapper.SysRoleMapper;
import com.atguigu.auth.service.SysRoleService;
import com.atguigu.model.system.SysRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * ClassName: TestDemo1
 * Package: com.atguigu.auth
 * Description:
 *
 * @Author Klilest
 * @Create 2024/5/10 15:13
 * @Version 1.0
 */


@SpringBootTest
public class TestDemo2 {

    @Autowired
    private SysRoleService sysRoleService;

    @Test
    public void getAll(){
        List<SysRole> list = sysRoleService.list();
        System.out.println(list);
    }


}
