package com.attempt.npoc.controllers;

import com.attempt.npoc.entity.ShowVulnTable;
import com.attempt.npoc.service.GetVulnTableService;
import com.attempt.npoc.service.SearchVulnService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class AddPocController {
    @FXML
    public Button submitPocButton;
    @FXML
    private CheckBox selectAllCheckBox;
    @FXML
    private TextField pocNameInput;

    @FXML
    private TableView<ShowVulnTable> showVulnTableView;
    @FXML
    private TableColumn<?, ?> cmsNameCol;
    @FXML
    private TableColumn<TableView,Boolean> selectedCol;
    @FXML
    private TableColumn<?, ?> vulnNameCol;
    @FXML
    private TableColumn<?, ?> vulnIntroCol;
   
    GetVulnTableService getVulnTableService =  new GetVulnTableService();
    String pocNumStr="当前POC数量:%s";
    SearchVulnService searchVulnService =  new SearchVulnService();
    private DataChangeListener dataChangeListener;
    ObservableList<ShowVulnTable> vulnInfoList = getVulnTableService.getShowList2();;

    public AddPocController() throws Exception {
    }

    public void setDataChangeListener(DataChangeListener listener) {
        this.dataChangeListener = listener;
    }
    
    @FXML
    private void initialize() throws Exception {

        /**
         * 获取表格数据并展示
         */
        
        
        selectedCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectedCol));
        selectedCol.setCellValueFactory(new PropertyValueFactory("selectedBoolean"));
        vulnNameCol.setCellValueFactory(new PropertyValueFactory("vulnNameString"));
        vulnIntroCol.setCellValueFactory(new PropertyValueFactory("vulnIntroString"));
        cmsNameCol.setCellValueFactory(new PropertyValueFactory("cmsNameString"));

        FXCollections.sort(vulnInfoList, (vuln1, vuln2) -> {
        String n1 = vuln1.getCmsName();  // 获取 CMS 名称
        String n2 = vuln2.getCmsName();

            // 判断并进行自定义的排序逻辑
        if (n1.contains("-") && n2.contains("-")) {
            n1 = n1.split("-", 2)[1];
            n2 = n2.split("-", 2)[1];
            try {
                return Integer.compare(Integer.parseInt(n1), Integer.parseInt(n2));
            } catch (NumberFormatException e) {
                // 如果无法转换为数字，则按字符串排序
                return n1.compareTo(n2);
            }
        } else {
            // 如果没有 "-", 则按字母顺序排序
            return n1.compareTo(n2);
        }
        });
        cmsNameCol.setComparator((name1, name2) -> {
            // 自定义排序逻辑，例如按字母顺序排序
            String n1 =name1.toString();
            String n2 = name2.toString();
            if(n1.contains("-")&&n2.contains("-")){
                n1  = n1.split("-", 2)[1];
                n2 = n2.split("-", 2)[1];
                return Integer.compare(Integer.parseInt(n1) ,Integer.parseInt(n2) );
            }else{
                return name1.toString().compareTo(name2.toString());
            }
        });
        
        
        showVulnTableView.setItems(vulnInfoList);

        // 绑定全选复选框的选中状态
        selectAllCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                for (ShowVulnTable item : vulnInfoList) {
                    item.setSelected(true);
                }
            }else {
                for (ShowVulnTable item : vulnInfoList) {
                    item.setSelected(false);
                }
            }
            showVulnTableView.refresh();
        });

        for(ShowVulnTable item : vulnInfoList){
            item.selectedBooleanProperty().addListener((observable, oldValue, newValue) -> {
                item.setOnlySelected(newValue);
            });
        }
    }
    @FXML
    void searchPoc(ActionEvent event) throws Exception {
        vulnInfoList = searchVulnService.searchByVulnName(pocNameInput.getText());
        showVulnTableView.getItems().clear();
        FXCollections.sort(vulnInfoList, (vuln1, vuln2) -> {
            String n1 = vuln1.getCmsName();  // 获取 CMS 名称
            String n2 = vuln2.getCmsName();

            // 判断并进行自定义的排序逻辑
            if (n1.contains("-") && n2.contains("-")) {
                n1 = n1.split("-", 2)[1];
                n2 = n2.split("-", 2)[1];
                try {
                    return Integer.compare(Integer.parseInt(n1), Integer.parseInt(n2));
                } catch (NumberFormatException e) {
                    // 如果无法转换为数字，则按字符串排序
                    return n1.compareTo(n2);
                }
            } else {
                // 如果没有 "-", 则按字母顺序排序
                return n1.compareTo(n2);
            }
        });
        cmsNameCol.setComparator((name1, name2) -> {
            // 自定义排序逻辑，例如按字母顺序排序
            String n1 =name1.toString();
            String n2 = name2.toString();
            if(n1.contains("-")&&n2.contains("-")){
                n1  = n1.split("-", 2)[1];
                n2 = n2.split("-", 2)[1];
                return Integer.compare(Integer.parseInt(n1) ,Integer.parseInt(n2) );
            }else{
                return name1.toString().compareTo(name2.toString());
            }
        });
        showVulnTableView.setItems(vulnInfoList);
       
    }
    
    @FXML
    public void submitPoc(ActionEvent event) {
        List<ShowVulnTable> collect = vulnInfoList.stream()
                .filter(u -> u.getSelected())
                .collect(Collectors.toList());
        if (dataChangeListener != null) {
            dataChangeListener.onDataChanged(collect);
        }
        Stage stage = (Stage) submitPocButton.getScene().getWindow();
        stage.close();
    }

    public interface DataChangeListener {
        void onDataChanged(List<ShowVulnTable> newData);
    }
}
