package com.example.demo1.controller;

import com.example.demo1.dto.ItemDTO;
import com.example.demo1.dto.OrderDTO;
import com.example.demo1.dto.StoreDTO;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
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

    private String loginAffiliationCode;

    public void setLoginAffiliationCode(String loginAffiliationCode) {
        this.loginAffiliationCode = loginAffiliationCode;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colItemID.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));

        tableView.setRowFactory(tv -> {
            TableRow<ItemDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    ItemDTO selectedItem = row.getItem();
                    openRequestFormWithItemId(selectedItem.getItemId());
                }
            });
            return row;
        });

        loadItemList();
    }

    private void openRequestFormWithItemId(int itemId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/requestForm.fxml"));
            Parent root = loader.load();

            RequestFormController controller = loader.getController();
            controller.setItemId(itemId); // itemId 전달
            controller.setLoginAffiliationCode(loginAffiliationCode); // 적절히 주입

            Stage stage = new Stage();
            stage.setTitle("재고 요청");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void openItemRequestList(OrderDTO itemId, OrderDTO quantity, OrderDTO affiliation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/itemlist.fxml"));
            Parent root = loader.load();

            ItemlistController controller = loader.getController();
            controller.setLoginAffiliationCode(loginAffiliationCode); // 꼭 넣어줘야 함

            Stage stage = new Stage();
            stage.setTitle("상품 리스트");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
