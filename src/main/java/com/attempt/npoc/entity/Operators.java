package com.attempt.npoc.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Operators {
    @JsonProperty("matchers")
    private Matchers[] matchers;
    @JsonProperty("extractors")
    private Extractors[] extractor;
    @JsonProperty("matchers-condition")
    private String MatchersCondition;
//
//    private String TemplateID;
//    private ExcludeMatchers excludeMatchers;
    Operators() {
        this.MatchersCondition = "or";
    }
}
