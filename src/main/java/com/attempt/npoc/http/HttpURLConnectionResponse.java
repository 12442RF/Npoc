package com.attempt.npoc.http;

import lombok.Data;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

@Data
public class HttpURLConnectionResponse {
    private int responseCode;
    private String body;
    private String response;
    private Map<String, List<String>> headerFields;
    public HttpURLConnectionResponse(HttpURLConnection httpURLConnection) throws IOException {
        String body ;
        this.responseCode = httpURLConnection.getResponseCode();
        this.headerFields = httpURLConnection.getHeaderFields();
        
        if (responseCode >= 200 && responseCode < 300) {
            InputStream inputStream = httpURLConnection.getInputStream();
            body = InputStreamToString(inputStream,httpURLConnection);  // 成功响应
            inputStream.close();
        } else {
            InputStream inputStream = httpURLConnection.getErrorStream();
            body = InputStreamToString(httpURLConnection.getErrorStream(),httpURLConnection);  // 错误响应 (如 404)
            inputStream.close();
        }
        this.body = body;
        this.response = Response(headerFields,body);
    }
    private String Response(Map<String, List<String>> headerFields,String body)  {
        StringBuilder response = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
            if (!(entry.getKey()==null)) {
                for (String value : entry.getValue()) {
                    response.append(entry.getKey()).append(": ").append(value).append("\n");
                }
            }else{
                response.insert(0,entry.getValue().get(0)+"\n");
            }
        }
        response.append("\n");
        response.append(body);
        return response.toString();
    }
    public String InputStreamToString(InputStream inputStream,HttpURLConnection httpURLConnection) throws IOException {
        String encoding = httpURLConnection.getContentEncoding();
        BufferedReader reader = null;
        if(encoding!=null && encoding.equals("gzip")) {
            GZIPInputStream gZIPInputStream = new GZIPInputStream(httpURLConnection.getInputStream());
            reader = new BufferedReader(new InputStreamReader(gZIPInputStream, StandardCharsets.UTF_8));
        }else{
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        }
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }
        inputStream.close();
        return response.toString();
    }

}
