package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.ItemDTO;
import com.example.demo1.dto.ItemLimitDTO;
import com.example.demo1.dto.ItemLimitViewDTO;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

    private void loadItemLimitData() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/itemStock/alarm/list");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                InputStream is = conn.getInputStream();
                ObjectMapper mapper = new ObjectMapper();

                // 서버에서 받아오는 DTO (realQuantity, quantity 등 포함)
                ItemLimitDTO[] itemLimits = mapper.readValue(is, ItemLimitDTO[].class);

                // ViewDTO로 변환
                var viewList = Arrays.stream(itemLimits)
                        .map(dto -> {
                            ItemDTO itemInfo = itemInfoMap.get(dto.getItemId());
                            String name = itemInfo != null ? itemInfo.getName() : "Unknown";
                            String category = itemInfo != null ? itemInfo.getCategory() : "Unknown";

                            return new ItemLimitViewDTO(
                                    dto.getItemId(),
                                    name,
                                    category,
                                    dto.getRealQuantity(),   // 실제 보유 수량
                                    dto.getQuantity(),       // 제한 수량
                                    dto.isWithinLimit()
                            );
                        })
                        .toList();

                javafx.application.Platform.runLater(() -> {
                    tableView.getItems().setAll(viewList);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
