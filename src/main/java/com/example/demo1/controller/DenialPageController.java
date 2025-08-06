package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.OrderDTO;
import com.example.demo1.dto.OrderRejectionHistoryDTO;
import com.example.demo1.properties.ConfigLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

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
        denyBtn.setOnAction(e -> showConfirmDialog());
    }

    //요청 거부 시 의사 재확인
    private void showConfirmDialog() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("거부 확인");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("정말 거부하시겠습니까?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                sendDismissedRequest();
            }
        });
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

    public void enableEditMode() { // 거부 사유 입력모드
        textArea.setEditable(true);
        denyBtn.setVisible(true);
        denyBtn.setManaged(true);
    }

    public void showReasonMode() { // 거부 사유 조회모드
        textArea.setEditable(false);
        denyBtn.setVisible(false);
        denyBtn.setManaged(false);

        new Thread(() -> {
            try {
                String urlStr = String.format(
                        "http://%s:%s/ordering/rejections",
                        ConfigLoader.getIp(),
                        ConfigLoader.getPort()
                );

                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<OrderRejectionHistoryDTO> list = mapper.readValue(
                        conn.getInputStream(),
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.List<OrderRejectionHistoryDTO>>() {}
                );

                String reasonText = list.stream()
                        .filter(r -> r.getOrderId() == order.getOrderId())
                        .map(r -> String.format(
                                "거부 사유: %s\n비고: %s\n시간: %s",
                                r.getRejectionReason(),
                                (r.getNotes() == null || r.getNotes().isBlank()) ? "-" : r.getNotes(),
                                r.getRejectionTime()
                        ))
                        .findFirst()
                        .orElse("해당 주문의 거부 사유가 없습니다.");

                Platform.runLater(() -> textArea.setText(reasonText));

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> textArea.setText("거부 사유를 불러오는 중 오류 발생"));
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
