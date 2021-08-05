package org.lin.http.common;

import java.io.Serializable;

public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1064373066413371092L;

    public static final int SUCCESS = 0;

    public static final int FAIL = -1;

    private Integer status; // 0 success -1 fail
    private Integer code;
    private String msg;
    private T data;


    public static Result successResult() {
        return new Result(SUCCESS, 0, "success");
    }

    public static Result successResult(Object data) {
        return new Result(SUCCESS, 0, "success", data);
    }

       public static Result errorResult() {
        return new Result(FAIL, -1, "fail");
    }

    public static Result errorResultWithValue(Object data){
        return new Result(FAIL,-1,"fail", data);
    }

    public static Result errorResult(Integer code, String msg) {
        return new Result(FAIL, code, msg);
    }


    public static Result errorResult(String msg) {
        return new Result(FAIL, -1, msg);
    }

    public static Result exceptionResult() {
        return new Result(FAIL, -500, "system error");
    }

    public Result() {
    }

    public Result(Integer status, Integer code, String msg, T data) {
        this.status = status;
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result(Integer status, Integer code, String msg) {
        this.status = status;
        this.code = code;
        this.msg = msg;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}