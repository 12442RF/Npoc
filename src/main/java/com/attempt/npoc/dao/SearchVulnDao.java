package com.attempt.npoc.dao;

import com.attempt.npoc.entity.Catalog;
import com.attempt.npoc.entity.ParsedTemplate;
import com.attempt.npoc.entity.Parser;
import com.attempt.npoc.entity.ShowVulnTable;
import com.attempt.npoc.utils.Cache;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class SearchVulnDao {
    /**
     * 根据POC名搜索POC
     */
    public ObservableList<ShowVulnTable> searchByVulnName(String vulnName) throws Exception {
        ShowVulnTable showList = null;
        ObservableList<ShowVulnTable> list = FXCollections.observableArrayList();     //该集合必须是符合javafx形式的集合

        
        Parser parser = new Parser();
        Catalog catalog = new Catalog("");
        parser.loadTemplate("./extention", catalog);
        Cache parsedTemplatesCache = parser.getParsedTemplatesCache();
        List<ParsedTemplate> allParsedTemplate = parsedTemplatesCache.getAllParsedTemplate();
        for (ParsedTemplate parsedTemplate : allParsedTemplate) {
            if (!parsedTemplate.getTemplate().getInfo().getName().contains(vulnName)) {
                continue;
            }
            showList = new ShowVulnTable();
            showList.setCmsName(parsedTemplate.getTemplate().getId());
            showList.setVulnName(parsedTemplate.getTemplate().getInfo().getName());
            showList.setVulnIntro(parsedTemplate.getTemplate().getInfo().getDescription());
            list.add(showList);//将每个pocShowList对象装入list集合
        }
        return list;
    
    }
}
