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


public class RegisterController {

    @FXML
    private TextField regId;
    @FXML
    private TextField regPw;
    @FXML
    private TextField storeId;
    @FXML
    private Button sameButton;
    @FXML
    private Button signUpButton;
    @FXML
    private Button backButton;
    @FXML
    private Text sameText;


    @FXML
    protected void onSameButtonClick() {
        if (regId.equals("")) {
            sameText.setVisible(true);
        }else{
            sameText.setText("중복된 ID값이거나 잘못된 값입니다.");
            sameText.setVisible(true);
        }
    }

    @FXML
    protected void onSignUpButtonClick() {
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
