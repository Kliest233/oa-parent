package com.atguigu.config.exception;

import com.atguigu.common.result.ResultCodeEnum;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ClassName: GuiguException
 * Package: com.atguigu.config.exception
 * Description:
 *
 * @Author Klilest
 * @Create 2024/5/11 20:48
 * @Version 1.0
 */

@Data
public class GuiguException extends RuntimeException{
    private Integer code;
    private String msg;

    public GuiguException(Integer code,String msg){
        super(msg);
        this.code=code;
        this.msg=msg;
    }

    /**
     * 接收枚举类型对象
     * @param resultCodeEnum
     */
    public GuiguException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
        this.msg = resultCodeEnum.getMessage();
    }

    @Override
    public String toString() {
        return "GuliException{" +
                "code=" + code +
                ", message=" + this.getMessage() +
                '}';
    }

}
