package com.example.demo1.controller;

import com.example.demo1.dto.LoginDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;


public class LoginController {
    @FXML
    private TextField affiliationCodeField;

    @FXML
    private TextField passwordField;

    @FXML
    protected void onLoginButtonClick(ActionEvent event) {
        try {
            String affiliationCode = affiliationCodeField.getText();
            String password = passwordField.getText();

            LoginDTO loginDTO = new LoginDTO(affiliationCode, password);

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(loginDTO);

            System.out.println("요청 바디: " + requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String body = response.body();

                JSONObject json = new JSONObject(body);
                boolean success = json.getBoolean("success");
                String message = json.getString("message");

                showAlert(success ? "로그인 성공" : "로그인 실패", message);

                if (success) {
                    if (affiliationCode.equals("101")) {
                        // 본점 로그인: storeManagement.fxml로 이동
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/storeManagement.fxml"));
                        Parent root = loader.load();

                        // StoreManagementController가 로그인 지점 코드 기억하게 하기
                        StoreManagementController controller = loader.getController();
                        controller.setLoginAffiliationCode("101");

                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setResizable(false);
                        stage.show();

                    } else {
                        // 분점 로그인: stuffManagement.fxml로 이동
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/stuffManagement.fxml"));
                        Parent root = loader.load();

                        // 로그인자와 조회 대상 모두 자신
                        StuffManagementController controller = loader.getController();
                        controller.setAffiliationContext(affiliationCode, affiliationCode);

                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setResizable(false);
                        stage.show();
                    }
                }


            } else {
                showAlert("오류", "서버 오류 발생: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("예외 발생", e.getMessage());
        }
    }
    @FXML
    protected void onSignUpBtnClick(ActionEvent event) {
        try {
            Parent register = FXMLLoader.load(getClass().getResource("/com/example/demo1/register.fxml"));
            Scene secondScene = new Scene(register);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(secondScene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}