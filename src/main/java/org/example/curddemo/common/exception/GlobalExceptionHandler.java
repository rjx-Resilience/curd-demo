package org.example.curddemo.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.curddemo.Response;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Response<String> handlerBusinessException(BusinessException e){
        log.warn("业务异常{}", e.getMessage());
        return Response.newFail(e.getMessage());
    }


    @ExceptionHandler(Exception.class)
    public Response<String> handlerException(Exception e){
        log.error("系统异常",e);

        return Response.newFail("系统繁忙，请稍后再试");
    }
}
