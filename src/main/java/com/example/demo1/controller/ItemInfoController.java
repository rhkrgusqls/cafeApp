package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.ItemDTO;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ItemInfoController {

    @FXML private TextField itemIdField;
    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private Button addBtn;

    private ItemlistController parentController;
    private String loginAffiliationCode;

    public void setLoginAffiliationCode(String code) {
        this.loginAffiliationCode = code;
        if (ConfigLoader.getManagerCode().equals(loginAffiliationCode)) {
            // 본사일 때 → 입력 가능 + Add 버튼 표시
            itemIdField.setEditable(true);
            nameField.setEditable(true);
            categoryField.setEditable(true);
            addBtn.setVisible(true);
        } else {
            // 분점일 때 → 입력 불가 + Add 버튼 숨김
            itemIdField.setEditable(false);
            nameField.setEditable(false);
            categoryField.setEditable(false);
            addBtn.setVisible(false);
        }
    }

    public void setParentController(ItemlistController controller) {
        this.parentController = controller;
    }

    @FXML
    public void initialize() {
        addBtn.setOnAction(e -> addItem());
    }

    private void addItem() {
        try {
            // 입력값 수집
            int itemId = Integer.parseInt(itemIdField.getText().trim());
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();

            if (name.isEmpty() || category.isEmpty()) {
                showAlert("오류", "모든 항목을 입력하세요.");
                return;
            }

            // DTO 생성
            ItemDTO item = new ItemDTO();
            item.setItemId(itemId);
            item.setName(name);
            item.setCategory(category);

            // JSON 직렬화
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(item);

            // HTTP 요청
            URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/items/add");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Cookie", Cookie.getSessionCookie());
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes());
            }

            // 응답 처리
            int responseCode = conn.getResponseCode();
            String responseMessage = (responseCode == 200)
                    ? new String(conn.getInputStream().readAllBytes())
                    : "응답 코드: " + responseCode;

            // 알림창 + 창 닫기 + 새로고침
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("서버 응답");
                alert.setHeaderText(null);
                alert.setContentText(responseMessage);

                alert.showAndWait().ifPresent(buttonType -> {
                    if (buttonType == ButtonType.OK) {
                        // 리스트 새로고침
                        if (parentController != null) {
                            parentController.loadItemList();
                        }
                        // 창 닫기
                        Stage stage = (Stage) addBtn.getScene().getWindow();
                        stage.close();
                    }
                });
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("예외 발생", e.getMessage());
        }
    }

    public void setItemId(int itemId) {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/items/list");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                InputStream is = conn.getInputStream();
                ObjectMapper mapper = new ObjectMapper();

                List<ItemDTO> itemList = mapper.readValue(is, new TypeReference<List<ItemDTO>>() {});
                for (ItemDTO item : itemList) {
                    if (item.getItemId() == itemId) {
                        Platform.runLater(() -> {
                            itemIdField.setText(String.valueOf(item.getItemId()));
                            nameField.setText(item.getName());
                            categoryField.setText(item.getCategory());
                        });
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
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
