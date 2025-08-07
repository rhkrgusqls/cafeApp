package com.example.demo1.refresh;

import java.util.List;

// RefreshEvent.java - 이벤트 객체 (갱신 페이지 목록 등)
public class RefreshEvent {
    private List<String> pagesToRefresh;

    public RefreshEvent(List<String> pagesToRefresh) {
        this.pagesToRefresh = pagesToRefresh;
    }

    public List<String> getPagesToRefresh() {
        return pagesToRefresh;
    }
}

