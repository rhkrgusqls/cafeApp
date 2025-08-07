package com.example.demo1.dto;

public class ItemLimitViewDTO {
    private int itemId;
    private String itemName;
    private String category;
    private int realQuantity;     // 현재 수량
    private int limitQuantity;    // 제한 수량
    private boolean withinLimit;

    public ItemLimitViewDTO(int itemId, String itemName, String category, int realQuantity, int limitQuantity, boolean withinLimit) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.category = category;
        this.realQuantity = realQuantity;
        this.limitQuantity = limitQuantity;
        this.withinLimit = withinLimit;
    }

    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getCategory() { return category; }
    public int getRealQuantity() { return realQuantity; }
    public int getLimitQuantity() { return limitQuantity; }
    public boolean isWithinLimit() { return withinLimit; }
}

