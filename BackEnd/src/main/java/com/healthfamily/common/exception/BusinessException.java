/**
 * BusinessException异常类
 * <p>
 * 用于表达业务域内的错误语义，并配合全局异常处理器统一返回。
 * </p>
 */
package com.healthfamily.common.exception;

public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

