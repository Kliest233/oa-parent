package com.atguigu.common.result;

import lombok.Getter;

/**
 * ClassName: ResultCodeEnum
 * Package: com.atguigu.common.result
 * Description:
 *
 * @Author Klilest
 * @Create 2024/5/10 16:25
 * @Version 1.0
 */
@Getter //生成getter
public enum ResultCodeEnum {

    SUCCESS(200,"成功"),
    FAIL(201, "失败"),
    LOGIN_ERROR(205,"认证失败"),
    SERVICE_ERROR(2012, "服务异常"),
    DATA_ERROR(204, "数据异常"),

    LOGIN_AUTH(208, "未登陆"),
    PERMISSION(209, "没有权限")
    ;

    private Integer code;
    private String message;

    private ResultCodeEnum(Integer code,String message){
        this.code=code;
        this.message=message;
    }
}
