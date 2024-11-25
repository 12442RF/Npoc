package com.attempt.npoc.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class VulTableController {
    public TextArea request;
    public TextArea response;
    
    private String requestString;

    private String responseString;
    
    @FXML
    private void initialize() {
        request.setText(requestString);
        response.setText(responseString);
    }
    
    public void setRequest(String requestString){
        this.requestString = requestString;
    }

    public void setResponse(String responseString){
        this.responseString = responseString;
    }
}
