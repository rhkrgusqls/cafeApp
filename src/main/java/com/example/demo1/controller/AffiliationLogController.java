package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.HistoryDTO;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AffiliationLogController {

    @FXML private TableView<HistoryDTO> historyTable;
    @FXML private TableColumn<HistoryDTO, Integer> orderIdColumn;
    @FXML private TableColumn<HistoryDTO, Integer> itemIdColumn;
    @FXML private TableColumn<HistoryDTO, Integer> quantityColumn;
    @FXML private TableColumn<HistoryDTO, String> stateColumn;
    @FXML private TableColumn<HistoryDTO, String> orderDateColumn;
    @FXML private TableColumn<HistoryDTO, String> mode;

    private String loginAffiliationCode;

    // FXML 로드 후 자동 실행되는 초기화 메서드
    @FXML
    public void initialize() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("state")); // DTO의 필드명
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate")); // DTO의 필드명

        mode.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                HistoryDTO dto = getTableView().getItems().get(getIndex());

                if ("processed".equalsIgnoreCase(dto.getState()) && !"101".equals(loginAffiliationCode)) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/modeBtnsRC.fxml"));
                        AnchorPane pane = loader.load();
                        ModeBtnsRCController btnController = loader.getController();

                        // 콜백: 현재 테이블 데이터 다시 불러오기
                        Runnable refreshCallback = () -> loadStockHistory();

                        btnController.init(dto.getOrderId(), loginAffiliationCode, refreshCallback);

                        setGraphic(pane);
                    } catch (IOException e) {
                        e.printStackTrace();
                        setGraphic(null);
                    }
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    public void setAffiliationContext(String affiliationCode) {
        this.loginAffiliationCode = affiliationCode;
        loadStockHistory();
    }


    private void loadStockHistory() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/ordering/display");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());
                conn.setDoOutput(true);

                String jsonBody = String.format("{\"affiliationCode\":\"%s\"}", loginAffiliationCode);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    InputStream is = conn.getInputStream();
                    ObjectMapper mapper = new ObjectMapper();
                    HistoryDTO[] stockHistories = mapper.readValue(is, HistoryDTO[].class);

                    Platform.runLater(() -> {
                        ObservableList<HistoryDTO> data = FXCollections.observableArrayList(stockHistories);
                        historyTable.setItems(data);
                    });
                } else {
                    Platform.runLater(() -> historyTable.setPlaceholder(new Label("서버 오류: " + responseCode)));
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> historyTable.setPlaceholder(new Label("불러오기 실패")));
            }
        }).start();
    }
}
