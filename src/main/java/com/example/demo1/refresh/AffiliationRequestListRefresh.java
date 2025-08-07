package com.example.demo1.refresh;

import com.example.demo1.controller.AffiliationRequestListController;

public class AffiliationRequestListRefresh {

    private static AffiliationRequestListController controller;

    // 컨트롤러 등록
    public static void registerController(AffiliationRequestListController ctrl) {
        controller = ctrl;
    }

    // 새로고침 실행
    public static void refresh() {
        if (controller != null) {
            controller.loadStockHistory(); // 실제 새로고침 메서드 호출
            //System.out.println("affiliationRequestListRefresh refresh"); // 디버깅용
        }
    }
}
