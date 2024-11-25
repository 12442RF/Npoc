package com.attempt.npoc.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.codec.binary.Hex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

@Data
public class Matchers {
    @JsonProperty("type")
    private MatcherTypeHolder type;
    @JsonProperty("condition")
    private String condition;
    @JsonProperty("part")
    private String part;
    @JsonProperty("negative")
    private boolean negative;
    @JsonProperty("name")
    private String name;
    @JsonProperty("status")
    private int[] status;
    @JsonProperty("size")
    private int[] size;
    @JsonProperty("words")
    private String[] words;
    @JsonProperty("regex")
    private String[] regex;
    @JsonProperty("binary")
    private String[] binary;
    @JsonProperty("dsl")
    private String[] dsl;
    @JsonProperty("xpath")
    private String[] xpath;
    @JsonProperty("encoding")
    private String[] encoding;
    @JsonProperty("case-insensitive")
    private Boolean caseInsensitive;
    @JsonProperty("match-all")
    private Boolean matchAll;
    @JsonProperty("internal")
    private Boolean internal;
    @JsonProperty("duration")
    private int duration;
    public Matchers(){
        this.matchAll = false;
        this.caseInsensitive = false;
        this.condition="or";
    }
    // cached data for the compiled matcher

    // cached data for the compiled matcher
//    private ConditionType conditionType;     // todo: this field should be the one used for overridden marshal ops
//    private MatcherType matcherType;
//    private []string binaryDecoded;
//    private []*regexp.Regexp regexCompiled;
//    private []*govaluate.EvaluableExpressiondslCompiled;

    public boolean MatchStatusCode(String statusCode){
        for (int s : status) {
            if (!Objects.equals(statusCode, String.valueOf(s))){
                continue;
            }
            return true;
        }
        return false;
    }
    public boolean MatchSize(int length){
        for (int s : size) {
            if (length!=s){
                continue;
            }
            return true;
        }
        return false;
    }
    
    public boolean MatchWords(String item){
        if(caseInsensitive){
            item = item.toLowerCase();
        }
        List<String> matchedWords = new ArrayList<>();
        int i = -1;
        for (String word : words) {
            i ++;
            if (!item.contains(word)){
                switch (condition) {
                    case "and":
                        return false;
                    case "or":
                        continue;
                }
            }
            if (condition.equals("or") && !matchAll){
                return true; 
            }
            matchedWords.add(word);
            if((words.length-1)==i && !matchAll){
                return true;
            }
        }
        if (matchedWords.size()>0 && matchAll){
            return true;
        }
        return false;
    }
    public boolean MatchRegex(String item){
        List<String> matchedRegexes = new ArrayList<>();
        int i =-1;
        for (String s : regex) {
            Pattern re = Pattern.compile(s);
            Matcher matcher = re.matcher(item);
            i++;
            if (!matcher.find()){
                switch (condition) {
                    case "and":
                        return false;
                    case "or":
                        continue;
                }
            }
            List<String> currentMatches = regexAll(matcher);
            if (condition.equals("or") && !matchAll){
                return true;
            }
            matchedRegexes.addAll(currentMatches);
            if((regex.length-1) == i &&matchAll){
                return true;
            }
            
        }
        return matchedRegexes.size() > 0 && matchAll;
    }
    public List<String> regexAll(Matcher matcher){
        List<String> temp = new ArrayList<>();
        while (matcher.find()){
            temp.add(matcher.group());
        }
        return temp;
    }

    public Boolean MatchBinary(String item) {
        List<String> MatchedBinary = new ArrayList<>();
        int i=0;
        for (String bin : binary) {
            i++;
            String binHex = Hex.encodeHexString(bin.getBytes());
            if(!binHex.contains(item)){
                switch (condition){
                    case "and":
                        return false;
                    case "or":
                        continue;
                }
            }
            if (condition.equals("or")){
                return true;
            }
            MatchedBinary.add(binHex);
            if ((binary.length-1) == i ){
                return true;
            }
        }
        return false;
    }

    public Boolean MatchXPath(String item) {
        if(item.startsWith("<?xml")){
            return MatchXML(item);
        }
        return MatchHTML(item);
    }

    private Boolean MatchHTML(String item) {
        try {
            Document doc = Jsoup.parse(item);
            int matches = 0;
            for (String k : xpath) {
                Elements nodes = doc.select(k);
                if (nodes.size() == 0) {
                    // If we are in an AND request and a match failed,
                    // return false as the AND condition fails on any single mismatch.
                    if (condition.equals("and")) {
                        return false;
                    } else if (condition.equals("or")) {
                        continue;
                    }
                }

                // If the condition was an OR, return on the first match.
                if (condition.equals("or") && !matchAll) {
                    return true;
                }

                matches += nodes.size();
            }
            return matches > 0;
        } catch (Exception e) {
            return false;
        }
    
    }

    private Boolean MatchXML(String item) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            Document doc;
            try {
                builder = factory.newDocumentBuilder();
                doc = (Document) builder.parse(new InputSource(new StringReader(item)));
            } catch (Exception e) {
                return false;
            }

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            int matches = 0;

            for (String expression : xpath) {
                NodeList nodes;
                try {
                    nodes = (NodeList) xPath.evaluate(expression, doc, XPathConstants.NODESET);
                } catch (XPathExpressionException e) {
                    continue;
                }

                if (nodes.getLength() == 0) {
                    if (condition.equals("and")) {
                        return false;
                    } else {
                        continue;
                    }
                }

                if (condition.equals("or") && !matchAll) {
                    return true;
                }
                matches += nodes.getLength();
            }

            return matches > 0;
        }

    public Boolean MatchDuration(String item) {
        return Integer.parseInt(item) >= duration;
    }
}
