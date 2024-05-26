package com.atguigu.common.jwt;

import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import java.util.Date;

public class JwtHelper {

    //有效时间
    private static long tokenExpiration = 365 * 24 * 60 * 60 * 1000;
    //加密密钥
    private static String tokenSignKey = "123456";

    public static String createToken(Long userId, String username) {
        String token = Jwts.builder()
                .setSubject("AUTH-USER")//分类
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))//设置token有效时间
                .claim("userId", userId)//设置主体部分
                .claim("username", username)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)//设置签名
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    public static Long getUserId(String token) {
        try {
            if (StringUtils.isEmpty(token)) return null;

            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            Integer userId = (Integer) claims.get("userId");
            return userId.longValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String getUsername(String token) {
        try {
            if (StringUtils.isEmpty(token)) return "";

            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            return (String) claims.get("username");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String token = JwtHelper.createToken(1L, "admin");
        System.out.println(token);
        System.out.println(JwtHelper.getUserId(token));
        System.out.println(JwtHelper.getUsername(token));
    }
}
