package com.attempt.npoc.entity;

import javafx.beans.property.SimpleStringProperty;

public class VulTable {

    public VulTable(String vulName, String vulLevel, String vulTarget, String vulId) {
        setVulId(vulId);
        setVulLevel(vulLevel);
        setVulName(vulName);
        setVulTarget(vulTarget);
    }
    
    private String vulName;
    private SimpleStringProperty vulNameString;

    private String vulLevel;
    private SimpleStringProperty vulLevelString;
    private String vulTarget;
    private SimpleStringProperty vulTargetString;
    private String vulId;
    private SimpleStringProperty vulIdString;

    private String vulRequest;
    
    private String vulResponse;
    
    public void setVulRequest(String vulRequest){
        this.vulRequest = vulRequest;
    }
    public String getVulRequest(){
        return this.vulRequest;
    }
    public void setVulResponse(String vulResponse){
        this.vulResponse = vulResponse;
    }
    public String getResponse(){
        return this.vulResponse;
    }
    
    public String getVulName() {
        return vulName;
    }
    public void setVulName(String vulName) {
        this.vulName = vulName;
        this.vulNameString = new SimpleStringProperty(String.valueOf(vulName));
    }
    public SimpleStringProperty vulNameStringProperty() {
        return vulNameString;
    }
    public String getVulId() {
        return vulName;
    }
    public void setVulId(String vulId) {
        this.vulId = vulId;
        this.vulIdString = new SimpleStringProperty(String.valueOf(vulId));
    }
    public SimpleStringProperty vulIdStringProperty() {
        return vulIdString;
    }
    public String getVulLevel() {
        return vulLevel;
    }
    public void setVulLevel(String vulLevel) {
        this.vulLevel = vulLevel;
        this.vulLevelString = new SimpleStringProperty(String.valueOf(vulLevel));
    }
    public SimpleStringProperty vulLevelStringProperty() {
        return vulLevelString;
    }
    public String getVulTarget() {
        return vulTarget;
    }
    public void setVulTarget(String vulTarget) {
        this.vulTarget = vulTarget;
        this.vulTargetString = new SimpleStringProperty(String.valueOf(vulTarget));
    }
    public SimpleStringProperty vulTargetStringProperty() {
        return vulTargetString;
    }
}
