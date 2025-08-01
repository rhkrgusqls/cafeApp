package com.example.demo1.dto;

public class OrderDTO {
    private int orderId;
    private int itemId;
    private int quantity;
    private String affiliationCode;
    private String state;
    private String orderDate;

    // Getters & Setters (Jackson용 기본 생성자도 있어야 함)
    public OrderDTO() {}

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getAffiliationCode() { return affiliationCode; }
    public void setAffiliationCode(String affiliationCode) { this.affiliationCode = affiliationCode; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
}

