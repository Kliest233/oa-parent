package com.atguigu.auth.service;

import com.atguigu.model.system.SysRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * ClassName: SysRoleService
 * Package: com.atguigu.auth.service
 * Description:
 *
 * @Author Klilest
 * @Create 2024/5/10 16:09
 * @Version 1.0
 */

public interface SysRoleService extends IService<SysRole> {

    Map<String, Object> findRoleDataByUserId(Long userId);

    void doAssign(AssginRoleVo assginRoleVo);
}
