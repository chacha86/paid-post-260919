package com.rest1.global.exception;

import com.rest1.global.rsData.RsData;

public class ServiceException extends RuntimeException {

    private String resultCode;
    private String msg;

    public ServiceException(String resultCode, String msg) {
        super("%s : %s".formatted(resultCode, msg));
        this.resultCode = resultCode;
        this.msg = msg;
    }

    public String getResultCode() {
        return resultCode;
    }

    public String getMsg() {
        return msg;
    }

    public RsData getRsData() {
        return new RsData(resultCode, msg);
    }
}
