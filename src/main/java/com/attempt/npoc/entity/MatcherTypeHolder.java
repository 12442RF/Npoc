package com.attempt.npoc.entity;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum MatcherTypeHolder {
    @JsonProperty("status")
    status,
    @JsonProperty("size")
    size,
    @JsonProperty("word")
    word,
    @JsonProperty("regex")
    regex,
    @JsonProperty("binary")
    binary,
    @JsonProperty("dsl")
    dsl,
    @JsonProperty("xpath")
    xpath,
    @JsonProperty("duration")
    duration;
}
