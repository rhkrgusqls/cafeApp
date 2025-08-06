package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.OrderDTO;
import com.example.demo1.properties.ConfigLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.TableView;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DenialPageController {

    @FXML private TextField orderIdField;
    @FXML private TextField affiliationField;
    @FXML private TextArea textArea;
    @FXML private Button denyBtn;

    private OrderDTO order;
    private TableView<OrderDTO> tableView;

    public void setOrder(OrderDTO order, TableView<OrderDTO> tableView) {
        this.order = order;
        this.tableView = tableView;
        orderIdField.setText(String.valueOf(order.getOrderId()));
        affiliationField.setText(order.getAffiliationCode());
    }

    @FXML
    private void initialize() {
        denyBtn.setOnAction(e -> sendDismissedRequest());
    }

    private void sendDismissedRequest() {
        String reason = textArea.getText().trim();
        if (reason.isEmpty()) {
            showAlert("오류", "거절 사유를 입력하세요.");
            return;
        }

        new Thread(() -> {
            try {
                String urlStr = String.format(
                        "http://%s:%s/ordering/dismissed?order_id=%d&reason=%s",
                        ConfigLoader.getIp(),
                        ConfigLoader.getPort(),
                        order.getOrderId(),
                        java.net.URLEncoder.encode(reason, StandardCharsets.UTF_8)
                );

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                String json = String.format("{\"affiliationCode\":\"%s\"}", ConfigLoader.getManagerCode());
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                String serverMsg = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

                Platform.runLater(() -> {
                    showAlert("서버 응답", serverMsg);
                    if (serverMsg.contains("요청이 거절되었습니다.")) {
                        order.setState("dismissed");
                        tableView.refresh();
                        ((Stage) denyBtn.getScene().getWindow()).close();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> showAlert("오류", "서버 요청 실패"));
            }
        }).start();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
