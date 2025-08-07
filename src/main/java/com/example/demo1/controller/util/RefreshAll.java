package com.example.demo1.controller.util;

import com.example.demo1.refresh.*;

public class RefreshAll {
    public RefreshAll() {
        AffiliationRequestListRefresh.refresh();
        ItemListRefresh.refresh();
        RequestListRefresh.refresh();
        StoreManagementRefresh.refresh();
        TotalItemListRefresh.refresh();
    }
}
