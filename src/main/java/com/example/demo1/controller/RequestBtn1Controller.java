package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.OrderDTO;
import com.example.demo1.properties.ConfigLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RequestBtn1Controller {

    @FXML private Button agreeBtn;
    @FXML private Button disagreeBtn;

    private TableView<OrderDTO> tableView;

    private static final String BASE_URL =
            "http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/ordering";

    public void setOrder(OrderDTO order, TableView<OrderDTO> tableView) {
        this.tableView = tableView;

        // processed, re-review-needed 두 상태에서만 버튼 활성
        if ("wait".equalsIgnoreCase(order.getState()) ||
                "re-review-needed".equalsIgnoreCase(order.getState())) {
            agreeBtn.setDisable(false);
            disagreeBtn.setDisable(false);
        } else {
            agreeBtn.setDisable(true);
            disagreeBtn.setDisable(true);
        }

        agreeBtn.setOnAction(e -> showConfirmDialog(order, true));
        disagreeBtn.setOnAction(e -> openDenialPage(order));
    }

    // 확인창 표시
    private void showConfirmDialog(OrderDTO order, boolean accepted) {
        String actionText = accepted ? "수락" : "거부";
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(actionText + " 확인");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("정말 이 요청을 " + actionText + "하시겠습니까?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                sendOrderDecision(order, accepted);
            }
        });
    }

    // 서버 요청
    private void sendOrderDecision(OrderDTO order, boolean accepted) {
        new Thread(() -> {
            try {
                String endpoint = accepted ? "/accept" : "/dismissed";
                URL url = new URL(BASE_URL + endpoint + "?order_id=" + order.getOrderId());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                String json = String.format("{\"affiliationCode\":\"%s\"}", 101); // 관리자 고정
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();

                // 응답 메시지 읽기
                InputStream is = (code >= 200 && code < 300)
                        ? conn.getInputStream() : conn.getErrorStream();
                String serverMsg = (is != null) ? new String(is.readAllBytes(), StandardCharsets.UTF_8) : "응답 없음";

                Platform.runLater(() -> {
                    // 서버 메시지 표시
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("서버 응답");
                    alert.setHeaderText(null);
                    alert.setContentText(serverMsg);
                    alert.showAndWait();

                    if (code == 200) {
                        order.setState(accepted ? "processed" : "dismissed");
                        tableView.refresh();

                        agreeBtn.setDisable(true);
                        disagreeBtn.setDisable(true);
                        agreeBtn.setText(accepted ? "수락됨" : "수락");
                        disagreeBtn.setText(accepted ? "거부" : "거부됨");
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
    private void openDenialPage(OrderDTO order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/DenialPage.fxml"));
            AnchorPane root = loader.load();

            DenialPageController controller = loader.getController();
            controller.setOrder(order, tableView);

            Stage stage = new Stage();
            stage.setTitle("거절 사유 입력");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
