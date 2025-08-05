package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.OrderDTO;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
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

    @FXML private TableColumn<OrderDTO, String> modeColumn;


    private String affiliationCode;

    public void setAffiliationCode(String code) {
        this.affiliationCode = code;
        loadOrders(); // 코드 설정 후 바로 로드
    }

    private void loadOrders() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/ordering/display");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

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
        modeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                TableView<OrderDTO> table = getTableView();  // 안전하게 가져오기

                if (empty || getIndex() >= table.getItems().size()) {
                    setGraphic(null);
                    return;
                }

                OrderDTO order = table.getItems().get(getIndex());

                if ("wait".equalsIgnoreCase(order.getState())||
                        "re-review-needed".equalsIgnoreCase(order.getState())) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/requestBtn1.fxml"));
                        Node node = loader.load();
                        RequestBtn1Controller controller = loader.getController();

                        controller.setOrder(order, table);

                        setGraphic(node);
                    } catch (IOException e) {
                        e.printStackTrace();
                        setGraphic(null);
                    }
                } else {
                    setGraphic(null); // wait 상태 아닐 경우 버튼 숨김
                }
            }
        });
    }
}
