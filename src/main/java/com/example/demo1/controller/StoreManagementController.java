package com.example.demo1.controller;

import com.example.demo1.dto.AffiliationListResponse;
import com.example.demo1.dto.SignUpDTO;
import com.example.demo1.dto.StoreDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class StoreManagementController implements Initializable {

    @FXML private TableView<StoreDTO> storeTable;
    @FXML private TableColumn<StoreDTO, String> colAffiliationCode;
    @FXML private TableColumn<StoreDTO, String> colStoreName;

    @FXML private TextField affiliationCodeField;
    @FXML private TextField passwordField;
    @FXML private TextField storeNameField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colAffiliationCode.setCellValueFactory(new PropertyValueFactory<>("affiliationCode"));
        colStoreName.setCellValueFactory(new PropertyValueFactory<>("storeName"));

        loadStoreList(); // 초기 테이블 데이터 불러오기
    }

    private void loadStoreList() {
        new Thread(() -> {
            List<StoreDTO> storeList = fetchStoresFromApi();
            Platform.runLater(() -> {
                if (storeTable != null) {
                    storeTable.getItems().setAll(storeList);
                }
            });
        }).start();
    }

    private List<StoreDTO> fetchStoresFromApi() {
        try {
            URL url = new URL("http://localhost:8080/affiliation/list");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            InputStream is = conn.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            AffiliationListResponse response = mapper.readValue(is, AffiliationListResponse.class);
            return response.getAffiliationList();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() ->
                    storeTable.setPlaceholder(new Label("데이터를 가져올 수 없습니다."))
            );
            return List.of();
        }
    }

    @FXML
    private void onAddStore() {
        String code = affiliationCodeField.getText().trim();
        String password = passwordField.getText().trim();
        String name = storeNameField.getText().trim();

        if (code.isEmpty() || password.isEmpty() || name.isEmpty()) {
            showAlert("모든 항목을 입력하세요.");
            return;
        }

        SignUpDTO dto = new SignUpDTO(code, password, name);

        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/register/signup");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                ObjectMapper mapper = new ObjectMapper();
                try (OutputStream os = conn.getOutputStream()) {
                    mapper.writeValue(os, dto);
                }

                int status = conn.getResponseCode();
                if (status == 200 || status == 201) {
                    Platform.runLater(() -> {
                        showAlert("지점 등록 성공!");
                        clearFields();
                        loadStoreList();
                    });
                } else {
                    Platform.runLater(() ->
                            showAlert("등록 실패: 상태 코드 " + status)
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showAlert("등록 중 오류 발생")
                );
            }
        }).start();
    }

    @FXML
    private void onLogout(ActionEvent event) {
        URL fxmlUrl = getClass().getResource("/com/example/demo1/login.fxml");
        System.out.println("FXML 경로: " + fxmlUrl); // null이면 경로 문제

        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        affiliationCodeField.clear();
        passwordField.clear();
        storeNameField.clear();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("알림");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
