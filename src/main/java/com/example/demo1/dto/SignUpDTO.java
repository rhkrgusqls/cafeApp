package com.example.demo1.dto;

public class SignUpDTO {
    private String affiliationCode;
    private String password;
    private String storeName;

    public SignUpDTO() {}

    public SignUpDTO(String affiliationCode, String password, String storeName) {
        this.affiliationCode = affiliationCode;
        this.password = password;
        this.storeName = storeName;
    }

    public String getAffiliationCode() {
        return affiliationCode;
    }

    public void setAffiliationCode(String affiliationCode) {
        this.affiliationCode = affiliationCode;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
