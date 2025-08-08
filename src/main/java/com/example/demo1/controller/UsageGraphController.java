package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.*;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class UsageGraphController {

    @FXML
    private BarChart<String, Number> usageChart;
    @FXML
    private MenuButton itemMenu;
    @FXML
    private MenuButton affiliationMenu;
    @FXML
    private RadioButton dayRadio;
    @FXML
    private RadioButton monthRadio;
    @FXML
    private RadioButton yearRadio;

    private final ObjectMapper mapper = new ObjectMapper();
    private String selectedAffiliation = null;
    private int selectedItemId = 1;
    private String selectedGroupType = "month"; // 기본값

    public void initialize() {
        setupToggleGroup();
        loadItemsFromServer();
        loadAffiliationsFromServer();
        loadGraphData();
    }

    private void setupToggleGroup() {
        ToggleGroup group = new ToggleGroup();
        dayRadio.setToggleGroup(group);
        monthRadio.setToggleGroup(group);
        yearRadio.setToggleGroup(group);
        monthRadio.setSelected(true); // 기본 선택

        group.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == dayRadio) { // day->week
                selectedGroupType = "day";
            } else if (newToggle == monthRadio) {
                selectedGroupType = "month";
            } else if (newToggle == yearRadio) {
                selectedGroupType = "year";
            }
            loadGraphData();
        });
    }

    private void loadItemsFromServer() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/items/list");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                InputStream is = conn.getInputStream();
                List<ItemDTO> itemList = mapper.readValue(is, new TypeReference<>() {
                });
                is.close();

                Platform.runLater(() -> {
                    itemMenu.getItems().clear();
                    for (ItemDTO item : itemList) {
                        if (!"available".equals(item.getState())) continue;

                        MenuItem menuItem = new MenuItem(item.getName());
                        menuItem.setOnAction(e -> {
                            selectedItemId = item.getItemId();
                            itemMenu.setText(item.getName());
                            loadGraphData();
                        });
                        itemMenu.getItems().add(menuItem);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadAffiliationsFromServer() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/affiliation/list");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                InputStream is = conn.getInputStream();
                AffiliationDTO wrapper = mapper.readValue(is, AffiliationDTO.class);
                is.close();

                Platform.runLater(() -> {
                    affiliationMenu.getItems().clear();
                    for (StoreDTO store : wrapper.getAffiliationList()) {
                        MenuItem menuItem = new MenuItem(store.getStoreName());
                        menuItem.setOnAction(e -> {
                            selectedAffiliation = store.getAffiliationCode();
                            affiliationMenu.setText(store.getStoreName());
                            loadGraphData();
                        });
                        affiliationMenu.getItems().add(menuItem);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadGraphData() {
        new Thread(() -> {
            try {
                String baseUrl = "http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/logs/consumptions";
                String aff = selectedAffiliation != null ? selectedAffiliation : "";

                String urlStr = String.format("%s?groupType=%s&affiliationCode=%s&itemId=%d",
                        baseUrl, selectedGroupType, aff, selectedItemId);

                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                InputStream is = conn.getInputStream();
                List<ConsumptionDTO> list = mapper.readValue(is, new TypeReference<>() {
                });
                is.close();

                Platform.runLater(() -> drawGraph(list));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void drawGraph(List<ConsumptionDTO> list) {
        usageChart.getData().clear();
        usageChart.setAnimated(false); // 애니메이션 비활성화

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("소모량");

        int previousQuantity = -1;

        for (ConsumptionDTO dto : list) {
            String label = dto.getPeriod(); // 예: "2025-08"
            int quantity = dto.getQuantity();

            XYChart.Data<String, Number> data = new XYChart.Data<>(label, quantity);
            series.getData().add(data);

            final int prev = previousQuantity; // for lambda
            final String period = label;       // for tooltip
            final int qty = quantity;

            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    String color;
                    if (prev == -1) {
                        color = "red";
                    } else if (qty > prev) {
                        color = "red";
                    } else if (qty < prev) {
                        color = "blue";
                    } else {
                        color = "gray";
                    }
                    newNode.setStyle("-fx-bar-fill: " + color + ";");

                    Tooltip tooltip = new Tooltip("기간: " + period + "\n사용량: " + qty);
                    tooltip.setShowDelay(Duration.ZERO);
                    tooltip.setHideDelay(Duration.ZERO);
                    tooltip.setShowDuration(Duration.INDEFINITE);
                    Tooltip.install(newNode, tooltip);
                }
            });

            previousQuantity = quantity;
        }
        usageChart.getData().add(series);
    }
}
