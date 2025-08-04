package com.example.demo1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// 로그 기록 DTO
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryDTO {
    private int orderId;       // 주문 ID
    private int itemId;        // 아이템 ID
    private int quantity;      // 수량
    private String state;     // 상태 (예: 정상, 불량)
    private String orderDate;       // 날짜
    //private String affiliationCode;


    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

//    public String getAffiliationCode() {
//        return affiliationCode;
//    }
//
//    public void setAffiliationCode(String affiliationCode) {
//        this.affiliationCode = affiliationCode;
//    }
}
