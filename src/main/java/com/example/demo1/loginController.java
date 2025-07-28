package com.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class loginController {
    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    protected void onLoginButtonClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if ("admin".equals(username) && "1234".equals(password)) {
            showAlert("로그인 성공", "환영합니다!");
        } else {
            showAlert("로그인 실패", "아이디 또는 비밀번호가 틀렸습니다.");
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