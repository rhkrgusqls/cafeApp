package com.example.demo1.dto;

// 서버에서 응답 받기용 DTO
public class ItemLimitDTO {
    private int itemId;
    private String affiliationCode;
    private int realQuantity;
    private int quantity;
    private boolean withinLimit;

    public ItemLimitDTO() {}  // 기본 생성자 (반드시 필요)

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getAffiliationCode() { return affiliationCode; }
    public void setAffiliationCode(String affiliationCode) { this.affiliationCode = affiliationCode; }

    public int getRealQuantity() { return realQuantity; }
    public void setRealQuantity(int realQuantity) { this.realQuantity = realQuantity; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public boolean isWithinLimit() { return withinLimit; }
    public void setWithinLimit(boolean withinLimit) { this.withinLimit = withinLimit; }
}
