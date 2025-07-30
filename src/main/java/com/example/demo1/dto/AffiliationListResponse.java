package com.example.demo1.dto;

//wrapper DTO(점포 리스트 수신용)

import java.util.List;

public class AffiliationListResponse {
    private List<StoreDTO> affiliationList;

    public List<StoreDTO> getAffiliationList() {
        return affiliationList;
    }

    public void setAffiliationList(List<StoreDTO> affiliationList) {
        this.affiliationList = affiliationList;
    }
}
