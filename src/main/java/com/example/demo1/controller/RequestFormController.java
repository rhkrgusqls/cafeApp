package com.example.demo1.controller;

import com.example.demo1.dto.ItemRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestFormController {

    @FXML private TextField itemIdField;
    @FXML private TextField QuantityField;
    @FXML private Button submitBtn;

    private String affiliationCode; // 외부에서 주입

//    public void setRequestContext(int itemId, int quantity, String affiliationCode) {
//        itemIdField.setText(String.valueOf(itemId));
//        QuantityField.setText(String.valueOf(quantity));
//        this.affiliationCode = affiliationCode;
//    }

    @FXML
    public void initialize() {
        submitBtn.setOnAction(e -> sendRequest());
    }

    public void setAffiliationContext(String affiliationCode) {
        this.affiliationCode = affiliationCode;
    }

    private void sendRequest() {
        try {
            int itemId = Integer.parseInt(itemIdField.getText());
            int quantity = Integer.parseInt(QuantityField.getText());

            ItemRequestDTO requestPayload = new ItemRequestDTO();
            requestPayload.setAffiliationCode(affiliationCode); // dto 필드 설정

            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(requestPayload);

            URL url = new URL("http://localhost:8080/ordering/request?item_id=" + itemId + "&quantity=" + quantity);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes());
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                showAlert("요청 완료", "재고 요청이 성공적으로 처리되었습니다.");
            } else {
                showAlert("요청 실패", "서버 응답 코드: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("오류", "예외 발생: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
