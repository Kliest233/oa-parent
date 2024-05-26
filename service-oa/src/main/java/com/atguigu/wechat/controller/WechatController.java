package com.atguigu.wechat.controller;

import com.atguigu.auth.service.SysUserService;
import com.atguigu.common.jwt.JwtHelper;
import com.atguigu.common.result.Result;
import com.atguigu.model.system.SysUser;
import com.atguigu.security.custom.LoginUserInfoHelper;
import com.atguigu.vo.wechat.BindPhoneVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Controller
@RequestMapping("/admin/wechat")
@Slf4j
@CrossOrigin
public class WechatController {
    @Resource
    private SysUserService sysUserService;

    @Autowired
    private WxMpService wxMpService;

    @Value("${wechat.userInfoUrl}")
    private String userInfoUrl;

    @GetMapping("/authorize")
    public String authorize(@RequestParam("returnUrl") String returnUrl, HttpServletRequest request){
        //参数1 授权路径 在哪个路径获取微信信息
        // 2 固定值，授权类型
        // 3 授权成功后跳转的路径
        String redirectUrl = null;
        try {
            redirectUrl = wxMpService.getOAuth2Service()
                    .buildAuthorizationUrl(
                            userInfoUrl,
                            WxConsts.OAuth2Scope.SNSAPI_USERINFO,
                            URLEncoder.encode(returnUrl.replace("guiguoa", "#"),"utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return "redirect"+redirectUrl;
    }

    @GetMapping("/userInfo")
    public String userInfo(@RequestParam("code") String code,//临时票据
                           @RequestParam("state") String returnUrl) throws Exception{
        //获取accessToken
        WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
        //使用accessToken获取openId
        String openId = accessToken.getOpenId();
        //获取微信用户信息
        WxOAuth2UserInfo wxMpUser= wxMpService.getOAuth2Service().getUserInfo(accessToken, null);
        //根据openid查用户表
        SysUser sysUser =
                sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getOpenId, openId));
        String token="";
        if (sysUser!=null){
            token= JwtHelper.createToken(sysUser.getId(),sysUser.getUsername());
        }
        if(returnUrl.indexOf("?") == -1) {
            return "redirect:" + returnUrl + "?token=" + token + "&openId=" + openId;
        } else {
            return "redirect:" + returnUrl + "&token=" + token + "&openId=" + openId;
        }
    }

    @ApiOperation(value = "微信账号绑定手机")
    @PostMapping("bindPhone")
    @ResponseBody
    public Result bindPhone(@RequestBody BindPhoneVo bindPhoneVo){
        //根据手机查数据库
        SysUser sysUser = sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, bindPhoneVo.getPhone()));
        if(null != sysUser){
            //存在 更新记录 openid
            sysUser.setOpenId(bindPhoneVo.getOpenId());
            sysUserService.updateById(sysUser);
            String token = JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());
            return Result.ok(token);
        }else {
            return Result.fail("手机号码不存在，绑定失败");
        }

    }
}
