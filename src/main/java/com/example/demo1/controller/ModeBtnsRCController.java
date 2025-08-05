package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.OrderDTO;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ModeBtnsRCController {

    @FXML private Button reviewButton;
    @FXML private Button completeButton;

    private int orderId;
    private String affiliationCode;
    private Runnable refreshTableCallback; // 테이블 새로고침 함수 참조

    public void init(int orderId, String affiliationCode, Runnable refreshCallback) {
        this.orderId = orderId;
        this.affiliationCode = affiliationCode;
        this.refreshTableCallback = refreshCallback;

        reviewButton.setOnAction(e -> confirmAndSend("/ordering/review", "재검토"));
        completeButton.setOnAction(e -> confirmAndSend("/ordering/completed", "검수완료"));
    }

    private void confirmAndSend(String endpoint, String actionName) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("확인");
        confirm.setHeaderText(null);
        confirm.setContentText("정말 " + actionName + " 하시겠습니까?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            sendRequest(endpoint, actionName);
        }
    }

    private void sendRequest(String endpoint, String actionName) {
        new Thread(() -> {
            try {
                OrderDTO req = new OrderDTO();
                req.setAffiliationCode(affiliationCode);

                ObjectMapper mapper = new ObjectMapper();
                String jsonBody = mapper.writeValueAsString(req);

                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort()
                        + endpoint + "?order_id=" + orderId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes());
                }

                int responseCode = conn.getResponseCode();
                String responseMessage;
                try (InputStream is = conn.getInputStream()) {
                    responseMessage = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }

                Platform.runLater(() -> {
                    Alert alert;
                    if (responseCode == 200) {
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("서버 응답");
                        alert.setHeaderText(null);
                        alert.setContentText(responseMessage); // 서버에서 보낸 문자열 표시
                        alert.showAndWait();

                        if (refreshTableCallback != null) {
                            refreshTableCallback.run(); // 테이블 새로고침
                        }
                    } else {
                        alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("서버 오류");
                        alert.setHeaderText(null);
                        alert.setContentText("응답 코드: " + responseCode + "\n" + responseMessage);
                        alert.showAndWait();
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("오류");
                    alert.setHeaderText(null);
                    alert.setContentText("예외 발생: " + ex.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

}
