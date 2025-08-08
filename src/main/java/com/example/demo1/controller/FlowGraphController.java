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
import java.util.LinkedHashMap;
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
                Platform.runLater(() -> drawChart(new ArrayList<>()));
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
                initialize();
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
                initialize();
            });
            affiliationMenu.getItems().add(menuItem);
        }
    }

    private void drawChart(List<ItemChangeDTO> logs) {
        flowChart.getData().clear();
        flowChart.setAnimated(false);
        flowChart.layout();
        if (logs.isEmpty()) return;

        int cumulativeQuantity = 0;
        String prevDate = null;
        int prevQuantity = 0;
        String prevType = null;

        // 색상 정의
        Map<String, String> colorMap = Map.of(
                "INBOUND", "black",
                "DISPOSAL", "green",
                "USAGE", "blue",
                "SHIPMENT", "purple",
                "MODIFY", "yellow"
        );

        List<XYChart.Series<String, Number>> seriesList = new ArrayList<>();
        XYChart.Series<String, Number> currentSeries = new XYChart.Series<>();

        for (int i = 0; i < logs.size(); i++) {
            ItemChangeDTO log = logs.get(i);
            cumulativeQuantity += log.getQuantity();
            String date = log.getChangeTime();
            String type = log.getChangeType();

            if (i == 0) {
                // 첫 데이터는 무조건 새 시리즈 생성
                currentSeries = new XYChart.Series<>();
                currentSeries.setName(type);
                currentSeries.getData().add(new XYChart.Data<>(date, cumulativeQuantity));
            } else {
                if (!type.equals(prevType)) {
                    // 타입이 바뀌었으면 기존 시리즈 저장 후 새 시리즈 시작
                    seriesList.add(currentSeries);

                    currentSeries = new XYChart.Series<>();
                    currentSeries.setName(type);

                    // 이전 지점 추가 (시작점)
                    currentSeries.getData().add(new XYChart.Data<>(prevDate, prevQuantity));
                }

                currentSeries.getData().add(new XYChart.Data<>(date, cumulativeQuantity));
            }

            prevDate = date;
            prevQuantity = cumulativeQuantity;
            prevType = type;
        }

        // 마지막 시리즈 추가
        if (!currentSeries.getData().isEmpty()) {
            seriesList.add(currentSeries);
        }

        // 차트에 시리즈 추가 및 스타일 지정
        for (XYChart.Series<String, Number> series : seriesList) {
            String typeName = series.getName();
            String color = colorMap.getOrDefault(typeName, "black");

            flowChart.getData().add(series);

            Platform.runLater(() -> {
                Node line = series.getNode().lookup(".chart-series-line");
                if (line != null) {
                    line.setStyle("-fx-stroke: " + color + ";");
                }

                for (XYChart.Data<String, Number> data : series.getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        node.setStyle("-fx-background-color: " + color + ", white;");
                    }

                    Tooltip tooltip = new Tooltip("날짜: " + data.getXValue()
                            + "\n누적 변화량: " + data.getYValue()
                            + "\n타입: " + typeName);
                    tooltip.setShowDelay(Duration.ZERO);
                    tooltip.setHideDelay(Duration.ZERO);
                    tooltip.setShowDuration(Duration.INDEFINITE);
                    Tooltip.install(node, tooltip);
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
