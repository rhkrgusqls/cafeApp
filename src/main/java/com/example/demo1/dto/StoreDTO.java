package com.example.demo1.dto;

public class StoreDTO {

    private String affiliationCode;
    private String storeName;

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
}
