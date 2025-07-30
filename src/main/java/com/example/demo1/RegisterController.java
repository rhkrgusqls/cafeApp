package com.example.demo1;

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

    @FXML
    private TextField affiliationCodeField;
    @FXML
    private TextField regPwField;
    @FXML
    private TextField storeNameField;

    @FXML
    private Button signUpButton;
    @FXML
    private Button backButton;
    @FXML
    private Text sameText;



    @FXML
    protected void onSignUpButtonClick() {
        try {
            String affiliationCode = affiliationCodeField.getText();
            String password = regPwField.getText();
            String storeName = storeNameField.getText();


            String requestBody = String.format
                    ("{\"affiliationCode\":\"%s\", " +
                            "\"password\":\"%s\", " +
                            "\"storeName\":\"%s\"}", affiliationCode, password, storeName);
            System.out.println("요청 바디: " + requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/register/signup"))
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

                showAlert(success ? "로그인 성공" : "회원가입 성공", message);
            } else {
                showAlert("오류", "서버 오류 발생: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("예외 발생", e.getMessage());
        }
    }

    @FXML
    protected void onBackBtnClick(ActionEvent event) {
        try {
            Parent login = FXMLLoader.load(getClass().getResource("login.fxml"));
            Scene secondScene = new Scene(login);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(secondScene);
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
