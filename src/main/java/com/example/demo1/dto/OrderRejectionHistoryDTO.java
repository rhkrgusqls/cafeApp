package com.example.demo1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRejectionHistoryDTO {
    //private int rejectionId; // 서버 응답에 있는 필드
    private int orderId;
    private String rejectionReason;
    private String rejectionTime;
    private String notes;

    // Getter & Setter
    public int getOrderId() {
        return orderId;
    }
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getRejectionTime() {
        return rejectionTime;
    }
    public void setRejectionTime(String rejectionTime) {
        this.rejectionTime = rejectionTime;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

//    public int getRejectionId() {
//        return rejectionId;
//    }
//
//    public void setRejectionId(int rejectionId) {
//        this.rejectionId = rejectionId;
//    }
}
