package com.attempt.npoc.entity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Template {
    @JsonProperty("id")
    private String id;
    @JsonProperty("info")
    private Model info;
    @JsonProperty("flow")
    private String flow;
    @JsonProperty("request")
    private Request[] request;

//    private CompiledWorkflow compiledWorkflow;
//    private Boolean SelfContained;
//    private Boolean stopAtFirstMatch;
//    private Signature signature;
//    private Variable variable;
//    private String Constants;
//
//    private Request requestsQueue;
//    private int TotalRequests;
//    private Executer executer;
//    private Path path;
//    private Boolean verified;
//    private String TemplateVerifier;
//    private String importedFiles;
}
