package com.atguigu.auth.service.impl;


import com.atguigu.auth.mapper.SysMenuMapper;
import com.atguigu.auth.service.SysMenuService;
import com.atguigu.auth.service.SysRoleMenuService;
import com.atguigu.auth.utils.MenuHelper;
import com.atguigu.config.exception.GuiguException;
import com.atguigu.model.system.SysMenu;
import com.atguigu.model.system.SysRoleMenu;
import com.atguigu.vo.system.AssginMenuVo;
import com.atguigu.vo.system.MetaVo;
import com.atguigu.vo.system.RouterVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2024-05-12
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Override
    public List<SysMenu> findNodes() {
        List<SysMenu> sysMenuList = baseMapper.selectList(null);
        //构建树形结构
        List<SysMenu> resultList=MenuHelper.buildTree(sysMenuList);
        return resultList;
    }

    @Override
    public void removeMenuById(Long id) {
        //判断是否有子菜单
        LambdaQueryWrapper<SysMenu> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysMenu::getParentId,id);
        Integer count = baseMapper.selectCount(lambdaQueryWrapper);
        if(count>0){
            throw new GuiguException(201,"菜单不能删除");
        }
        baseMapper.deleteById(id);
    }

    @Override
    public List<SysMenu> findMenuByRoleId(Long roleId) {
        //查询所有菜单 添加条件 status=1可用
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getStatus,1);
        List<SysMenu> allSysMenuList = baseMapper.selectList(wrapper);

        //查角色菜单关系表 角色id对应的所有菜单
        LambdaQueryWrapper<SysRoleMenu> wrapperSysRoleMenu = new LambdaQueryWrapper<>();
        wrapperSysRoleMenu.eq(SysRoleMenu::getRoleId,roleId);
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuService.list(wrapperSysRoleMenu);

        List<Long> menuIdList =
                sysRoleMenuList.stream().map(c -> c.getMenuId()).collect(Collectors.toList());

        allSysMenuList.stream().forEach(item->{
            if(menuIdList.contains(item.getId())){
                item.setSelect(true);
            }else {
                item.setSelect(false);
            }
        });

        List<SysMenu> sysMenus = MenuHelper.buildTree(allSysMenuList);
        return sysMenus;
    }
    @Transactional
    @Override
    public void doAssign(AssginMenuVo assginMenuVo) {
        //根据角色id删除已分配菜单数据
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId,assginMenuVo.getRoleId());
        sysRoleMenuService.remove(wrapper);
        //从参数里获得角色新分配菜单id列表 把每个id数据添加菜单角色表
        List<Long> menuIdList = assginMenuVo.getMenuIdList();
        for (Long menuId:menuIdList){
            if(StringUtils.isEmpty(menuId)){
                continue;
            }
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setRoleId(assginMenuVo.getRoleId());
            sysRoleMenu.setMenuId(menuId);
            sysRoleMenuService.save(sysRoleMenu);
        }
    }

    @Override
    public List<RouterVo> findUserMenuListByUserId(Long userId) {
        List<SysMenu> sysMenuList =null;
        //判断当前用户是否是管理员 userId=1
        if(userId==1){
            //是管理 查询所有菜单列表
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus,1).orderByAsc(SysMenu::getSortValue);
            sysMenuList = baseMapper.selectList(wrapper);
        }else {
            //不是 根据id查可操作菜单列表
            //多表关联查询
            sysMenuList=baseMapper.findMenuListByUserId(userId);
        }

        //把查询出的数据列表构建成框架要求的路由数据结构
        List<SysMenu> treeList = MenuHelper.buildTree(sysMenuList);//构建树形结构
        List<RouterVo> routerVoList = this.buildRouter(treeList);
        return routerVoList;
    }

    private List<RouterVo> buildRouter(List<SysMenu> treeList) {
        //创建list存储最终数据
        List<RouterVo> routers=new ArrayList<>();
        //遍历
        for(SysMenu menu:treeList){
            RouterVo router = new RouterVo();
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(menu));
            router.setComponent(menu.getComponent());
            router.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
            List<SysMenu> children = menu.getChildren();
            if(menu.getType().intValue() == 1) {
                List<SysMenu> hiddenMenuList =
                        children.stream().filter(item -> !StringUtils.isEmpty(item.getComponent()))
                                .collect(Collectors.toList());
                for (SysMenu hiddenMenu : hiddenMenuList) {
                    RouterVo hiddenRouter = new RouterVo();
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    routers.add(hiddenRouter);
                }
            } else {
                if (!CollectionUtils.isEmpty(children)) {
                    if(children.size() > 0) {
                        router.setAlwaysShow(true);
                    }
                    router.setChildren(buildRouter(children));
                }
            }
            routers.add(router);
        }
        return routers;

    }

    @Override
    public List<String> findUserPersByUserId(Long userId) {
        List<SysMenu> sysMenuList =null;
        //判断当前用户是否是管理员 userId=1
        if(userId==1){
            //是管理 查询所有按钮列表
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus,1);
            sysMenuList = baseMapper.selectList(wrapper);
        }else {
            //不是 根据id查可操作按钮列表
            //多表关联查询
            sysMenuList=baseMapper.findMenuListByUserId(userId);
        }

        //获取可操作按钮集合 返回
        List<String> permsList = sysMenuList.stream()
                .filter(item -> item.getType() == 2)
                .map(item -> item.getPerms())
                .collect(Collectors.toList());

        return permsList;
    }

    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        if(menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }
}
