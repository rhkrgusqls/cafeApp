package com.example.demo1.controller;

import com.example.demo1.controller.util.RefreshAll;
import com.example.demo1.dto.StuffDTO;
import com.example.demo1.properties.ConfigLoader;
import com.example.demo1.controller.util.Cookie;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class StuffDecreaseController {

    @FXML private TextField itemIdField;
    @FXML private TextField nameField;
    @FXML private TextField quantityField;
    @FXML private TextField decreaseField;
    @FXML private Button confirmBtn;

    private String affiliationCode;
    private StuffManagementController parentController; // 테이블 새로고침용

    public void setItemData(StuffDTO dto, String affiliationCode, StuffManagementController parent) {
        itemIdField.setText(String.valueOf(dto.getItemId()));
        nameField.setText(dto.getItemName());
        quantityField.setText(String.valueOf(dto.getQuantity()));
        this.affiliationCode = affiliationCode;
        this.parentController = parent;
    }

    @FXML
    private void onConfirm() {
        // 입력값 체크
        if (decreaseField.getText().isEmpty()) {
            showAlert("오류", "줄어든 수량을 입력하세요.");
            return;
        }

        int decreaseQty;
        try {
            decreaseQty = Integer.parseInt(decreaseField.getText());
        } catch (NumberFormatException e) {
            showAlert("오류", "숫자를 입력하세요.");
            return;
        }

        // 확인 다이얼로그
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("재고 감소 확인");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("정말 재고 감소를 하시겠습니까?");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response.getText().equals("OK") || response.getText().equals("확인")) {
                sendDecreaseRequest(decreaseQty);
            }
        });
    }

    private void sendDecreaseRequest(int decreaseQty) {
        new Thread(() -> {
            try {
                int itemId = Integer.parseInt(itemIdField.getText());
                System.out.println(affiliationCode);
                String urlStr = String.format(
                        "http://%s:%s/itemStock/decrease?itemId=%d&affiliationCode=%s&quantity=%d",
                        ConfigLoader.getIp(),
                        ConfigLoader.getPort(),
                        itemId,
                        affiliationCode,
                        decreaseQty
                );

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                String message = response.toString();

                javafx.application.Platform.runLater(() -> {
                    new RefreshAll();
                    showAlert("결과", message);

                    // 창 닫기
                    Stage stage = (Stage) confirmBtn.getScene().getWindow();
                    stage.close();

                    // 부모 테이블 새로고침
                    if (parentController != null) {
                        parentController.loadStuffList();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() ->
                        showAlert("오류", "서버 요청 중 오류가 발생했습니다.")
                );
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
