package com.attempt.npoc.http.entity;

import lombok.Data;

import java.util.List;

@Data
public class Headers {
    private List<Header> headers;
}
