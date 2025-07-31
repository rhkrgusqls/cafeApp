package com.example.demo1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // 서버에서 json으로 받아올 때 불필요 필드 무시
public class StuffDTO {
    private int stockId;
    private int itemId;
    private String itemName;
    private String itemCategory;
    private int quantity;
    private String expireDate;
    private String receivedDate;
    private String status;
    private String affiliationCode;
    private String mode; // 추후 모드 버튼용

    public StuffDTO() {
    }

    public StuffDTO(int stockId, int itemId, String itemName, String itemCategory,
                    int quantity, String expireDate, String receivedDate,
                    String status, String affiliationCode, String mode) {
        this.stockId = stockId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemCategory = itemCategory;
        this.quantity = quantity;
        this.expireDate = expireDate;
        this.receivedDate = receivedDate;
        this.status = status;
        this.affiliationCode = affiliationCode;
        this.mode = mode;
    }

    public int getStockId() {return stockId;}

    public void setStockId(int stockId) {this.stockId = stockId;}

    public int getItemId() {return itemId;}

    public void setItemId(int itemId) {this.itemId = itemId;}

    public String getItemName() {return itemName;}

    public void setItemName(String itemName) {this.itemName = itemName;}

    public String getItemCategory() {return itemCategory;}

    public void setItemCategory(String itemCategory) {this.itemCategory = itemCategory;}

    public int getQuantity() {return quantity;}

    public void setQuantity(int quantity) {this.quantity = quantity;}

    public String getExpireDate() {return expireDate;}

    public void setExpireDate(String expireDate) {this.expireDate = expireDate;}

    public String getReceivedDate() {return receivedDate;}

    public void setReceivedDate(String receivedDate) {this.receivedDate = receivedDate;}

    public String getStatus() {return status;}

    public void setStatus(String status) {this.status = status;}

    public String getAffiliationCode() {return affiliationCode;}

    public void setAffiliationCode(String affiliationCode) {this.affiliationCode = affiliationCode;}

    public String getMode() {return mode;}

    public void setMode(String mode) {this.mode = mode;}
}
