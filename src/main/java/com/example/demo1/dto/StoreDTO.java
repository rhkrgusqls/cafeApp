package com.example.demo1.dto;

public class StoreDTO {

    private String affiliationCode;
    private String storeName;
    private String alarmState; // wait나 re-review-needed 상태 확인

    public StoreDTO() {}

    public StoreDTO(String affiliationCode, String storeName) {
        this.affiliationCode = affiliationCode;
        this.storeName = storeName;
    }

    public String getAffiliationCode() {
        return affiliationCode;
    }

    public void setAffiliationCode(String affiliationCode) {
        this.affiliationCode = affiliationCode;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getAlarmState() {return alarmState;}

    public void setAlarmState(String alarmState) {this.alarmState = alarmState;}
}
