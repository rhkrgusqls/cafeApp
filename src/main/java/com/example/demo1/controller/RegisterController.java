package com.example.demo1.controller;

import com.example.demo1.dto.SignUpDTO;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class RegisterController {

    @FXML private TextField affiliationCodeField;
    @FXML private TextField regPwField;
    @FXML private TextField storeNameField;

    @FXML private Button signUpButton;
    @FXML private Button backButton;
    @FXML private Text sameText;

    @FXML
    protected void onSignUpButtonClick() {
        try {
            String affiliationCode = affiliationCodeField.getText();
            String password = regPwField.getText();
            String storeName = storeNameField.getText();

            if (affiliationCode.isEmpty() || password.isEmpty() || storeName.isEmpty()) {
                showAlert("오류","모든 항목을 입력하세요.");
                return;
            }

            SignUpDTO signUpDTO = new SignUpDTO(affiliationCode, password, storeName);

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(signUpDTO);


            System.out.println("요청 바디: " + requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/register/signup"))
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

                showAlert(success ? "회원가입 성공" : "회원가입 실패", message);
            } else {
                showAlert("오류", "서버 오류 발생: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage();
            if (msg == null || msg.trim().isEmpty()) {
                msg = "알 수 없는 오류가 발생했습니다.";
            }
            showAlert("예외 발생", msg);
        }
    }

    @FXML
    protected void onBackBtnClick(ActionEvent event) {
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/com/example/demo1/login.fxml"));
            Scene secondScene = new Scene(login);

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
