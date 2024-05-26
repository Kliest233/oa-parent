package com.atguigu.wechat.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.model.wechat.Menu;
import com.atguigu.vo.wechat.MenuVo;
import com.atguigu.wechat.mapper.MenuMapper;
import com.atguigu.wechat.service.MenuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2024-05-15
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Autowired
    private WxMpService wxMpService;

    @Override
    public List<MenuVo> findMenuInfo() {
        List<MenuVo> list=new ArrayList<>();
        //查询出所有菜单list集合
        List<Menu> menuList = baseMapper.selectList(null);
        //查询所有一级菜单 parent_id=0 返回list集合
        List<Menu> oneMenuList = menuList.stream().filter(menu -> menu.getParentId().longValue() == 0)
                .collect(Collectors.toList());
        //遍历一级菜单list 得到每个一级菜单
        for (Menu oneMenu : oneMenuList) {
            //一级菜单转化成vo
            MenuVo menuVo = new MenuVo();
            BeanUtils.copyProperties(oneMenu,menuVo);
            //list.add(menuVo);
            //获取每个一级菜单里面所有二级菜单id和parent_id比较
            List<Menu> twoMenuList = menuList.stream()
                    .filter(menu -> menu.getParentId() == oneMenu.getId()).collect(Collectors.toList());
            //把一级菜单里所有2级菜单获取到 封装一级菜单children集合里
            List<MenuVo> children=new ArrayList<>();
            for(Menu twoMenu:twoMenuList){
                MenuVo twoMenuVo = new MenuVo();
                BeanUtils.copyProperties(twoMenu,twoMenuVo);
                children.add(twoMenuVo);
            }
            menuVo.setChildren(children);
            //menu->menuvo
            list.add(menuVo);
        }
        return list;
    }

    @Override
    public void syncMenu() {
        List<MenuVo> menuVoList = this.findMenuInfo();
        //菜单
        JSONArray buttonList = new JSONArray();
        for(MenuVo oneMenuVo : menuVoList) {
            JSONObject one = new JSONObject();
            one.put("name", oneMenuVo.getName());
            if(CollectionUtils.isEmpty(oneMenuVo.getChildren())) {
                one.put("type", oneMenuVo.getType());
                one.put("url", "http://oa.atguigu.cn/#"+oneMenuVo.getUrl());
            } else {
                JSONArray subButton = new JSONArray();
                for(MenuVo twoMenuVo : oneMenuVo.getChildren()) {
                    JSONObject view = new JSONObject();
                    view.put("type", twoMenuVo.getType());
                    if(twoMenuVo.getType().equals("view")) {
                        view.put("name", twoMenuVo.getName());
                        //H5页面地址
                        view.put("url", "http://oa.atguigu.cn#"+twoMenuVo.getUrl());
                    } else {
                        view.put("name", twoMenuVo.getName());
                        view.put("key", twoMenuVo.getMeunKey());
                    }
                    subButton.add(view);
                }
                one.put("sub_button", subButton);
            }
            buttonList.add(one);
        }
        //菜单
        JSONObject button = new JSONObject();
        button.put("button", buttonList);
        try {
            wxMpService.getMenuService().menuCreate(button.toJSONString());
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }
}
