package com.atguigu.auth;


import com.atguigu.auth.mapper.SysRoleMapper;
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
public class TestDemo1 {

    @Autowired
    private SysRoleMapper mapper;

    @Test
    public void getAll(){
        List<SysRole> sysRoles = mapper.selectList(null);
        System.out.println(sysRoles);
    }

    @Test
    public void testInsert(){
        SysRole sysRole = new SysRole();
        sysRole.setRoleName("角色管理员");
        sysRole.setRoleCode("role");
        sysRole.setDescription("角色管理员");

        int result = mapper.insert(sysRole);
        System.out.println(result); //影响的行数
        System.out.println(sysRole); //id自动回填
    }

    @Test
    public void testDeleteById(){
        int result = mapper.deleteById(2L);
        System.out.println(result);
    }

    @Test
    public void testDeleteBatchIds() {
        int result = mapper.deleteBatchIds(Arrays.asList(8,9));
        System.out.println(result);
    }
}
