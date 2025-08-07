package com.example.demo1.refresh;

import com.example.demo1.controller.TotalItemListController;

// 전체 재고 페이지(수량제한 포함) 새로고침
public class TotalItemListRefresh {

    private static TotalItemListController controller;

    // 컨트롤러 등록
    public static void registerController(TotalItemListController ctrl) {
        controller = ctrl;
    }

    // 새로고침 메서드
    public static void refresh() {
        if (controller != null) {
            controller.loadItemLimitData();
        }
    }
}
