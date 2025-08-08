package com.example.demo1.dto;

public class ItemChangeDTO {

    private int itemId;
    private int quantity;
    private String changeType;
    private String affiliationCode;
    private String changeTime; // 서버는 Date지만 JSON은 문자열 형태로 전달됨

    // Jackson이 사용하기 위해 기본 생성자 필요
    public ItemChangeDTO() {}

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

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getAffiliationCode() {
        return affiliationCode;
    }

    public void setAffiliationCode(String affiliationCode) {
        this.affiliationCode = affiliationCode;
    }

    public String getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(String changeTime) {
        this.changeTime = changeTime;
    }
}
