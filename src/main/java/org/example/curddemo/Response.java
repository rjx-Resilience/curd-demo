package org.example.curddemo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
@Schema(description = "全局统一响应结果")
@Data
public class Response<T> {
    @Schema(description = "业务数据")
    private T data;

    @Schema(description = "是否成功",example = "true")
    private boolean status;

    @Schema(description = "错误信息（仅 status=false 时有值，成功时应保持空）")
    private String errorMsg;

    public static <K> Response<K> newSuccess(K data){
          Response<K> response = new Response<>();
          response.setData(data);
          response.setStatus(true);
          return response;
    }

    public static <K> Response<K> newFail(String errorMsg){
        Response<K> response = new Response<>();
        response.setStatus(false);
        response.setErrorMsg(errorMsg);
        return response;
    }

    public static <K> Response<K> newFail(K data, String errorMsg) {
        Response<K> response = new Response<>();
        response.setData(data);
        response.setStatus(false);
        response.setErrorMsg(errorMsg);
        return response;
    }
}
