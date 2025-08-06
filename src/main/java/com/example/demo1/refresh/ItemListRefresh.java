package com.example.demo1.refresh;

import com.example.demo1.controller.ItemlistController;

public class ItemListRefresh {

    private static ItemlistController controller;

    // 컨트롤러 등록
    public static void registerController(ItemlistController ctrl) {
        controller = ctrl;
    }

    // 새로고침 실행
    public static void refresh() {
        if (controller != null) {
            controller.loadItemList();
        }
    }
}
