package org.example.curddemo.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.curddemo.Response;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLDataException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Response<String> handlerBusinessException(BusinessException e){
        log.warn("业务异常{}", e.getMessage());
        return Response.newFail(e.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Response<String> handleDuplicateKey(DuplicateKeyException e) {
        log.warn("唯一键冲突: {}", e.getMostSpecificCause().getMessage());
        String msg = simplify(e.getMostSpecificCause().getMessage());
        return Response.newFail("数据重复：数据库唯一键冲突 " + msg);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Response<String> handleIntegrity(DataIntegrityViolationException e) {
        log.warn("数据完整性异常: {}", e.getMostSpecificCause().getMessage());
        Throwable cause = e.getMostSpecificCause();
        String msg = cause != null ? simplify(cause.getMessage()) : e.getMessage();
        if (msg != null && msg.toLowerCase().contains("too long")) {
            return Response.newFail("字段超长：请检查邮箱/手机/学号/姓名等字段长度 " + msg);
        }
        return Response.newFail("数据不合法 " + msg);
    }

    @ExceptionHandler({SQLIntegrityConstraintViolationException.class, SQLDataException.class, SQLSyntaxErrorException.class})
    public Response<String> handleSql(SQLException e) {
        log.warn("SQL异常[{}]: {}", e.getSQLState(), e.getMessage());
        return Response.newFail("SQL执行错误 " + simplify(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public Response<String> handlerException(Exception e){
        log.error("系统异常", e);
        return Response.newFail("系统繁忙，请稍后再试");
    }

    private String simplify(String msg) {
        if (msg == null) return "";
        String s = msg.replaceAll("\\s+", " ");
        return s.length() > 120 ? s.substring(0, 120) + "..." : s;
    }
}
