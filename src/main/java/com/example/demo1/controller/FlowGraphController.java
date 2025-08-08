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
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

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
        flowChart.setAnimated(false); // 애니메이션 비활성화

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("재고량 변화");

        for (ItemChangeDTO log : logs) {
            String date = log.getChangeTime();  // x축 값
            int quantity = log.getQuantity();   // y축 값
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(date, quantity);
            series.getData().add(dataPoint);
        }
        flowChart.getData().add(series);
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Tooltip tooltip = new Tooltip("날짜: " + data.getXValue() + "\n변화량: " + data.getYValue());
                tooltip.setShowDelay(Duration.ZERO);
                tooltip.setHideDelay(Duration.ZERO);
                tooltip.setShowDuration(Duration.INDEFINITE);
                Tooltip.install(data.getNode(), tooltip);
            }
        });
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
