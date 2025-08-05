package com.example.demo1.dto;

import java.sql.Timestamp;

public class PriItemStockDTO {
        private int itemId;
        private int quantity;
        private Timestamp expireDate;

        public int getItemId() { return itemId; }
        public void setItemId(int itemId) { this.itemId = itemId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public Timestamp getExpireDate() { return expireDate; }
        public void setExpireDate(Timestamp expireDate) { this.expireDate = expireDate; }
}
