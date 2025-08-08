package com.example.demo1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConsumptionDTO {

    @JsonProperty("totalQuantity")
    private int quantity;

    private int itemId;

    private String period;

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
}
