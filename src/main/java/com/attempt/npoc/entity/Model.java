package com.attempt.npoc.entity;
import com.attempt.npoc.deserializer.StringSliceDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class Model {
    @JsonProperty("name")
    private String name;
    @JsonDeserialize(using = StringSliceDeserializer.class)
    @JsonProperty("author")
    private  List<String> author;
    @JsonDeserialize(using = StringSliceDeserializer.class)
    @JsonProperty("tags")
    private  List<String> tags;
    @JsonProperty("description")
    private String description;
    @JsonProperty("impact")
    private String impact;
    @JsonDeserialize(using = StringSliceDeserializer.class)
    @JsonProperty("reference")
    private List<String> reference;
    @JsonProperty("severity")
    private String severity;
    @JsonProperty("metadata")
    private Map<String,Object> metadata;
    @JsonProperty("classification")
    private Classification classification;
    @JsonProperty("remediation")
    private String remediation;
}
