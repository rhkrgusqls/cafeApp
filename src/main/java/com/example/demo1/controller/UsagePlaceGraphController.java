package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.AffiliationDTO;
import com.example.demo1.dto.ItemDTO;
import com.example.demo1.dto.StoreDTO;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class UsagePlaceGraphController {

    @FXML private MenuButton itemMenu;
    @FXML private MenuButton affiliationMenu;
    @FXML private DatePicker monthPicker;
    @FXML private PieChart usagePlaceChart;

    // "값이 없습니다" 시각 요소
    private final Text noDataText = new Text("값이 없습니다");
    private final Circle grayCircle = new Circle(150, Color.LIGHTGRAY);

    private final ObjectMapper mapper = new ObjectMapper();
    private int selectedItemId = 1;
    private String selectedAffiliationCode = null;

    public void initialize() {
        setupNoDataMessage();
        setupMonthPicker();
        loadItems();
        loadAffiliations();
        monthPicker.setValue(LocalDate.now());
        fetchAndRenderChart();
    }

    // 월 선택기 설정
    private void setupMonthPicker() {
        monthPicker.setConverter(new StringConverter<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

            @Override
            public String toString(LocalDate date) {
                return date != null ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return LocalDate.parse(string + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        });

        monthPicker.valueProperty().addListener((obs, oldVal, newVal) -> fetchAndRenderChart());
    }

    private void loadItems() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/items/list");
                HttpURLConnection conn = openGetConnection(url);
                List<ItemDTO> items = mapper.readValue(conn.getInputStream(), new TypeReference<>() {});

                Platform.runLater(() -> {
                    itemMenu.getItems().clear();
                    for (ItemDTO item : items) {
                        if (!"available".equals(item.getState())) continue;

                        MenuItem mi = new MenuItem(item.getName());
                        mi.setOnAction(e -> {
                            selectedItemId = item.getItemId();
                            itemMenu.setText(item.getName());
                            fetchAndRenderChart();
                        });
                        itemMenu.getItems().add(mi);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadAffiliations() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/affiliation/list");
                HttpURLConnection conn = openGetConnection(url);
                AffiliationDTO wrapper = mapper.readValue(conn.getInputStream(), AffiliationDTO.class);

                Platform.runLater(() -> {
                    affiliationMenu.getItems().clear();
                    for (StoreDTO store : wrapper.getAffiliationList()) {
                        MenuItem mi = new MenuItem(store.getStoreName());
                        mi.setOnAction(e -> {
                            selectedAffiliationCode = store.getAffiliationCode();
                            affiliationMenu.setText(store.getStoreName());
                            fetchAndRenderChart();
                        });
                        affiliationMenu.getItems().add(mi);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void fetchAndRenderChart() {
        new Thread(() -> {
            try {
                String month = monthPicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                String urlStr = String.format("http://%s:%s/logs/inventory-breakdown?month=%s&itemId=%d",
                        ConfigLoader.getIp(), ConfigLoader.getPort(), month, selectedItemId);

                if (selectedAffiliationCode != null) {
                    urlStr += "&affiliationCode=" + selectedAffiliationCode;
                }

                HttpURLConnection conn = openGetConnection(new URL(urlStr));
                List<Map<String, Integer>> result = mapper.readValue(conn.getInputStream(), new TypeReference<>() {});

                Platform.runLater(() -> renderPieChart(result));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void renderPieChart(List<Map<String, Integer>> data) {
        usagePlaceChart.getData().clear();

        Map<String, String> colorMap = Map.of(
                "INBOUND", "#000000",   // 검정
                "DISPOSAL", "#228B22",  // 초록
                "USAGE", "#1E90FF",     // 파랑
                "SHIPMENT", "#800080",  // 보라
                "MODIFY", "#FFD700",    // 노랑
                "LEFTOVER", "#808080"   // 회색
        );

        if (data == null || data.isEmpty()) {
            grayCircle.setVisible(true);
            noDataText.setVisible(true);
            return;
        }

        grayCircle.setVisible(false);
        noDataText.setVisible(false);

        // 1. 데이터 추가만 먼저 함
        for (Map<String, Integer> entry : data) {
            for (Map.Entry<String, Integer> e : entry.entrySet()) {
                String label = e.getKey();
                int value = e.getValue();
                usagePlaceChart.getData().add(new PieChart.Data(label, value));
            }
        }

        // 2. 색상과 툴팁은 노드 렌더링 완료 후 적용 (이게 핵심)
        Platform.runLater(() -> {
            for (PieChart.Data slice : usagePlaceChart.getData()) {
                String label = slice.getName();
                double value = slice.getPieValue();

                if (colorMap.containsKey(label)) {
                    slice.getNode().setStyle("-fx-pie-color: " + colorMap.get(label) + ";");
                } else {
                    slice.getNode().setStyle("-fx-pie-color: #000000;"); // 기본 검정색
                }

                Tooltip tooltip = new Tooltip(label + ": " + (int)value);
                tooltip.setStyle("-fx-font-size: 14px;");
                tooltip.setShowDelay(Duration.ZERO);
                tooltip.setHideDelay(Duration.ZERO);
                tooltip.setShowDuration(Duration.INDEFINITE);
                Tooltip.install(slice.getNode(), tooltip);
            }
        });
    }

    private void setupNoDataMessage() {
        noDataText.setVisible(false);
        noDataText.setStyle("-fx-font-size: 24px; -fx-fill: #555;");
        noDataText.setLayoutX(usagePlaceChart.getLayoutX() + usagePlaceChart.getPrefWidth() / 2 - 60);
        noDataText.setLayoutY(usagePlaceChart.getLayoutY() + usagePlaceChart.getPrefHeight() / 2);

        grayCircle.setVisible(false);
        grayCircle.setLayoutX(usagePlaceChart.getLayoutX() + usagePlaceChart.getPrefWidth() / 2);
        grayCircle.setLayoutY(usagePlaceChart.getLayoutY() + usagePlaceChart.getPrefHeight() / 2);

        ((AnchorPane) usagePlaceChart.getParent()).getChildren().addAll(grayCircle, noDataText);
    }

    private HttpURLConnection openGetConnection(URL url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Cookie", Cookie.getSessionCookie());
        return conn;
    }
}
