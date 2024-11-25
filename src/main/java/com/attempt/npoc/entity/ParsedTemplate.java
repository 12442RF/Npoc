package com.attempt.npoc.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class ParsedTemplate {
    private Template template; 
    private String rawData;      
    private Exception err;
    private String path;
    
    public ParsedTemplate(Template template, String rawData, Exception error,String path) {
        this.template = template;
        this.rawData = rawData;
        this.err = error;
        this.path = path;
    }
    public List<String> GetTemplateRaw(){
        List<String> rawList = new ArrayList<>();
        for (Request request : template.getRequest()) {
            rawList.addAll(Arrays.asList(request.getRaw()));
        }
        return rawList;
    }
}
