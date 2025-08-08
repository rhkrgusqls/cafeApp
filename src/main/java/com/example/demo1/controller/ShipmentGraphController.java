package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.*;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;

public class ShipmentGraphController {

    @FXML private LineChart<String, Number> shipmentChart;
    @FXML private MenuButton itemMenu;
    @FXML private MenuButton affiliationMenu;
    @FXML private RadioButton dateRadio;
    @FXML private RadioButton monthRadio;
    @FXML private RadioButton yearRadio;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private int selectedItemId = 1;
    private String selectedAffiliation = null;
    private String selectedGroupType = "day";

    public void initialize() {
        setupRadioButtons();
        loadItemsFromServer();
        loadAffiliationsFromServer();
        loadShipmentData();
    }

    private void setupRadioButtons() {
        dateRadio.setSelected(true);
        dateRadio.setOnAction(e -> {
            if (dateRadio.isSelected()) {
                monthRadio.setSelected(false);
                yearRadio.setSelected(false);
                selectedGroupType = "day";
                loadShipmentData();
            }
        });
        monthRadio.setOnAction(e -> {
            if (monthRadio.isSelected()) {
                dateRadio.setSelected(false);
                yearRadio.setSelected(false);
                selectedGroupType = "month";
                loadShipmentData();
            }
        });
        yearRadio.setOnAction(e -> {
            if (yearRadio.isSelected()) {
                dateRadio.setSelected(false);
                monthRadio.setSelected(false);
                selectedGroupType = "year";
                loadShipmentData();
            }
        });
    }

    private void loadShipmentData() {
        new Thread(() -> {
            try {
                String baseUrl = "http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/logs/shipments";
                String affiliation = selectedAffiliation != null ? selectedAffiliation : "";
                String urlStr = String.format("%s?itemId=%d&affiliationCode=%s&groupType=%s",
                        baseUrl, selectedItemId, affiliation, selectedGroupType);

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    List<ShipmentDTO> shipmentList = objectMapper.readValue(in, new TypeReference<>() {});
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
        xAxis.setTickLabelGap(0);
        shipmentChart.setAnimated(false);

        if (xAxis.getCategories().size() == 1) {
            String realDate = xAxis.getCategories().get(0);

            // 중복 방지: 이미 해당 항목이 있다면 setCategories 호출 안 함
            if (!xAxis.getCategories().contains(" ") && !xAxis.getCategories().contains(realDate)) {
                xAxis.setCategories(FXCollections.observableArrayList(List.of(" ", realDate, " ")));
            }
        }

        for (XYChart.Data<String, Number> data : series.getData()) {
            Tooltip tooltip = new Tooltip("날짜: " + data.getXValue() + "\n수량: " + data.getYValue());
            tooltip.setShowDelay(Duration.ZERO);
            tooltip.setHideDelay(Duration.ZERO);
            tooltip.setShowDuration(Duration.INDEFINITE);
            Tooltip.install(data.getNode(), tooltip);
        }
    }

    private void loadItemsFromServer() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/items/list");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                List<ItemDTO> items = objectMapper.readValue(conn.getInputStream(), new TypeReference<>() {});

                Platform.runLater(() -> {
                    itemMenu.getItems().clear();
                    for (ItemDTO item : items) {
                        if (!"available".equalsIgnoreCase(item.getState())) continue;  // available만 필터링

                        MenuItem menuItem = new MenuItem(item.getName() + " (" + item.getItemId() + ")");
                        menuItem.setOnAction(e -> {
                            selectedItemId = item.getItemId();
                            itemMenu.setText(item.getName());
                            loadShipmentData();
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

                AffiliationDTO responseDTO = objectMapper.readValue(conn.getInputStream(), AffiliationDTO.class);
                List<StoreDTO> affiliations = responseDTO.getAffiliationList();

                Platform.runLater(() -> {
                    affiliationMenu.getItems().clear();
                    for (StoreDTO store : affiliations) {
                        String code = store.getAffiliationCode();
                        String storeName = store.getStoreName();

                        MenuItem menuItem = new MenuItem(storeName + " (" + code + ")");
                        menuItem.setOnAction(e -> {
                            selectedAffiliation = code;
                            affiliationMenu.setText(storeName);
                            loadShipmentData();
                        });

                        affiliationMenu.getItems().add(menuItem);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
