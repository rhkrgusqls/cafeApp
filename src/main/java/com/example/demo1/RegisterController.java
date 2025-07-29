package com.example.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;


public class RegisterController {

    @FXML
    private TextField regIdField;
    @FXML
    private TextField regPasswordField;
    @FXML
    private Button sameButton;
    @FXML
    private Button signUpButton;
    @FXML
    private Button backButton;

    @FXML
    protected void onSameButtonClick() {
        String regId = regIdField.getText();
    }

    @FXML
    protected void onSignUpButtonClick() {
        String regId = regIdField.getText();
        String regPassword = regPasswordField.getText();
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
}
