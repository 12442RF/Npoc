package com.attempt.npoc.service;

import com.attempt.npoc.dao.GetVulnTableDao;
import com.attempt.npoc.entity.ShowVulnTable;
import javafx.collections.ObservableList;


public class GetVulnTableService {
    GetVulnTableDao getVulnTableDao = new GetVulnTableDao();
    public ObservableList<ShowVulnTable> getShowList() throws Exception {
        return getVulnTableDao.getListFromFile();
    }
    public ObservableList<ShowVulnTable> getShowList2() throws Exception {
        return getVulnTableDao.getListFromFileChoose();
    }
}
