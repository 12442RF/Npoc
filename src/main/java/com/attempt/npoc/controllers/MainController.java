package com.attempt.npoc.controllers;

import com.attempt.npoc.StartApplication;
import com.attempt.npoc.entity.*;
import com.attempt.npoc.http.HttpURLConnectionResponse;
import com.attempt.npoc.http.RawHttp;
import com.attempt.npoc.http.entity.Raw;
import com.attempt.npoc.service.GetVulnTableService;
import com.attempt.npoc.service.SearchVulnService;
import com.attempt.npoc.utils.Cache;
import com.attempt.npoc.utils.LoadingSpinner;
import com.attempt.npoc.utils.RetryFuture;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import org.apache.log4j.Logger;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainController implements AddPocController.DataChangeListener {
    @FXML
    public LoadingSpinner loadingSpinner;
    @FXML
    public TableView<VulTable> vulTableView = new TableView<>();
    
    @FXML
    public TableColumn<?, ?>  vulTargetCol;
    @FXML
    public TableColumn<?, ?> vulLevelCol;
    @FXML
    public TableColumn<?, ?> vulNameCol;
    @FXML
    private TextArea logArea;
    @FXML
    private TextField targetInput;
    @FXML
    private TextField threadNumTextAll;
    @FXML
    private AnchorPane root;
    @FXML
    private TableView<ShowVulnTable> showVulnTableView;
    @FXML
    private TableColumn<?, ?> cmsNameCol;
    @FXML
    private TableColumn<?, ?> vulnNameCol;
    @FXML
    private TableColumn<?, ?> vulnIntroCol;
    @FXML
    private TextField vulnNameInput;
    @FXML
    private Text pocNumText;
//    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private volatile boolean stopRequested = false; // 控制标志
    GetVulnTableService getVulnTableService =  new GetVulnTableService();
    SearchVulnService searchVulnService =  new SearchVulnService();
    String pocNumStr="当前POC数量:%s";
    List<ShowVulnTable> submitPoc;
    private Task<Void> scanTask;
    static final Logger logger = Logger.getLogger(MainController.class);
    
    private int successNum = 0; // 成功数量

    ObservableList<VulTable> vTables = FXCollections.observableArrayList();;
    /**
     * 初始化加载函数
     */
    @FXML
    private void initialize() throws Exception {

         /**
         * 获取表格数据并展示
         */
        ObservableList<ShowVulnTable> vulnInfoList = getVulnTableService.getShowList();
        vulnNameCol.setCellValueFactory(new PropertyValueFactory("vulnNameString"));
        vulnIntroCol.setCellValueFactory(new PropertyValueFactory("vulnIntroString"));
        cmsNameCol.setCellValueFactory(new PropertyValueFactory("cmsNameString"));
        
        vulLevelCol.setCellValueFactory(new PropertyValueFactory<>("vulLevelString"));
        vulNameCol.setCellValueFactory(new PropertyValueFactory<>("vulNameString"));
        vulTargetCol.setCellValueFactory(new PropertyValueFactory<>("vulTargetString"));

        vulTableView.setItems(vTables);
        vulTableView.refresh();
        // 自定义排序逻辑
        // 自定义排序逻辑，应用到 vulnInfoList
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
        pocNumText.setText(String.format(pocNumStr, vulnInfoList.size()));

        // 创建右键菜单 (ContextMenu)
        ContextMenu contextMenu = new ContextMenu();
        // 创建“全选”菜单项
        MenuItem selectAllItem = new MenuItem("全选");
        selectAllItem.setOnAction(e -> logArea.selectAll());

        // 创建“复制”菜单项
        MenuItem copyItem = new MenuItem("复制");
        copyItem.setOnAction(e -> {
            String selectedText = logArea.getSelectedText();
            if (!selectedText.isEmpty()) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(selectedText);
                clipboard.setContent(content);
            }
        });

        // 创建“清空”菜单项
        MenuItem clearItem = new MenuItem("清空");
        clearItem.setOnAction(e -> logArea.clear());
        // 将菜单项添加到 ContextMenu
        contextMenu.getItems().addAll(selectAllItem, copyItem, clearItem);
        // 将 ContextMenu 绑定到 TextArea
        logArea.setContextMenu(contextMenu);
        
    }



    /**
     * 打开漏洞添加GUI
     */
    @FXML
    void addVulnGui(ActionEvent event) throws Exception {
        FXMLLoader fxmlLoaderAddVulnGui = new FXMLLoader(StartApplication.class.getResource("addVuln-view.fxml"));//导入fxml文件
        Scene addVulnGuiScene = new Scene(fxmlLoaderAddVulnGui.load() , 900, 550);//绑定fxml并设置内容大小
        Stage addPocGuiStage = new Stage();//实例化窗体
        addPocGuiStage.setScene(addVulnGuiScene);//设置窗体内容
        addPocGuiStage.setTitle("增加POC");
        addPocGuiStage.getIcons().add(new Image("img/addIcon.png"));
        addPocGuiStage.setResizable(false);
        addPocGuiStage.show();
        ObservableList<ShowVulnTable> vulnInfoList = getVulnTableService.getShowList();
        vulnNameCol.setCellValueFactory(new PropertyValueFactory("vulnNameString"));
        vulnIntroCol.setCellValueFactory(new PropertyValueFactory("vulnIntroString"));
        cmsNameCol.setCellValueFactory(new PropertyValueFactory("cmsNameString"));
        // 自定义排序逻辑
        // 自定义排序逻辑，应用到 vulnInfoList
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
        pocNumText.setText(String.format(pocNumStr, vulnInfoList.size()));
    }
    /**
     * 选择模板扫描
     */
    @FXML
    void addPocGui(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoaderAddPocGui = new FXMLLoader(StartApplication.class.getResource("addPoc-view.fxml"));//导入fxml文件
        Scene addVulnGuiScene = new Scene(fxmlLoaderAddPocGui.load() , 900, 550);//绑定fxml并设置内容大小
        Stage addPocGuiStage = new Stage();//实例化窗体
        addPocGuiStage.setScene(addVulnGuiScene);//设置窗体内容
        addPocGuiStage.setTitle("选择模板");
        addPocGuiStage.getIcons().add(new Image("img/addIcon.png"));
        addPocGuiStage.setResizable(false);
        AddPocController addPocController = fxmlLoaderAddPocGui.getController();
        // 设置 DataChangeListener
        addPocController.setDataChangeListener(this);
        addPocGuiStage.show();
        
    }
    
    
    /**
     * 搜索漏洞
     */
    @FXML
    void searchVuln(ActionEvent event) throws Exception {
                ObservableList<ShowVulnTable> resVulnInfoList = searchVulnService.searchByVulnName(vulnNameInput.getText());
                showVulnTableView.getItems().clear();
                FXCollections.sort(resVulnInfoList, (vuln1, vuln2) -> {
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
                showVulnTableView.setItems(resVulnInfoList);
                pocNumText.setText(String.format(pocNumStr, resVulnInfoList.size()));
    }
    /**
     * 编辑漏洞并打开漏洞添加GUI
     */
    @FXML
    void editVuln(ActionEvent event) throws Exception {
        AddVulnController addVulnController = new AddVulnController();
        addVulnController.setCmsName(showVulnTableView.getSelectionModel().getSelectedItem().getCmsName());

        FXMLLoader fxmlLoaderAddVulnGui = new FXMLLoader(StartApplication.class.getResource("addVuln-view.fxml"));//导入fxml文件
        fxmlLoaderAddVulnGui.setControllerFactory(param -> addVulnController);
        Scene addVulnGuiScene = new Scene(fxmlLoaderAddVulnGui.load() , 900, 550);//绑定fxml并设置内容大小
        Stage addPocGuiStage = new Stage();//实例化窗体
        addPocGuiStage.setScene(addVulnGuiScene);//设置窗体内容
        addPocGuiStage.setTitle("编辑POC");
        addPocGuiStage.getIcons().add(new Image("img/addIcon.png"));
        addPocGuiStage.setResizable(false);
        addPocGuiStage.show();
        ObservableList<ShowVulnTable> vulnInfoList = getVulnTableService.getShowList();
        vulnNameCol.setCellValueFactory(new PropertyValueFactory("vulnNameString"));
        vulnIntroCol.setCellValueFactory(new PropertyValueFactory("vulnIntroString"));
        cmsNameCol.setCellValueFactory(new PropertyValueFactory("cmsNameString"));
        // 自定义排序逻辑
        // 自定义排序逻辑，应用到 vulnInfoList
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
        pocNumText.setText(String.format(pocNumStr, vulnInfoList.size()));
    }
    /**
     * 打开设置代理界面
     */
    @FXML
    void setProxy(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoaderSetting = new FXMLLoader(StartApplication.class.getResource("setProxy-view.fxml"));//导入fxml文件
        Scene settingGuiScene = new Scene(fxmlLoaderSetting.load() , 400, 230);//绑定fxml并设置内容大小
        Stage settingGuiStage = new Stage();//实例化窗体
        settingGuiStage.setScene(settingGuiScene);//设置窗体内容
        settingGuiStage.setTitle("设置");
        settingGuiStage.getIcons().add(new Image("img/settings.png"));
        settingGuiStage.setResizable(false);
        settingGuiStage.show();

    }
    
    @FXML
    void deleteVuln(ActionEvent event) throws Exception {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // 创建一个确认对话框
        alert.setTitle("提示");
        alert.setHeaderText(null); // 设置对话框的头部文本
        // 设置对话框的内容文本
        alert.setContentText("\n是否要删除:"+showVulnTableView.getSelectionModel().getSelectedItem().getVulnName());
        // 显示对话框，并等待按钮返回
        Optional<ButtonType> buttonType = alert.showAndWait();
        // 判断返回的按钮类型是确定还是取消，再据此分别进一步处理
        if (buttonType.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE)) { // 单击了确定按钮OK_DONE
//            new AddVulnService().deleteByUuid(showVulnTableView.getSelectionModel().getSelectedItem().getUuid());
            Parser parser = new Parser();
            Catalog catalog = new Catalog("");
            parser.loadTemplate("./extention", catalog);
            Cache parsedTemplatesCache = parser.getParsedTemplatesCache();
            parsedTemplatesCache.getItems().iterate((k, v) -> {
               if (v.getTemplate().getId().equals(showVulnTableView.getSelectionModel().getSelectedItem().getCmsName())){
                   Path path = Paths.get(k);
                   try {
                       Files.delete(path);
                   } catch (IOException e) {
                       throw new RuntimeException(e);
                   }
               }
            });
            searchVuln(new ActionEvent());
        } else { // 单击了取消按钮CANCEL_CLOSE
            System.out.println("取消");
        }
    }
    
    @Override
    public void onDataChanged(List<ShowVulnTable> newData) {
        submitPoc = newData;
    }
    @FXML
    public void scanPoc(ActionEvent event) throws Exception {
        successNum = 0;
        if (submitPoc == null || submitPoc.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("未选择模板POC");
            alert.showAndWait();
        }else if (targetInput == null || targetInput.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("未输入目标地址");
            alert.showAndWait();
        }else {
            stopRequested = false; // 重置停止标志
            loadingSpinner.setVisible(true);
            
            scanTask  = new Task<>() {

                @Override
                protected Void call() throws Exception {
                    try {
                        if (stopRequested) {
                            // 如果请求停止，则退出任务
                            Scan(targetInput.getText(), submitPoc, threadNumTextAll.getText(),stopRequested,threadNumTextAll.getText());
                        }else {
                            Scan(targetInput.getText(), submitPoc, threadNumTextAll.getText(),stopRequested,threadNumTextAll.getText());
                        }
                    }finally {
                        loadingSpinner.setVisible(false);
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    // Hide the progress indicator
                    // 在 JavaFX 应用线程中关闭 Loading 对话框
                    Platform.runLater(() -> {
                        loadingSpinner.setVisible(false);
                    });
                }

                @Override
                protected void failed() {
                    super.failed();
                    Platform.runLater(() -> {
                        loadingSpinner.setVisible(false);
                        // 处理错误
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("扫描过程中发生错误:"+getException().getMessage());
                        alert.showAndWait();
                    });
                }
                @Override
                protected void cancelled() {
                    super.cancelled();
                }
            };

            new Thread(scanTask).start();
        }
    }
    @FXML
    public void scanPocStop(ActionEvent event) {
        if (scanTask != null && scanTask.isRunning()) {
            Platform.runLater(() -> {
                stopRequested = true; // 设置停止标志
                scanTask.cancel(true); // 取消任务
                // 停止
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Stop");
                alert.setHeaderText(null);
                alert.setContentText("停止扫描任务");
                alert.showAndWait();
            });
        }
    }
    /*
     * @Description: 获取勾选的扫描模板
     */
    public List<ParsedTemplate> getParsedTemplates(List<ShowVulnTable> showVulnTables) throws Exception {
        Parser parser = new Parser();
        Catalog catalog = new Catalog("");
        parser.loadTemplate("./extention", catalog);
        Cache parsedTemplatesCache = parser.getParsedTemplatesCache();
        List<ParsedTemplate> allParsedTemplate = new ArrayList<>();
        for (ShowVulnTable showVulnTable : showVulnTables) {
            ParsedTemplate template = parsedTemplatesCache.has(showVulnTable.getKey());
            if (template != null) {
                allParsedTemplate.add(template);
            }
        }
        return allParsedTemplate;
    }
    /*
     * @Description: 执行Raw封装请求
     */
    public HttpResponseWithDuration DoRawRequest(String raw) throws Exception {
        com.attempt.npoc.http.RawHttp rawHttp = new RawHttp();
        Raw rawRequest = rawHttp.readRawRequest(raw);
        String host = rawRequest.getHeaders().get("Host");
        ExecuteRequest executeRequest = new ExecuteRequest();
        Instant start = Instant.now();
        HttpURLConnectionResponse response = executeRequest.sendHttpRequest(rawRequest.getPath(), host,rawRequest.getMethod(), rawRequest.getHeaders(), rawRequest.getData());
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        return new HttpResponseWithDuration(response,timeElapsed,raw);
    }
    /*
     * @Description: 执行PhRaw封装请求
     */
    public List<HttpResponseWithDuration> DoPhRawRequest(List<String> rawListCopy,Request[] requestList,List<HttpResponseWithDuration> responseListCopy,List<HttpResponseWithDuration> responseList) throws Exception {
        for (String raw : rawListCopy) {
            List<String> TNameList = getPlaceholders(raw);
            for (Request request : requestList) {
                for (Extractors extractors : request.getOperators().getExtractor()) {
                    HttpResponseWithDuration extractResponse = Extract(responseListCopy, extractors);
                    if (extractResponse != null) {
                        for (String TName : TNameList) {
                            if (extractResponse.getExkval() != null) {
                                if (extractResponse.getExkval().get(TName) != null) {
                                    HashMap<String, String> temp = new HashMap<>();
                                    temp.put(TName, String.join("; ", extractResponse.getExkval().get(TName)));
                                    raw = replacePlaceholders(raw, temp);
                                }
                            }
                            if (extractResponse.getExRegex() != null) {
                                if (extractResponse.getExRegex().get(TName) != null) {
                                    HashMap<String, String> temp = new HashMap<>();
                                    temp.put(TName, String.join("; ", extractResponse.getExRegex().get(TName)));
                                    raw = replacePlaceholders(raw, temp);
                                }
                            }
                        }
                    }
                }
                responseList.add(DoRawRequest(raw));
            }
        }
        return responseList;
    }
    /*
     * @Description: 追加文本到textArea并且自行滚动到最后一行,默认info
     */
    private void appendTextAndScroll(TextArea textArea, String text) {
        appendTextAndScroll(textArea, text, "info"); // 默认调用 "info" 级别
    }
    /*
     * @Description: 追加文本到textArea并且自行滚动到最后一行
     */
    private void appendTextAndScroll(TextArea textArea, String text,String level) {
        LocalDateTime now = LocalDateTime.now();
        // 指定日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 格式化日期
        String formattedDate = now.format(formatter);
        // 输出格式为 [2024-09-11 10:05:00]
        String timestamp = "[" + formattedDate + "]";
        textArea.appendText(timestamp + " [" + level.toUpperCase() +"] - "+ text + "\n"); 
        textArea.positionCaret(textArea.getLength()); 
        textArea.selectPositionCaret(textArea.getLength()); 
        textArea.deselect(); 
    }
    /*
     * @Description: 执行RequestMatcher
     */
    public void DoRequestMatcher(Request[] requestsList, List<HttpResponseWithDuration> responseList,ParsedTemplate parsedTemplate,String target) throws Exception {
        List<Boolean> flag = new ArrayList<>();
        for (Request request : requestsList) {
            for (Matchers matcher : request.getOperators().getMatchers()){
                flag.add(Match(responseList,matcher));
            }
            com.attempt.npoc.http.RawHttp rawHttp ;
            Raw rawRequest1 =new Raw();
            try {
                rawHttp = new RawHttp();
                rawRequest1 = rawHttp.readRawRequest(request.getRaw()[0]);
            } catch (IOException e) {
               logger.error("解析数据包出错: "+e);
            }
            switch (request.getOperators().getMatchersCondition()) {
                case "and":
                    if( flag.stream().allMatch(b -> b)){
                        Raw finalRawRequest = rawRequest1;
                        Platform.runLater(() -> {
                            successNum ++;
                            appendTextAndScroll(logArea, "[SUCCESS] ["+parsedTemplate.getTemplate().getInfo().getSeverity().toUpperCase()+"] ["+ parsedTemplate.getTemplate().getInfo().getName() + "] [" + target+ finalRawRequest.getPath() +"]"  );
                            VulTable vulTable = new VulTable(parsedTemplate.getTemplate().getInfo().getName(),parsedTemplate.getTemplate().getInfo().getSeverity(),target,parsedTemplate.getTemplate().getId());
                            StringBuilder sr = new StringBuilder();
                            StringBuilder sq = new StringBuilder();
                            for(HttpResponseWithDuration i : responseList){
                                if(responseList.size() >= 2){
                                    sq.append(i.getResponse().getResponse()+"\n===================================================\n");
                                    sr.append(i.getRequest()+"\n===================================================\n");
                                }else{
                                    sq.append(i.getResponse().getResponse());
                                    sr.append(i.getRequest());
                                }
                            }
                            vulTable.setVulRequest(sr.toString());
                            vulTable.setVulResponse(sq.toString());
                            vTables.add(vulTable);
                            vulTableView.refresh();
                        });
                    }else {
                        Raw finalRawRequest = rawRequest1;
                        Platform.runLater(() -> {
                            appendTextAndScroll(logArea, "[FAIL] ["+parsedTemplate.getTemplate().getInfo().getSeverity().toUpperCase()+"] ["+parsedTemplate.getTemplate().getInfo().getName()+"] ["+ target + finalRawRequest.getPath() +"]");
                        });
                    }
                    break;
                case "or":
                    if (flag.stream().anyMatch(b -> b)){
                        Raw finalRawRequest = rawRequest1;
                        Platform.runLater(() -> {
                            successNum ++;
                            appendTextAndScroll(logArea, "[SUCCESS] ["+parsedTemplate.getTemplate().getInfo().getSeverity().toUpperCase()+"] ["+ parsedTemplate.getTemplate().getInfo().getName() + "] [" + target + finalRawRequest.getPath() +"]" );
                            VulTable vulTable = new VulTable(parsedTemplate.getTemplate().getInfo().getName(),parsedTemplate.getTemplate().getInfo().getSeverity(),target,parsedTemplate.getTemplate().getId());
                            StringBuilder sr = new StringBuilder();
                            StringBuilder sq = new StringBuilder();
                            for(HttpResponseWithDuration i : responseList){
                                if(responseList.size() >= 2){
                                    sq.append(i.getResponse().getResponse()+"\n===================================================\n");
                                    sr.append(i.getRequest()+"\n===================================================\n");
                                }else{
                                    sq.append(i.getResponse().getResponse());
                                    sr.append(i.getRequest());
                                }
                            }
                            vulTable.setVulRequest(sr.toString());
                            vulTable.setVulResponse(sq.toString());
                            vTables.add(vulTable);
                            vulTableView.refresh();
                        });
                    }else{
                        Raw finalRawRequest = rawRequest1;
                        Platform.runLater(() -> {
                            appendTextAndScroll(logArea, "[FAIL] ["+parsedTemplate.getTemplate().getInfo().getSeverity().toUpperCase()+"] ["+parsedTemplate.getTemplate().getInfo().getName()+"] ["+ target + finalRawRequest.getPath() +"]");
                        });
                    }
                    break;
            }
        }
    }
    /*
     * @Description: 扫描函数
     */
    public String Scan(String target, List<ShowVulnTable> showVulnTables,String pocNumStr,boolean stopRequested,String threadNum) throws Exception {
            long sTime = System.currentTimeMillis();
            ExecutorService executor = Executors.newFixedThreadPool(Integer.parseInt(threadNum));
            List<RetryFuture<Integer>> futureList = new ArrayList<>();
            List<Callable<Integer>> tasksList = new ArrayList<>();
            List<ParsedTemplate> allParsedTemplate = getParsedTemplates(showVulnTables);
            Platform.runLater(() -> {
                appendTextAndScroll(logArea, "已加载 "+ allParsedTemplate.size() +" 个扫描模板" );
            });
            for (ParsedTemplate parsedTemplate : allParsedTemplate) {
                boolean finalStopRequested1 = stopRequested;
                tasksList.add(() -> {
                    if (finalStopRequested1){
                        logger.info("Scan stopped");
                        return null;
                    }
                    List<String> rawList = parsedTemplate.GetTemplateRaw();
                    List<HttpResponseWithDuration> responseList = new ArrayList<>();
                    List<String> rawListCopy = new ArrayList<>();
                    for (String raw : rawList) {
                        raw = compileTemplate(raw, target);
                        if (ContainExtractors(raw, parsedTemplate.getTemplate().getRequest())) {
                            rawListCopy.add(raw);
                        } else {
                            responseList.add(DoRawRequest(raw));
                        }
                    }
                    List<HttpResponseWithDuration> responseListCopy = new ArrayList<>(responseList);
                    responseList = DoPhRawRequest(rawListCopy, parsedTemplate.getTemplate().getRequest(), responseListCopy, responseList);
                    DoRequestMatcher(parsedTemplate.getTemplate().getRequest(), responseList, parsedTemplate, target);
                    return null; 
                });
           
            }
            // 提交任务到线程池
            for (Callable<Integer> task : tasksList) {
                RetryFuture<Integer> future = new RetryFuture<>(task, executor, 3);
                futureList.add(future);
            }
            for (RetryFuture<Integer> future : futureList) {
                try {
                    if (stopRequested) {
                        future.cancel(true);
                        break;
                    }
                    future.get();
                }catch (InterruptedException interruptedException){
                    logger.error("Interrupted exception: " + interruptedException.getMessage());
                    executor.shutdown();
                    break;
                }
                catch (ExecutionException e) {
                    logger.error("Error occurred while executing task: " + e.getMessage());
                }
            }
            long eTime = System.currentTimeMillis();
            executor.awaitTermination(1, TimeUnit.SECONDS);
            Platform.runLater(() -> {
                appendTextAndScroll(logArea, "扫描任务完成, 共发现 "+ successNum +" 个漏洞, 耗时 "+(eTime - sTime)+" ms");
            });
            
            executor.shutdown();
            return null;
    }
    
    public String GetMatchPart(String part,HttpResponseWithDuration response) {
        if (part == null){
            part = "body";
        }
        if (part.equals("header")){
            return response.getResponse().getHeaderFields().toString();
        } else if (part.equals("body")) {
            return  response.getResponse().getBody();
        } else if (part.equals("all")) {
            return response.getResponse().getResponse();
        }else if(part.equals("status")) {
            return String.valueOf(response.getResponse().getResponseCode());
        }else if(part.equals("duration")){
            return String.valueOf(response.getDuration().toSeconds());
        }else {
            return null;
        }
    }
    public Boolean Match(List<HttpResponseWithDuration> responseList,Matchers matchers) throws IOException {
        String part = matchers.getPart();
        HttpResponseWithDuration response = new HttpResponseWithDuration();
        if(part!=null){
            String[] s = matchers.getPart().split("_", 2);
            if(s.length==2){
                part = s[0];
                response=responseList.get(Integer.parseInt(s[1]));
            }
        }
        if (responseList.size()==1){
            response = responseList.get(0);
        }
        String item = GetMatchPart(part, response);
        if (item != null){
            switch (matchers.getType()){
                case status:
                    return matchers.MatchStatusCode(item);
                case size:
                    return matchers.MatchSize(item.length());
                case word:
                    return matchers.MatchWords(item);
                case regex:
                    return matchers.MatchRegex(item);
                case binary:
                    return matchers.MatchBinary(item);
                case dsl:
                    break;
                case xpath:
                    return matchers.MatchXPath(item);
                case duration:
                    return matchers.MatchDuration(item);
            }
        }
        return false;

    }

    public HttpResponseWithDuration Extract(List<HttpResponseWithDuration> responseList,Extractors extractors) throws IOException {
        String part = extractors.getPart();
        HttpResponseWithDuration response = new HttpResponseWithDuration();
        if(part!=null){
            String[] s = extractors.getPart().split("_", 2);
            if(s.length==2){
                if(responseList.size()<Integer.parseInt(s[1])+1){
                    return null;
                }
                part = s[0];
                response=responseList.get(Integer.parseInt(s[1]));

            }
        }
        if(responseList.size()==1){
            response = responseList.get(0);
        }
        String item = GetMatchPart(part, response);
        switch (extractors.getType()){
            case regex:
                return extractors.ExtractRegex(response,item);
            case kval:
                return extractors.ExtractKval(response);
            case dsl:
                break;
            case json:
                break;
            case xpath:
                break;
        }
        return null;
    }
    // 判断模板中是否包含 {{名字}} 这样的占位符
    public static boolean containsPlaceholders(String template) {
        Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
        Matcher matcher = pattern.matcher(template);
        return matcher.find();
    }
    public static boolean ContainExtractors(String template,
                                            Request[] requestList) {
        Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
        Matcher matcher = pattern.matcher(template);
        ArrayList<String> TPName = new ArrayList<>();
        Boolean find = false;
        for (Request request : requestList) {
            if(request.getOperators().getExtractor()!=null) {
                for (Extractors extractors : request.getOperators().getExtractor()) {
                    String name = extractors.getName();
                    TPName.add(name);
                }
            }
        }
        
        while (matcher.find()) {
            String key = matcher.group(1).trim(); // 获取 {{和}} 之间的内容并去除空格
            if (TPName.contains(key)) {
                find = true;
                break;
            }
        }
        return find;
    }
    
    public static String replacePlaceholders(String template, Map<String, String> values) {
        // 定义匹配 {{名字}} 模板语法的正则表达式
        Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
        Matcher matcher = pattern.matcher(template);
        StringBuilder result = new StringBuilder();

        // 遍历所有匹配项并替换
        while (matcher.find()) {
            String key = matcher.group(1).trim(); // 获取 {{和}} 之间的内容并去除空格
            if(values.get(key) !=null){
                String replacement = values.getOrDefault(key, ""); // 获取替换值
                matcher.appendReplacement(result, replacement);
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }
    public static Map<String, String> parseURL(String url) throws URISyntaxException {
        URI uri = new URI(url);
        Map<String, String> values = new HashMap<>();
        // 如果 URL 的路径为空且不以 '/' 结尾，手动补充 '/'
        if (uri.getPath().isEmpty() && !url.endsWith("/")) {
            url += "/";
            uri = new URI(url);
        }
        // Parse and store the URL parts
        String baseUrl = url;
        String rootUrl = uri.getScheme() + "://" + uri.getAuthority();
        String hostname = uri.getAuthority();
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? (uri.getScheme().equals("https") ? 443 : 80) : uri.getPort();
        String path = uri.getPath().substring(0, uri.getPath().lastIndexOf('/'));
        String file = uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1);
        String scheme = uri.getScheme();

        // Store values in the map
        values.put("BaseURL", baseUrl);
        values.put("RootURL", rootUrl);
        values.put("Hostname", hostname);
        values.put("Host", host);
        values.put("Port", String.valueOf(port));
        values.put("Path", path);
        values.put("File", file);
        values.put("Scheme", scheme);

        return values;
 
    }
    // 返回模板中所有 {{}} 里面的字符串
    public static List<String> getPlaceholders(String template) {
        Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
        Matcher matcher = pattern.matcher(template);
        List<String> placeholders = new ArrayList<>();

        while (matcher.find()) {
            String key = matcher.group(1).trim(); // 获取 {{和}} 之间的内容并去除空格
            placeholders.add(key);
        }

        return placeholders;
    }
    public String compileTemplate(String raw,String target) throws URISyntaxException {
        Map<String, String> URLMap = parseURL(target);
        return replacePlaceholders(raw, URLMap);
    }

    @FXML
    public void editVulnDetail(ActionEvent event) throws IOException {
        VulTableController vulTableController = new VulTableController();
        vulTableController.setRequest(vulTableView.getSelectionModel().getSelectedItem().getVulRequest());
        vulTableController.setResponse(vulTableView.getSelectionModel().getSelectedItem().getResponse());
        FXMLLoader fxmlLoaderVulTableGui = new FXMLLoader(StartApplication.class.getResource("vulTable-view.fxml"));//导入fxml文件
        fxmlLoaderVulTableGui.setControllerFactory(param -> vulTableController);
        Scene VulTableGuiScene = new Scene(fxmlLoaderVulTableGui.load() , 900, 550);//绑定fxml并设置内容大小
//        VulTableGuiScene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage addPocGuiStage = new Stage();//实例化窗体
        addPocGuiStage.setScene(VulTableGuiScene);//设置窗体内容
        addPocGuiStage.setTitle("漏洞详情");
        addPocGuiStage.getIcons().add(new Image("img/addIcon.png"));
        addPocGuiStage.setResizable(false);
        addPocGuiStage.show();
    }
    @FXML
    public void refresh(ActionEvent event) throws Exception {
        /**
         * 获取表格数据并展示
         */
        ObservableList<ShowVulnTable> vulnInfoList = getVulnTableService.getShowList();
        vulnNameCol.setCellValueFactory(new PropertyValueFactory("vulnNameString"));
        vulnIntroCol.setCellValueFactory(new PropertyValueFactory("vulnIntroString"));
        cmsNameCol.setCellValueFactory(new PropertyValueFactory("cmsNameString"));

//        vulLevelCol.setCellValueFactory(new PropertyValueFactory<>("vulLevelString"));
//        vulNameCol.setCellValueFactory(new PropertyValueFactory<>("vulNameString"));
//        vulTargetCol.setCellValueFactory(new PropertyValueFactory<>("vulTargetString"));
//
//        vulTableView.setItems(vTables);
//        vulTableView.refresh();
        // 自定义排序逻辑
        // 自定义排序逻辑，应用到 vulnInfoList
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
        pocNumText.setText(String.format(pocNumStr, vulnInfoList.size()));
    }
}