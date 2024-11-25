package com.attempt.npoc.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
public class Extractors {
    @JsonProperty("name")
    private String name;
    @JsonProperty("type")
    private ExtractorTypeHolder type;
    private int extractorType;
    @JsonProperty("regex")
    private String[] regex;
    @JsonProperty("group")
    private int regexGroup;
    private Regexp[] regexCompiled;
    @JsonProperty("kval")
    private String[] kVal;
    @JsonProperty("json")
    private String[] json;
    @JsonProperty("xpath")
    private String[] xpath;
    private String attribute;
    private Code[] jsonCompiled;
    @JsonProperty("dsl")
    private String[] dsl;
    private EvaluableExpression[] dslCompiled;
    @JsonProperty("part")
    private String part;
    @JsonProperty("internal")
    private Boolean internal;
    @JsonProperty("case-insensitive")
    private Boolean caseInsensitive;
    
    public Extractors(){
        caseInsensitive = false;
    }
    // 将 Header[] 转换为 Map<String, List<String>>

    public HttpResponseWithDuration ExtractKval(HttpResponseWithDuration response){
        
        Map<String, List<String>> data = response.getResponse().getHeaderFields();
        if (caseInsensitive){
            Map<String, List<String>> inputData = response.getResponse().getHeaderFields();
            data = inputData.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey().toLowerCase(),
                            entry -> entry.getValue().stream()
                                    .map(String::toLowerCase)
                                    .collect(Collectors.toList())
                    ));
        }
        Map<String, List<String>> result = new HashMap<>();
        for (String k : kVal) {
            List<String> value = data.get(k);
            if (value == null){
                continue;
            }
            result.put(name,value);
        }
        response.setExkval(result);
        return response;
    }
    public HttpResponseWithDuration ExtractRegex(HttpResponseWithDuration response,String item){
        List<String> matchedRegexes = new ArrayList<>();
        for (String s : regex) {
            Pattern pattern = Pattern.compile(s);
            Matcher matcher = pattern.matcher(item);
            if (matcher.find()) {
                matchedRegexes.add(matcher.group(1));
            }
        }
        HashMap<String, List<String>> temp = new HashMap<>();
        temp.put(name,matchedRegexes);
        response.setExRegex(temp);
        return response ;
    }
    public String GetMatchPart(String part,HttpResponseWithDuration response) throws IOException {
        if (part == null){
            part = "body";
        }
        if (part.equals("header")){
            return response.getResponse().getHeaderFields().toString();
        } else if (part.equals("body")) {
            return response.getResponse().getBody();
        } else if (part.equals("all")) {
            return response.getResponse().getResponse();
        }else {
            return null;
        }
    }
}
