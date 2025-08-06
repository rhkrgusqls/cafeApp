package com.example.demo1.controller;

import com.example.demo1.dto.OrderDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class DenyReasonBtnController {

    @FXML private Button dReasonBtn;
    private OrderDTO order;

    public void setOrder(OrderDTO order) {
        this.order = order;
        dReasonBtn.setOnAction(e -> openReasonPopup());
    }

    private void openReasonPopup() {
        // 팝업 띄워서 서버에서 거부 사유 불러와 보여주기
        System.out.println("거부 사유 보기: " + order.getOrderId());
    }
}
