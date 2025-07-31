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
        colStatus.setCellFactory(column -> new TableCell<StuffDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("defective".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

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
                URL url = new URL("http://localhost:8080/itemStock/list");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                // JSON 바디 작성
                String jsonBody = String.format("{\"affiliationCode\":\"%s\"}", affiliationCode);
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    InputStream is = conn.getInputStream();
                    ObjectMapper mapper = new ObjectMapper();
                    StuffDTO[] items = mapper.readValue(is, StuffDTO[].class);

                    Platform.runLater(() -> stuffTable.getItems().setAll(items));
                } else {
                    Platform.runLater(() ->
                            stuffTable.setPlaceholder(new Label("서버 오류: " + responseCode))
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        stuffTable.setPlaceholder(new Label("불러오기 실패"))
                );
            }
        }).start();
    }

}
