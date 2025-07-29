package com.example.demo1;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.*;


public class LoginController {
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

    @FXML
    protected void onSignUpBtnClick(ActionEvent event) {
        try {
            Parent register = FXMLLoader.load(getClass().getResource("register.fxml"));
            Scene secondScene = new Scene(register);

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