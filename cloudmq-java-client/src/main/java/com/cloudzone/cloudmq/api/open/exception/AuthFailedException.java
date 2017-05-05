package com.cloudzone.cloudmq.api.open.exception;

/**
 * @author yintongjiang
 * @params 认证异常类
 * @since 2017/3/23
 */
public class AuthFailedException extends RuntimeException {
    private static final long serialVersionUID = 5755256574640041014L;

    public AuthFailedException() {
    }

    public AuthFailedException(String message) {
        super(message);
    }

    public AuthFailedException(Throwable cause) {
        super(cause);
    }

    public AuthFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
