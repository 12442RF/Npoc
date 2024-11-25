package com.attempt.npoc.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;

@Data
public class Request {
    @JsonUnwrapped
    private Operators operators;
//    private String[] path;
    @JsonProperty("raw")
    private String[] raw;
//    private String id;
//    private String name;
//    //AttackType generators.AttackTypeHolder
//    private String attackType;
//    //Method HTTPMethodTypeHolder
//    private String method;
//    private String body;
//    private Map<String,Object> payloads;
//    private Map<String,String> headers;
//    private int raceNumberRequests;
//    private int maxRedirects;
//    private int pipelineConcurrentConnections;
//    private int pipelineRequestsPerConnection;
//    private int threads;
//    private int maxSize;
////    private Rule fuzz;
//    private Operators CompiledOperators;
//    private ExecutorOptions options;
//    private Configuration connConfiguration;
//    private int totalRequests;
//    private Map<String,String> customHeaders;
//    private PayloadGenerator generator;
//    private Client httpClient;
//    private RawHttp rawHttpClient;
//    private boolean selfContained;
//    //private Signature signatureTypeHolder;
//    private boolean cookieReuse;
//    private boolean disableCookie;
//    private boolean forceReadAllBody;
//    private boolean Redirects;
//    private boolean hostRedirects;
//    private boolean pipLine;
//    private boolean unsafe;
//    private boolean race;
//    private boolean reqCondition;
//    private boolean stopAtFirstMatch;
//    private boolean SkipVariablesCheck;
//    private boolean iterateAll;
//    private String digestAuthUsername;
//    private String digestAuthPassword;
//    private boolean disablePathAutoMerge;
//    private Matchers fuzzPreCondition;
//    private String fuzzPreConditionOperator;
//    private ConditionType fuzzPreConditionOperatorType;
}
