package com.attempt.npoc.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ExtractorTypeHolder {
    @JsonProperty("regex")
    regex,
    @JsonProperty("kval")
    kval,
    @JsonProperty("xpath")
    xpath,
    @JsonProperty("json")
    json,
    @JsonProperty("dsl")
    dsl,
}
