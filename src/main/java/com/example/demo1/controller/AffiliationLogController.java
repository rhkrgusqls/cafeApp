package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.HistoryDTO;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AffiliationLogController {

    @FXML private TableView<HistoryDTO> historyTable;
    @FXML private TableColumn<HistoryDTO, Integer> orderIdColumn;  // 주문 ID 컬럼
    @FXML private TableColumn<HistoryDTO, Integer> itemIdColumn;   // 아이템 ID 컬럼
    @FXML private TableColumn<HistoryDTO, Integer> quantityColumn; // 수량 컬럼
    @FXML private TableColumn<HistoryDTO, String> statusColumn;   // 상태 컬럼
    @FXML private TableColumn<HistoryDTO, String> dateColumn;     // 날짜 컬럼

    private String affiliationCode;

    // affiliationCode를 세팅하여 데이터 로딩
    public void setAffiliationContext(String affiliationCode) {
        this.affiliationCode = affiliationCode;
        loadStockHistory();
    }

    private void loadStockHistory() {
        new Thread(() -> {
            try {
                // 서버에서 재고 기록 조회
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/ordering/display");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());
                // 요청 바디에 affiliationCode 전달
                String jsonBody = String.format("{\"affiliationCode\":\"%s\"}", affiliationCode);
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    InputStream is = conn.getInputStream();
                    ObjectMapper mapper = new ObjectMapper();
                    HistoryDTO[] stockHistories = mapper.readValue(is, HistoryDTO[].class);

                    // UI 업데이트는 Platform.runLater()로 처리
                    javafx.application.Platform.runLater(() -> {
                        ObservableList<HistoryDTO> data = FXCollections.observableArrayList(stockHistories);

                        historyTable.setItems(data);
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        // 서버 응답 오류 시 처리
                        historyTable.setPlaceholder(new javafx.scene.control.Label("서버 오류: " + responseCode));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    // 오류 처리
                    historyTable.setPlaceholder(new javafx.scene.control.Label("불러오기 실패"));
                });
            }
        }).start();
    }
}
