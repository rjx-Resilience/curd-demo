package org.example.curddemo.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter

public enum ResponseEnum {
    EMAIL_DUPLICATE("B1001","该邮箱已经被注册"),
    PHONE_DUPLICATE("B1002","该手机号已经被注册"),
    STUDENT_NO_DUPLICATE("B1003","该学号已经存在"),
    STUDENT_NOT_FOUND("B1004","学生不存在");


    private final String errorCode;
    private final String errorMsg;

    ResponseEnum(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }
}
