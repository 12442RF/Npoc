package com.attempt.npoc.http.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Raw {
    private String fullUrl;
    private String method;
    private String path;
    private String Data;
    private Map<String,String> headers;
    private Headers unsafeHeaders;
    private byte[]  unsafeRawBytes;
    
    public Raw(){
        this.headers = new HashMap<>();
    }
    public void AddHeader(String key,String value){
        this.headers.put(key,value);
    }
}
