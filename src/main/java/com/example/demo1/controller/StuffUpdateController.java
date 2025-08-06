package com.example.demo1.controller;

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

public class StuffUpdateController {

    @FXML private TextField itemIdField;
    @FXML private TextField nameField;
    @FXML private TextField quantityField;
    @FXML private TextField updateField;
    @FXML private Button updateBtn;

    private int stockId;
    private String affiliationCode;
    private StuffManagementController parentController;

    public void setItemData(StuffDTO dto, StuffManagementController parent) {
        this.stockId = dto.getStockId();
        this.affiliationCode = dto.getAffiliationCode();
        itemIdField.setText(String.valueOf(dto.getItemId()));
        nameField.setText(dto.getItemName());
        quantityField.setText(String.valueOf(dto.getQuantity()));
        this.parentController = parent;
    }

    @FXML
    private void onUpdate() {
        if (updateField.getText().isEmpty()) {
            showAlert("오류", "바꿀 수량을 입력하세요.");
            return;
        }

        int newQty;
        try {
            newQty = Integer.parseInt(updateField.getText());
        } catch (NumberFormatException e) {
            showAlert("오류", "숫자를 입력하세요.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("재고 수정 확인");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("정말 재고를 수정하시겠습니까?");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response.getText().equals("OK") || response.getText().equals("확인")) {
                sendUpdateRequest(newQty);
            }
        });
    }

    private void sendUpdateRequest(int newQty) {
        new Thread(() -> {
            try {
                String urlStr = String.format(
                        "http://%s:%s/itemStock/update?stockId=%d&quantity=%d",
                        ConfigLoader.getIp(),
                        ConfigLoader.getPort(),
                        stockId,
                        newQty
                );

                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
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
                    showAlert("결과", message);
                    ((Stage) updateBtn.getScene().getWindow()).close();
                    parentController.loadStuffList();
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
