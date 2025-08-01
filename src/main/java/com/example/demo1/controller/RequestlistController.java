package com.example.demo1.controller;

import com.example.demo1.dto.OrderDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

public class RequestlistController implements Initializable {

    @FXML private TableView<OrderDTO> tableView;
    @FXML private TableColumn<OrderDTO, Integer> orderIdColumn;
    @FXML
    private TableColumn<OrderDTO, Integer> itemIdColumn;
    @FXML private TableColumn<OrderDTO, Integer> quantityColumn;
    @FXML private TableColumn<OrderDTO, String> affiliationCodeColumn;
    @FXML private TableColumn<OrderDTO, String> stateColumn;
    @FXML private TableColumn<OrderDTO, String> orderDateColumn;

    private String affiliationCode;

    public void setAffiliationCode(String code) {
        this.affiliationCode = code;
        loadOrders(); // 코드 설정 후 바로 로드
    }

    private void loadOrders() {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/ordering/display");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                String json = String.format("{\"affiliationCode\":\"%s\"}", affiliationCode);
                conn.getOutputStream().write(json.getBytes("utf-8"));

                InputStream is = conn.getInputStream();
                ObjectMapper mapper = new ObjectMapper();
                OrderDTO[] orders = mapper.readValue(is, OrderDTO[].class);

                Platform.runLater(() -> tableView.getItems().setAll(orders));

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> tableView.setPlaceholder(new Label("불러오기 실패")));
            }
        }).start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        affiliationCodeColumn.setCellValueFactory(new PropertyValueFactory<>("affiliationCode"));
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
    }
}
