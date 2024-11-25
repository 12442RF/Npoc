package com.attempt.npoc.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

public class Config {
    @JsonProperty("templates-directory")
    private String templatesDirectory;

    public String getTemplatesDirectory() {
        return templatesDirectory;
    }

    public void setTemplatesDirectory(String nucleiTemplatesDirectory) {
        this.templatesDirectory = nucleiTemplatesDirectory;
    }
    
}
