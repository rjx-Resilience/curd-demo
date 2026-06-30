package org.example.curddemo.common.exception;

public class BusinessException extends RuntimeException{
    private final String errorCode;
    private final String userMessage;

    public BusinessException(String errorCode,String userMessage){
        super(userMessage);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    public BusinessException(ResponseEnum responseEnum){
        super(responseEnum.getErrorMsg());
        this.errorCode = responseEnum.getErrorCode();
        this.userMessage = responseEnum.getErrorMsg();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
