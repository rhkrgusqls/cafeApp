package com.example.demo1.controller;

import com.example.demo1.dto.ItemDTO;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ItemInfoController {

    @FXML private TextField itemIdField;
    @FXML private TextField nameField;
    @FXML private TextField categoryField;

    public void setItemId(int itemId) {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/items/list");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                InputStream is = conn.getInputStream();
                ObjectMapper mapper = new ObjectMapper();

                List<ItemDTO> itemList = mapper.readValue(is, new TypeReference<List<ItemDTO>>() {});
                for (ItemDTO item : itemList) {
                    if (item.getItemId() == itemId) {
                        Platform.runLater(() -> {
                            itemIdField.setText(String.valueOf(item.getItemId()));
                            nameField.setText(item.getName());
                            categoryField.setText(item.getCategory());
                        });
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
