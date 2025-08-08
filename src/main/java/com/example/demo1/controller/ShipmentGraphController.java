package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.controller.util.ExcelUtil;
import com.example.demo1.dto.ShipmentDTO;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;

public class ShipmentGraphController {

    @FXML
    private LineChart<String, Number> shipmentChart;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 서버에서 JSON을 받아오는 메서드
    public void initialize() {
        int itemId = 1; // 필요시 동적으로 변경
        loadShipmentData(itemId);
    }

    private void loadShipmentData(int itemId) {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/logs/shipments?itemId=" + itemId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    List<ShipmentDTO> shipmentList = objectMapper.readValue(
                            in, new TypeReference<List<ShipmentDTO>>() {}
                    );
                    in.close();

                    Platform.runLater(() -> drawGraph(shipmentList));
                } else {
                    System.out.println("응답 실패: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void drawGraph(List<ShipmentDTO> shipmentList) {
        shipmentChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("출하량");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        for (ShipmentDTO dto : shipmentList) {
            String dateStr = formatter.format(dto.getShipmentTime());
            int quantity = dto.getQuantity();

            XYChart.Data<String, Number> data = new XYChart.Data<>(dateStr, quantity);
            series.getData().add(data);
        }

        shipmentChart.getData().add(series);

        CategoryAxis xAxis = (CategoryAxis) shipmentChart.getXAxis();
        xAxis.setTickLabelGap(0);                // 레이블 간격 줄이기
        shipmentChart.setAnimated(false);       // 점 위치 안정화

        // 데이터가 1개일 때 좌우 여백을 가짜 항목으로 추가해 중앙 정렬 유도
        if (xAxis.getCategories().size() == 1) {
            String realDate = xAxis.getCategories().get(0);
            xAxis.setCategories(FXCollections.observableArrayList(List.of(" ", realDate, " ")));
        }

        for (XYChart.Data<String, Number> data : series.getData()) {
            Tooltip tooltip = new Tooltip("날짜: " + data.getXValue() + "\n수량: " + data.getYValue());
            tooltip.setShowDelay(Duration.ZERO);
            tooltip.setHideDelay(Duration.ZERO);
            tooltip.setShowDuration(Duration.INDEFINITE);
            Tooltip.install(data.getNode(), tooltip);
        }
    }
}
