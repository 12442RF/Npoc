package com.attempt.npoc.entity;

import lombok.Data;

import java.util.Map;

@Data
public class Raw {
    private String fullUrl;
    private String method;
    private String path;
    private String Data;
    private Map<String,String> headers;
    private Byte[]  UnsafeRawBytes;
}
