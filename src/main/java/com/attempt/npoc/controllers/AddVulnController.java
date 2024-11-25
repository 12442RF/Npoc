package com.attempt.npoc.controllers;
import com.attempt.npoc.entity.Catalog;
import com.attempt.npoc.entity.ParsedTemplate;
import com.attempt.npoc.entity.Parser;
import com.attempt.npoc.entity.Template;
import com.attempt.npoc.utils.Cache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class AddVulnController {

    @FXML
    private TextField cmsName;
    private String cmsName1;
    @FXML
    private TextField vulnName;
    @FXML
    private TextArea vulnIntro;
 
    String editUuid = null;
    @FXML
    private void initialize() throws Exception {
        
        Parser parser = new Parser();
        Catalog catalog = new Catalog("");
        parser.loadTemplate("./extention", catalog);
        Cache parsedTemplatesCache = parser.getParsedTemplatesCache();
        List<ParsedTemplate> allParsedTemplate = parsedTemplatesCache.getAllParsedTemplate();
        for (ParsedTemplate parsedTemplate : allParsedTemplate) {
            if(parsedTemplate.getTemplate().getId().equals(cmsName1)){
                vulnIntro.setText(parsedTemplate.getRawData());
            }
        }
        // 创建右键菜单 (ContextMenu)
        ContextMenu contextMenu = new ContextMenu();
        // 创建“保存”菜单项
        MenuItem saveItem = new MenuItem("保存");
        saveItem.setOnAction(e -> {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            try {
                boolean flag = true;
                if(vulnIntro.getText()!=null) {
                    Template template = mapper.readValue(vulnIntro.getText(), Template.class);
                    for (ParsedTemplate parsedTemplate : allParsedTemplate) {
                        if (parsedTemplate.getTemplate().getId().equals(template.getId())) {
                            flag = false;
                            FileWriter writer = new FileWriter(parsedTemplate.getPath());
                            writer.append(vulnIntro.getText());
                            writer.close();
                        }
                    }
                    if(flag){
                        FileWriter writer = new FileWriter(".\\extention\\yaml\\"+template.getId()+".yaml");
                        writer.append(vulnIntro.getText());
                        writer.close();
                    }
                }else{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("保存的内容不能为空");
                    alert.showAndWait();
                }
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("发生错误: "+ex);
                alert.showAndWait();
            } 
        });
        contextMenu.getItems().addAll(saveItem);
        // 将“保存”菜单项添加到右键菜单中
        vulnIntro.setContextMenu(contextMenu);
    }

    
    public String getEditId() {
        return editUuid;
    }

    public void setEditUuid(String editUuid) {
        this.editUuid = editUuid;
    }
    public String getCmsName() {
        return cmsName1;
    }

    public void setCmsName(String cmsName1) {
        this.cmsName1 = cmsName1;
    }

}
