package org.example.curddemo;

import lombok.Data;

@Data
public class Response<T> {
    private T data;
    private boolean status;
    private String errorMsg;

    public static <K> Response<K> newSuccess(K data){
          Response<K> response = new Response<>();
          response.setData(data);
          response.setStatus(true);
          return response;
    }

    public static Response<Void> newFail(String errorMsg){
        Response<Void> response = new Response<>();
        response.setStatus(false);
        response.setErrorMsg(errorMsg);
        return response;
    }
}
