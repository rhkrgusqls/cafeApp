package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.ItemDTO;
import com.example.demo1.dto.ItemLimitDTO;
import com.example.demo1.dto.ItemLimitViewDTO;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TotalItemListController implements Initializable {
    private Map<Integer, ItemDTO> itemInfoMap;

    @FXML private TableView<ItemLimitViewDTO> tableView;
    @FXML private TableColumn<ItemLimitViewDTO, Integer> colItemId;
    @FXML private TableColumn<ItemLimitViewDTO, String> colItemName;
    @FXML private TableColumn<ItemLimitViewDTO, String> colCategory;
    @FXML private TableColumn<ItemLimitViewDTO, Integer> colRealQty;
    @FXML private TableColumn<ItemLimitViewDTO, Integer> colLimitQty;
    @FXML private TableColumn<ItemLimitViewDTO, Boolean> colWithinLimit;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        itemInfoMap = fetchItemInfoMap();

        colItemId.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colRealQty.setCellValueFactory(new PropertyValueFactory<>("realQuantity"));
        colLimitQty.setCellValueFactory(new PropertyValueFactory<>("limitQuantity"));
        colWithinLimit.setCellValueFactory(new PropertyValueFactory<>("withinLimit"));

        loadItemList();
        loadItemLimitData();
    }

    private Map<Integer, ItemDTO> fetchItemInfoMap() {
        try {
            URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/items/list");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

            InputStream is = conn.getInputStream();
            ObjectMapper mapper = new ObjectMapper();

            ItemDTO[] items = mapper.readValue(is, ItemDTO[].class);

            return Arrays.stream(items)
                    .collect(Collectors.toMap(ItemDTO::getItemId, Function.identity()));

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of();  // 빈 맵 반환
        }
    }

    private void loadItemList() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/itemStock/alarm/list");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                int responseCode = conn.getResponseCode();
                InputStream is = conn.getInputStream();
                byte[] bytes = is.readAllBytes();

                if (bytes.length == 0) {
                    Platform.runLater(() -> {
                        tableView.getItems().clear();
                    });
                    return;
                }

                ObjectMapper mapper = new ObjectMapper();
                ItemLimitDTO[] itemLimits = mapper.readValue(bytes, ItemLimitDTO[].class);

                var viewList = Arrays.stream(itemLimits)
                        .map(dto -> {
                            ItemDTO itemInfo = itemInfoMap.get(dto.getItemId());
                            String name = itemInfo != null ? itemInfo.getName() : "Unknown";
                            String category = itemInfo != null ? itemInfo.getCategory() : "Unknown";

                            return new ItemLimitViewDTO(
                                    dto.getItemId(),
                                    name,
                                    category,
                                    dto.getRealQuantity(),
                                    dto.getQuantity(),
                                    dto.isWithinLimit()
                            );
                        })
                        .toList();

                Platform.runLater(() -> {
                    tableView.getItems().setAll(viewList);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    tableView.setPlaceholder(new Label("불러오기 실패"));
                });
            }
        }).start();
    }


    private void loadItemLimitData() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/itemStock/alarm/list");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                InputStream is = conn.getInputStream();
                byte[] bytes = is.readAllBytes(); // 응답 전체 읽기

                if (bytes.length == 0) {
                    return; // 응답 없음이면 아무 일도 안 함
                }

                ObjectMapper mapper = new ObjectMapper();
                ItemLimitDTO[] itemLimits = mapper.readValue(bytes, ItemLimitDTO[].class);

                var lowStockList = Arrays.stream(itemLimits)
                        .filter(ItemLimitDTO::isWithinLimit) // 부족한 재고만 필터링
                        .map(dto -> {
                            ItemDTO itemInfo = itemInfoMap.get(dto.getItemId());
                            return itemInfo != null ? itemInfo.getName() : "Unknown";
                        })
                        .toList();

                if (!lowStockList.isEmpty()) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("재고 부족 경고");
                        alert.setHeaderText(null);
                        alert.setContentText(String.join(", ", lowStockList) + " 재고가 부족합니다.\n재고 요청을 고려해보세요.");
                        alert.showAndWait();
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR, "재고 정보 조회 실패").showAndWait();
                });
            }
        }).start();
    }

}
