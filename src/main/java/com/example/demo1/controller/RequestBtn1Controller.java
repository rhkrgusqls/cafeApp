package com.example.demo1.controller;

import com.example.demo1.dto.OrderDTO;
import com.example.demo1.properties.ConfigLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestBtn1Controller {

    @FXML private Button agreeBtn;
    @FXML private Button disagreeBtn;

    private TableView<OrderDTO> tableView;

    private static final String BASE_URL = "http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/ordering";

    public void setOrder(OrderDTO order, TableView<OrderDTO> tableView) {
        this.tableView = tableView;

        agreeBtn.setOnAction(e -> sendOrderDecision(order, true));
        disagreeBtn.setOnAction(e -> sendOrderDecision(order, false));
    }

    private void sendOrderDecision(OrderDTO order, boolean accepted) {
        new Thread(() -> {
            try {
                String endpoint = accepted ? "/accept" : "/dismissed";
                URL url = new URL(BASE_URL + endpoint + "?order_id=" + order.getOrderId());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = String.format("{\"affiliationCode\":\"%s\"}",101); // 어차피 이 페이지는 관리자밖에 못봄

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes("utf-8"));
                }

                int code = conn.getResponseCode();
                if (code == 200) {
                    Platform.runLater(() -> {
                        order.setState(accepted ? "processed" : "dismissed");
                        tableView.refresh();

                        agreeBtn.setDisable(true);
                        disagreeBtn.setDisable(true);
                        agreeBtn.setText(accepted ? "수락됨" : "수락");
                        disagreeBtn.setText(accepted ? "거부" : "거부됨");
                    });
                } else {
                    System.err.println("서버 응답 오류: " + code);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}
