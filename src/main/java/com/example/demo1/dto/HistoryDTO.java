package com.example.demo1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// 로그 기록 DTO
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryDTO {
    private int orderId;       // 주문 ID
    private int itemId;        // 아이템 ID
    private int quantity;      // 수량
    private String status;     // 상태 (예: 정상, 불량)
    private String date;       // 날짜
    private String affiliationCode;


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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAffiliationCode() {
        return affiliationCode;
    }

    public void setAffiliationCode(String affiliationCode) {
        this.affiliationCode = affiliationCode;
    }
}
