package com.attempt.npoc.entity;

import com.attempt.npoc.http.HttpURLConnectionResponse;
import lombok.Data;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class HttpResponseWithDuration {
    private HttpURLConnectionResponse response;
    
    private String request;
    private Duration duration;
    private Map<String, List<String>> Exkval;
    private Map<String, List<String>> ExRegex;
    public HttpResponseWithDuration(){
        Exkval = new HashMap<>();
        ExRegex = new HashMap<>();
        duration = Duration.ZERO;
    }
    public HttpResponseWithDuration(HttpURLConnectionResponse response, Duration duration,String request){
        this.duration = duration;
        this.response = response;
        this.request = request;
    }
}
