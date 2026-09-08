package com.rest1.global.rsData;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RsData<T> {

    private String resultCode;
    private String msg;
    private T data;

    public RsData(String resultCode, String msg) {
        this.resultCode = resultCode;
        this.msg = msg;
        this.data = null;
    }

    @JsonIgnore
    public int getStatusCode() {
        String statusCode = resultCode.split("-")[0];
        return Integer.parseInt(statusCode);
    }

}
