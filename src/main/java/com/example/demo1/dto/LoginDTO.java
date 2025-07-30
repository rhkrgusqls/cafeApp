package com.example.demo1.dto;

public class LoginDTO {
    private String affiliationCode;
    private String password;

    public LoginDTO() {} // 기본 생성자 필수

    public LoginDTO(String affiliationCode, String password) {
        this.affiliationCode = affiliationCode;
        this.password = password;
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
}
