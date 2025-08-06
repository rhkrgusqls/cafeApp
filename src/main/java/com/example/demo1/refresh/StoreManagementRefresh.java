package com.example.demo1.refresh;

import com.example.demo1.controller.StoreManagementController;

public class StoreManagementRefresh {

    private static StoreManagementController controller;

    // 컨트롤러 등록
    public static void registerController(StoreManagementController ctrl) {
        controller = ctrl;
    }

    // 새로고침 메서드
    public static void refresh() {
        if (controller != null) {
            controller.loadStoreList(); // or controller.loadAllStock();
        }
    }
}
