package com.lyj.aicode.exception;

import com.lyj.aicode.common.ErrorCode;

/**
 * 自定义异常类
 */
public class BusinessException extends RuntimeException {

    //1. 定义错误码
    private final int code;

    //2. 构造器
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    //3. 自定义构造器
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    //4. get方法
    public int getCode() {
        return code;
    }
}

