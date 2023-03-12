package com.eagletsoft.boot.framework.api.utils;

import com.eagletsoft.boot.framework.common.errors.StandardErrors;

import java.io.Serializable;
import java.util.Date;

public class ApiResponse<T> implements Serializable {
    public ApiResponse(){

    }

    private int status;

    private T data;

    private String message;

    private Date timestamp = new Date();

    public int getStatus(){
        return status;
    }

    public void setStatus(int status){
        this.status = status;
    }

    public T getData(){
        return data;
    }

    public void setData(T data){
        this.data = data;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public static ApiResponse make(){
        ApiResponse httpResponse = new ApiResponse();
        httpResponse.setStatus(StandardErrors.NO.getStatus());
        return httpResponse;
    }

    public static ApiResponse make(Object data){
        ApiResponse httpResponse = new ApiResponse();
        httpResponse.setStatus(StandardErrors.NO.getStatus());
        httpResponse.setData(data);
        return httpResponse;
    }

    public static ApiResponse make(int status,String message){
        ApiResponse httpResponse = new ApiResponse();
        httpResponse.setStatus(status);
        httpResponse.setMessage(message);
        return httpResponse;
    }

    public static ApiResponse make(int status,String message,Object data){
        ApiResponse httpResponse = new ApiResponse();
        httpResponse.setStatus(status);
        httpResponse.setMessage(message);
        httpResponse.setData(data);
        return httpResponse;
    }

    public static ApiResponse make(Object data,int status){
        ApiResponse httpResponse = new ApiResponse();
        httpResponse.setStatus(status);
        httpResponse.setData(data);
        return httpResponse;
    }
}
