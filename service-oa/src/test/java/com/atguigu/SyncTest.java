package com.atguigu;

import com.atguigu.wechat.service.MenuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SyncTest {

    @Autowired
    private MenuService menuService;

    @Test
    public void test(){
        menuService.syncMenu();
    }

}
