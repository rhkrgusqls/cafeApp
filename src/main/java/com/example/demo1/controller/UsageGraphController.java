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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class UsageGraphController {

    @FXML private BarChart<String, Number> usageChart;
    @FXML private MenuButton itemMenu;
    @FXML private MenuButton affiliationMenu;
    @FXML private RadioButton weekRadio;
    @FXML private RadioButton monthRadio;
    @FXML private RadioButton yearRadio;

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
        weekRadio.setToggleGroup(group);
        monthRadio.setToggleGroup(group);
        yearRadio.setToggleGroup(group);
        monthRadio.setSelected(true); // 기본 선택

        group.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == weekRadio) {
                selectedGroupType = "week";
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
                List<ItemDTO> itemList = mapper.readValue(is, new TypeReference<>() {});
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
                List<ConsumptionDTO> list = mapper.readValue(is, new TypeReference<>() {});
                is.close();

                Platform.runLater(() -> drawGraph(list));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void drawGraph(List<ConsumptionDTO> list) {
        usageChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("소모량");
        usageChart.setAnimated(false); // 애니메이션 키면 그래프가 이상해보임

        for (ConsumptionDTO dto : list) {
            String label = dto.getPeriod(); // 사용된 기간 단위 (예: "2025-08")
            XYChart.Data<String, Number> data = new XYChart.Data<>(label, dto.getQuantity());
            series.getData().add(data);
        }

        usageChart.getData().add(series);
    }
}
