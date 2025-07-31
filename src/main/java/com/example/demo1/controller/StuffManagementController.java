package com.example.demo1.controller;

import com.example.demo1.dto.StuffDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ResourceBundle;

public class StuffManagementController implements Initializable {

    @FXML private TableView<StuffDTO> stuffTable;

    @FXML private TableColumn<StuffDTO, Integer> colNumber;
    @FXML private TableColumn<StuffDTO, String> colItemID;
    @FXML private TableColumn<StuffDTO, Integer> colQuantity;
    @FXML private TableColumn<StuffDTO, String> colExpireDate;
    @FXML private TableColumn<StuffDTO, String> colReceivedDate;
    @FXML private TableColumn<StuffDTO, String> colStatus;
    @FXML private TableColumn<StuffDTO, String> colAffiliationCode;
    @FXML private TableColumn<StuffDTO, String> colMode;

    private String affiliationCode; // 외부에서 설정

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNumber.setCellValueFactory(new PropertyValueFactory<>("stockId"));       // number → stockId
        colItemID.setCellValueFactory(new PropertyValueFactory<>("itemId"));        // itemID → itemId
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colExpireDate.setCellValueFactory(new PropertyValueFactory<>("expireDate"));
        colReceivedDate.setCellValueFactory(new PropertyValueFactory<>("receivedDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAffiliationCode.setCellValueFactory(new PropertyValueFactory<>("affiliationCode"));
        colMode.setCellValueFactory(new PropertyValueFactory<>("mode"));
    }

    // StoreManagementController에서 호출 시 affiliationCode를 전달
    public void setAffiliationCode(String affiliationCode) {
        this.affiliationCode = affiliationCode;
        loadStuffList();
    }

    private void loadStuffList() {
        new Thread(() -> {
            try {
                String urlStr = "http://localhost:8080/itemStock/list?affiliationCode=" + URLEncoder.encode(affiliationCode, "UTF-8");

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                InputStream is = conn.getInputStream();
                ObjectMapper mapper = new ObjectMapper();
                StuffDTO[] items = mapper.readValue(is, StuffDTO[].class);

                Platform.runLater(() -> {
                    stuffTable.getItems().setAll(items);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        stuffTable.setPlaceholder(new Label("불러오기 실패"))
                );
            }
        }).start();
    }
}
