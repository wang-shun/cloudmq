package com.cloudzone.cloudmq.api.open.exception;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class GomeClientException extends RuntimeException {
    private static final long serialVersionUID = 5755356574640041094L;


    public GomeClientException() {
    }


    public GomeClientException(String message) {
        super(message);
    }


    public GomeClientException(Throwable cause) {
        super(cause);
    }


    public GomeClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
