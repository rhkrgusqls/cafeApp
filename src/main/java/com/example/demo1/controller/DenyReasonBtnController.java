package com.example.demo1.controller;

import com.example.demo1.dto.OrderDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class DenyReasonBtnController {

    @FXML private Button dReasonBtn;
    private OrderDTO order;

    public void setOrder(OrderDTO order) {
        this.order = order;
        dReasonBtn.setOnAction(e -> openReasonPopup());
    }

    private void openReasonPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/DenialPage.fxml"));
            AnchorPane root = loader.load();

            DenialPageController controller = loader.getController();
            // order만 설정하고 거부 사유 보기 모드로 전환
            controller.setOrder(order, null);
            controller.showReasonMode();

            Stage stage = new Stage();
            stage.setTitle("거부 사유 보기");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
