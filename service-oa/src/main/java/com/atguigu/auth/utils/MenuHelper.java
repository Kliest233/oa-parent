package com.atguigu.auth.utils;

import com.atguigu.model.system.SysMenu;

import java.util.ArrayList;
import java.util.List;

public class MenuHelper {

    public static List<SysMenu> buildTree(List<SysMenu> sysMenuList) {
        //使用递归方法建菜单
        //创建list用于最终数据
        List<SysMenu> tree=new ArrayList<>();
        //把所有菜单数据进行遍历
        for (SysMenu sysMenu:sysMenuList){
            //递归入口进入 parentId==0
            if (sysMenu.getParentId().longValue()==0){
                tree.add(getChildren(sysMenu,sysMenuList));
            }
        }
        return tree;
    }

    private static SysMenu getChildren(SysMenu sysMenu, List<SysMenu> sysMenuList) {
        sysMenu.setChildren(new ArrayList<SysMenu>());
        //遍历所有菜单数据 判断id和parentId
        for (SysMenu item:sysMenuList){
            if(sysMenu.getId().longValue()==item.getParentId().longValue()){
                if(sysMenu.getChildren()==null){
                    sysMenu.setChildren(new ArrayList<>());
                }
                sysMenu.getChildren().add(getChildren(item,sysMenuList));
            }
        }
        return sysMenu;
    }
}
