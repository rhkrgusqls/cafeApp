package com.example.demo1.dto;

import java.util.Date;

public class ShipmentDTO {
    private int itemId;
    private int quantity;
    private String targetAffiliationCode;
    private Date shipmentTime;

    public int getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getTargetAffiliationCode() {
        return targetAffiliationCode;
    }

    public Date getShipmentTime() {
        return shipmentTime;
    }
}
