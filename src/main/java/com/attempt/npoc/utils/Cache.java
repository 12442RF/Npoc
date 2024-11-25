package com.attempt.npoc.utils;

import com.attempt.npoc.entity.ParsedTemplate;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Cache {
    private SyncLockMap<String, ParsedTemplate> items;
    
    public Cache(){
        this.items = new SyncLockMap<String, ParsedTemplate>();
    }
    
    public SyncLockMap<String, ParsedTemplate> NewCache(){
        return new SyncLockMap<String, ParsedTemplate>();
    }
    
    public ParsedTemplate has(String template){
        try {
            return items.get(template);
        }catch (NullPointerException e){
            return null;
        }
    }
    public List<ParsedTemplate> getAllParsedTemplate(){
        List<ParsedTemplate> parsedTemplateList = new ArrayList<>();
        items.iterate((k, v) -> {
            parsedTemplateList.add(v);
        });
        return parsedTemplateList;
    }
//    public SyncLockMap<String,ParsedTemplate> getAllParsedTemplatePath(){
//        List<ParsedTemplate> parsedTemplateList = new ArrayList<>();
//        items.iterate((k, v) -> {
//            parsedTemplateList.add(v);
//        });
//        return parsedTemplateList;
//    }
    public void store(String id, ParsedTemplate tpl) {
        items.set(id,tpl);
    }
    
    public void Purge(){
        items.clear();
    }
}
