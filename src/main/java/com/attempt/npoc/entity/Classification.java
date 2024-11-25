package com.attempt.npoc.entity;

import com.attempt.npoc.deserializer.StringSliceDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import java.util.List;

@Data
public class Classification {
    @JsonDeserialize(using = StringSliceDeserializer.class)
    @JsonProperty("cve-id")
    private List<String> cveId;
    @JsonDeserialize(using = StringSliceDeserializer.class)
    @JsonProperty("cwe-id")
    private List<String>  cweId;
    @JsonProperty("cvss-metrics")
    private String cvssMetrics;
    @JsonProperty("cvss-score")
    private double cvssScore;
    @JsonProperty("epss-score")
    private double epssScore;
    @JsonProperty("epss-percentile")
    private double epssPercentile;
    @JsonProperty("cpe")
    private String cpe;
}
