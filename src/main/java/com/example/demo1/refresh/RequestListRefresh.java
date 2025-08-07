package com.example.demo1.refresh;

import com.example.demo1.controller.RequestlistController;

public class RequestListRefresh {

    private static RequestlistController controller;

    // 컨트롤러 등록
    public static void registerController(RequestlistController ctrl) {
        controller = ctrl;
    }

    // 새로고침 실행
    public static void refresh() {
        System.out.println("Refreshing request list");
        if (controller != null) {
            controller.loadOrders();
        }
    }
}
