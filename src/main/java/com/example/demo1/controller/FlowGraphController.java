package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.ItemChangeDTO;
import com.example.demo1.dto.ItemDTO;
import com.example.demo1.dto.AffiliationDTO;
import com.example.demo1.dto.StoreDTO;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FlowGraphController {

    @FXML private LineChart<String, Number> flowChart;
    @FXML private MenuButton itemMenu;
    @FXML private MenuButton affiliationMenu;

    private final ObjectMapper mapper = new ObjectMapper();
    private String selectedAffiliation = null;
    private int selectedItemId = 1;

    public void initialize() {
        fetchItemList();
        fetchAffiliationList();
        fetchAndDrawChart(); // 초기 로드
    }

    private void fetchItemList() {
        new Thread(() -> {
            try {
                String api = String.format("http://%s:%s/items/list", ConfigLoader.getIp(), ConfigLoader.getPort());
                HttpURLConnection conn = openGetConnection(api);

                List<ItemDTO> items = mapper.readValue(conn.getInputStream(), new TypeReference<>() {});
                Platform.runLater(() -> populateItemMenu(items));
            } catch (Exception e) {
                logError("Failed to load items", e);
            }
        }).start();
    }

    private void fetchAffiliationList() {
        new Thread(() -> {
            try {
                String api = String.format("http://%s:%s/affiliation/list", ConfigLoader.getIp(), ConfigLoader.getPort());
                HttpURLConnection conn = openGetConnection(api);

                AffiliationDTO wrapper = mapper.readValue(conn.getInputStream(), AffiliationDTO.class);
                Platform.runLater(() -> populateAffiliationMenu(wrapper.getAffiliationList()));
            } catch (Exception e) {
                logError("Failed to load affiliations", e);
            }
        }).start();
    }

    private void fetchAndDrawChart() {
        new Thread(() -> {
            try {
                String affiliation = selectedAffiliation != null ? selectedAffiliation : "";
                String api = String.format("http://%s:%s/logs/changes?affiliationCode=%s&itemId=%d",
                        ConfigLoader.getIp(), ConfigLoader.getPort(), affiliation, selectedItemId);

                HttpURLConnection conn = openGetConnection(api);
                List<ItemChangeDTO> logs = mapper.readValue(conn.getInputStream(), new TypeReference<>() {});
                Platform.runLater(() -> drawChart(logs));
            } catch (Exception e) {
                logError("Failed to fetch chart data", e);
            }
        }).start();
    }

    private void populateItemMenu(List<ItemDTO> items) {
        itemMenu.getItems().clear();
        for (ItemDTO item : items) {
            if (!"available".equals(item.getState())) continue;

            MenuItem menuItem = new MenuItem(item.getName());
            menuItem.setOnAction(e -> {
                selectedItemId = item.getItemId();
                itemMenu.setText(item.getName());
                fetchAndDrawChart();
            });
            itemMenu.getItems().add(menuItem);
        }
    }

    private void populateAffiliationMenu(List<StoreDTO> stores) {
        affiliationMenu.getItems().clear();
        for (StoreDTO store : stores) {
            MenuItem menuItem = new MenuItem(store.getStoreName());
            menuItem.setOnAction(e -> {
                selectedAffiliation = store.getAffiliationCode();
                affiliationMenu.setText(store.getStoreName());
                fetchAndDrawChart();
            });
            affiliationMenu.getItems().add(menuItem);
        }
    }

    private void drawChart(List<ItemChangeDTO> logs) {
        flowChart.getData().clear();
        flowChart.setAnimated(false);

        if (logs.isEmpty()) return;

        // 누적량 계산용 변수
        int cumulativeQuantity = 0;

        // 이전 누적량, 날짜, 타입 초기화
        String prevDate = null;
        int prevQuantity = 0;
        String prevType = null;

        // 여러 series를 저장할 리스트
        List<XYChart.Series<String, Number>> seriesList = new ArrayList<>();

        // 임시 series 생성 및 색상 적용 함수
        XYChart.Series<String, Number> currentSeries = new XYChart.Series<>();
        currentSeries.setName("재고량 변화");

        // changeType 별 색상 맵 (CSS 스타일 문자열)
        Map<String, String> colorMap = Map.of(
                "INBOUND", "black",
                "DISPOSAL", "green",
                "USAGE", "blue",
                "SHIPMENT", "purple",
                "MODIFY", "yellow"
        );

        for (int i = 0; i < logs.size(); i++) {
            ItemChangeDTO log = logs.get(i);
            cumulativeQuantity += log.getQuantity();
            String date = log.getChangeTime();

            if (i == 0) {
                // 첫 데이터는 무조건 추가
                XYChart.Data<String, Number> point = new XYChart.Data<>(date, cumulativeQuantity);
                currentSeries.getData().add(point);
            } else {
                // 이전 타입과 현재 타입이 다르면 새로운 시리즈 생성
                if (!log.getChangeType().equals(prevType)) {
                    // 기존 series 저장
                    if (!currentSeries.getData().isEmpty()) {
                        seriesList.add(currentSeries);
                    }
                    // 새로운 series 생성
                    currentSeries = new XYChart.Series<>();
                    currentSeries.setName(log.getChangeType());
                }

                // 이전 누적량 (x1, y1)
                XYChart.Data<String, Number> prevPoint = new XYChart.Data<>(prevDate, prevQuantity);
                // 현재 누적량 (x2, y2)
                XYChart.Data<String, Number> currPoint = new XYChart.Data<>(date, cumulativeQuantity);

                // 시리즈에 이전점(시작) 없으면 추가 (처음 점)
                if (currentSeries.getData().isEmpty()) {
                    currentSeries.getData().add(prevPoint);
                }
                // 현재점 추가
                currentSeries.getData().add(currPoint);
            }

            prevDate = date;
            prevQuantity = cumulativeQuantity;
            prevType = log.getChangeType();
        }

        // 마지막 시리즈 추가
        if (!currentSeries.getData().isEmpty()) {
            seriesList.add(currentSeries);
        }

        // flowChart에 시리즈들 추가 및 색상 적용
        for (XYChart.Series<String, Number> s : seriesList) {
            flowChart.getData().add(s);

            // 스타일 적용
            String typeName = s.getName();
            String color = colorMap.getOrDefault(typeName, "black");

            // 시리즈 노드 스타일 (선 색상)
            // 시리즈의 노드는 항상 첫 렌더링 후 생성되므로 runLater로 스타일 적용
            Platform.runLater(() -> {
                Node line = s.getNode().lookup(".chart-series-line");
                if (line != null) {
                    line.setStyle("-fx-stroke: " + color + ";");
                }

                for (XYChart.Data<String, Number> data : s.getData()) {
                    // 노드 색상 변경 (점 색상)
                    if (data.getNode() != null) {
                        data.getNode().setStyle("-fx-background-color: " + color + ", white;");
                        // or "-fx-background-radius: 5px;" 등도 추가 가능
                    }

                    Tooltip tooltip = new Tooltip("날짜: " + data.getXValue() + "\n누적 변화량: " + data.getYValue() + "\n타입: " + typeName);
                    tooltip.setShowDelay(Duration.ZERO);
                    tooltip.setHideDelay(Duration.ZERO);
                    tooltip.setShowDuration(Duration.INDEFINITE);
                    Tooltip.install(data.getNode(), tooltip);
                }
            });
        }
    }


    private HttpURLConnection openGetConnection(String apiUrl) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Cookie", Cookie.getSessionCookie());
        return conn;
    }

    private void logError(String message, Exception e) {
        System.err.println("[FlowGraphController] " + message);
        e.printStackTrace();
    }
}
