package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.OrderDTO;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

public class RequestlistController implements Initializable {

    @FXML private TableView<OrderDTO> tableView;
    @FXML private TableColumn<OrderDTO, Integer> orderIdColumn;
    @FXML private TableColumn<OrderDTO, Integer> itemIdColumn;
    @FXML private TableColumn<OrderDTO, Integer> quantityColumn;
    @FXML private TableColumn<OrderDTO, String> affiliationCodeColumn;
    @FXML private TableColumn<OrderDTO, String> stateColumn;
    @FXML private TableColumn<OrderDTO, String> orderDateColumn;
    @FXML private TableColumn<OrderDTO, String> modeColumn;

    private String affiliationCode;

    private ObservableList<OrderDTO> allOrders = FXCollections.observableArrayList();

    public void setAffiliationCode(String code) {
        this.affiliationCode = code;
        loadOrders();
    }

    public void loadOrders() {
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

                Platform.runLater(() -> {
                    allOrders.setAll(orders); // 전체 데이터 저장
                    tableView.setItems(allOrders);
                });

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

        stateColumn.setSortable(false);

        //필터 메뉴 생성(State)
        ContextMenu filterMenu = new ContextMenu();
        String[] states = {"전체", "wait", "completed", "dismissed", "processed", "re-review-needed"};
        for (String state : states) {
            MenuItem menuItem = new MenuItem(state);
            menuItem.setOnAction(e -> filterByState(state.equals("전체") ? null : state));
            filterMenu.getItems().add(menuItem);
        }

        Label stateHeader = new Label("State ▼");
        stateHeader.setOnMouseClicked(e -> filterMenu.show(stateHeader, e.getScreenX(), e.getScreenY()));
        stateColumn.setGraphic(stateHeader);

        modeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                TableView<OrderDTO> table = getTableView();
                if (empty || getIndex() >= table.getItems().size()) {
                    setGraphic(null);
                    return;
                }

                OrderDTO order = table.getItems().get(getIndex());

                try {
                    if ("wait".equalsIgnoreCase(order.getState()) ||
                            "re-review-needed".equalsIgnoreCase(order.getState())) {

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/requestBtn1.fxml"));
                        Node node = loader.load();
                        RequestBtn1Controller controller = loader.getController();
                        controller.setOrder(order, table);
                        setGraphic(node);

                    } else if ("dismissed".equalsIgnoreCase(order.getState())) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/denyReasonBtn.fxml"));
                        Node node = loader.load();
                        DenyReasonBtnController controller = loader.getController();
                        controller.setOrder(order);
                        setGraphic(node);

                    } else {
                        setGraphic(null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    setGraphic(null);
                }
            }
        });
    }

    // 상태별 필터링
    private void filterByState(String state) {
        if (state == null) {
            tableView.setItems(allOrders); // 전체 표시
        } else {
            tableView.setItems(allOrders.filtered(o -> state.equalsIgnoreCase(o.getState())));
        }
    }
}
