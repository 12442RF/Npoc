package com.attempt.npoc.service;

import com.attempt.npoc.dao.GetVulnTableDao;
import com.attempt.npoc.dao.SearchVulnDao;
import com.attempt.npoc.entity.ShowVulnTable;
import javafx.collections.ObservableList;

public class SearchVulnService {
    GetVulnTableDao getVulnTableDao = new GetVulnTableDao();
    SearchVulnDao searchVulnDao =  new SearchVulnDao();

    public ObservableList<ShowVulnTable> searchByVulnName(String vulnName) throws Exception {
        if (vulnName != null && vulnName.length() != 0){
            return searchVulnDao.searchByVulnName(vulnName);
        }
        else {
            return getVulnTableDao.getListFromFile();
        }
    }
}
