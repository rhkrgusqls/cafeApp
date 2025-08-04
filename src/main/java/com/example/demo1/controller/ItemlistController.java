package com.example.demo1.controller;

import com.example.demo1.dto.ItemDTO;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

import java.net.URLConnection;

public class ItemlistController implements Initializable {

    @FXML private TableView<ItemDTO> tableView;
    @FXML private TableColumn<ItemDTO, Integer> colItemID;
    @FXML private TableColumn<ItemDTO, String> colName;
    @FXML private TableColumn<ItemDTO, String> colCategory;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colItemID.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));

        loadItemList();
    }

    private void loadItemList() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/items/list");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    InputStream is = conn.getInputStream();
                    ObjectMapper mapper = new ObjectMapper();
                    ItemDTO[] items = mapper.readValue(is, ItemDTO[].class);

                    Platform.runLater(() -> {
                        ObservableList<ItemDTO> data = FXCollections.observableArrayList(items);
                        tableView.setItems(data);
                    });
                } else {
                    Platform.runLater(() -> {
                        tableView.setPlaceholder(new Label("서버 오류: " + responseCode));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    tableView.setPlaceholder(new Label("불러오기 실패"));
                });
            }
        }).start();
    }
}
