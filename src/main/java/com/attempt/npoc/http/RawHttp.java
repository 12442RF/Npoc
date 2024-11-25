package com.attempt.npoc.http;

import com.attempt.npoc.entity.Request;
import com.attempt.npoc.http.entity.Raw;
import com.attempt.npoc.http.entity.URLBean;
import org.apache.log4j.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;


public class RawHttp {
    static final Logger logger = Logger.getLogger(RawHttp.class);
    public Raw readRawRequest(String request) throws IOException {
        Raw rawRequest = new Raw();
        BufferedReader reader = new BufferedReader(new StringReader(request));
        boolean multiPartRequest = false;
        try {
            /** 
             * 获取 RawHttp 第一行 内容
             */
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String[] parts = line.split("\\s+");
                if(parts.length >0){
                    rawRequest.setMethod(parts[0]);
                    if (parts.length ==2 && parts[1].contains("HTTP") ){
                        parts = new String[]{parts[0],"",parts[1]};
                    }
                    if(parts.length<3){
                        throw  new Exception(String.format("malformed request specified: %s",line));
                    }
                    rawRequest.setPath(parts[1]);
                    break;
                }
                
            }
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if(line.isEmpty()){
                    break;
                }
                int colonIndex = line.indexOf(":",2);
                if(colonIndex != -1){
                    String key = line.substring(0, colonIndex).trim();
                    String value = line.substring(colonIndex + 1).trim();
                    if (key.equalsIgnoreCase("Content-Type") && value.contains("multipart/")) {
                        multiPartRequest = true;
                    }
                    rawRequest.AddHeader(key,value);
                }
            }
            if(multiPartRequest){
                StringBuilder body = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    body.append(line).append("\r\n");
                }
                if (!body.toString().isEmpty()){
                    rawRequest.setData(body.toString());
                }
            }else{
                StringBuilder body = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
                if (!body.toString().isEmpty()){
                    rawRequest.setData(body.toString());
                }
            }
        }catch (Exception e){
            logger.error(e);
            return null;
        }
        return rawRequest;
    }
    
}