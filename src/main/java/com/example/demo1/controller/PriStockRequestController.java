package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.PriItemStockDTO;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;

public class PriStockRequestController {

    @FXML private TextField itemIdField;
    @FXML private TextField quantityField;
    @FXML private DatePicker expire_DateField;
    @FXML private Button confirmBtn;

    private int itemId; // 아이템 ID 전달받음

    public void setItemId(int itemId) {
        this.itemId = itemId;
        itemIdField.setText(String.valueOf(itemId));
        itemIdField.setEditable(false); // 고정
    }

    @FXML
    public void initialize() {
        confirmBtn.setOnAction(e -> sendAddItemStockRequest());
    }

    private void sendAddItemStockRequest() {
        try {
            itemId = Integer.parseInt(itemIdField.getText());
            String quantityText = quantityField.getText().trim();
            LocalDate expireDate = expire_DateField.getValue();

            if (quantityText.isEmpty() || expireDate == null) {
                showAlert("입력 오류", "모든 항목을 입력하세요.");
                return;
            }

            int quantity = Integer.parseInt(quantityText);

            PriItemStockDTO dto = new PriItemStockDTO();
            dto.setItemId(itemId);
            dto.setQuantity(quantity);
            Timestamp expireDate2 = Timestamp.valueOf(expireDate.atStartOfDay());

            dto.setExpireDate(expireDate2);

            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(dto);

            URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/itemStock/add");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Cookie", Cookie.getSessionCookie());
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes());
            }

            int responseCode = conn.getResponseCode();

            // 응답 메시지 읽기 (성공 / 실패 둘 다)
            InputStream responseStream =
                    (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
            String serverMsg = new String(responseStream.readAllBytes());

            showAlert("서버 응답", serverMsg);

            // 성공 시 창 닫기
            if (responseCode == 200) {
                closeWindow();
            }

        } catch (NumberFormatException e) {
            showAlert("입력 오류", "수량은 숫자로 입력해야 합니다.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("예외 발생", e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) confirmBtn.getScene().getWindow();
        stage.close();
    }
}
