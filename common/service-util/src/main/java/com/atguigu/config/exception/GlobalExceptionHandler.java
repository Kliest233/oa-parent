package com.atguigu.config.exception;


import com.atguigu.common.result.Result;
import com.atguigu.common.result.ResultCodeEnum;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;




/**
 * ClassName: GlobalExceptionHandler
 * Package: com.atguigu.config.exception
 * Description:
 *
 * @Author Klilest
 * @Create 2024/5/11 20:39
 * @Version 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    //全局异常处理执行的方法
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail().message("执行全局异常处理");
    }

    //特定异常处理
    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public Result error(ArithmeticException e){
        e.printStackTrace();
        return Result.fail().message("执行特定异常处理");
    }

    //特定异常处理
    @ExceptionHandler(GuiguException.class)
    @ResponseBody
    public Result error(GuiguException e){
        e.printStackTrace();
        return Result.fail().code(e.getCode()).message(e.getMsg());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public Result error(AccessDeniedException e) throws AccessDeniedException {
        return Result.fail().code(206).message("没有操作权限");
    }
}
