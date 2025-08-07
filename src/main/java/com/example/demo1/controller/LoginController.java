package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.LoginDTO;
import com.example.demo1.properties.ConfigLoader;
import com.example.demo1.refresh.RefreshEventConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.event.*;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;

import org.json.JSONObject;


public class LoginController {
    @FXML private TextField affiliationCodeField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginBtn;

    @FXML
    private AnchorPane rootPane; // 루트 FXML의 fx:id

    @FXML
    public void initialize() {
        rootPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loginBtn.fire();
            }
        });
    }

    @FXML
    protected void onLoginButtonClick(ActionEvent event) {
        try {
            String affiliationCode = affiliationCodeField.getText();
            String password = passwordField.getText();

            LoginDTO loginDTO = new LoginDTO(affiliationCode, password);

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(loginDTO);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String body = response.body();
                response.headers()
                        .firstValue("Set-Cookie")
                        .ifPresent(Cookie::setSessionCookie);
                JSONObject json = new JSONObject(body);
                boolean success = json.getBoolean("success");
                String message = json.getString("message");

                showAlert(success ? "로그인 성공" : "로그인 실패", message);

                if (success) {
                    // WebSocket 연결 시도 추가
                    try {
                        RefreshEventConnection refreshConnection = new RefreshEventConnection();
                        refreshConnection.connect("ws://localhost:8080/refresh", "102");
                        // 필요 시 컨트롤러나 앱 전역에 refreshConnection 참조 저장 가능
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("WebSocket 연결 실패", "실시간 새로고침 연결을 할 수 없습니다.");
                    }

                    if (affiliationCode.equals(ConfigLoader.getManagerCode())) {
                        // 본점 로그인: storeManagement.fxml로 이동
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/storeManagement.fxml"));
                        Parent root = loader.load();

                        StoreManagementController controller = loader.getController();
                        controller.setLoginAffiliationCode(ConfigLoader.getManagerCode());

                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setResizable(false);
                        stage.centerOnScreen();
                        stage.show();
                    } else {
                        // 분점 로그인: stuffManagement.fxml로 이동
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/stuffManagement.fxml"));
                        Parent root = loader.load();

                        StuffManagementController controller = loader.getController();
                        controller.setAffiliationContext(affiliationCode, affiliationCode);

                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setResizable(false);
                        stage.centerOnScreen();
                        stage.show();
                    }
                }

            } else {
                showAlert("오류", "서버 오류 발생: " + response.statusCode());
            }

        } catch (ConnectException ce) {
            showAlert("서버 연결 실패", "서버가 실행되고 있지 않거나 연결할 수 없습니다.");
        } catch (HttpTimeoutException te) {
            showAlert("시간 초과", "서버 응답 시간이 초과되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("예외 발생", "알 수 없는 오류: " + e.getMessage());
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
            stage.centerOnScreen();
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