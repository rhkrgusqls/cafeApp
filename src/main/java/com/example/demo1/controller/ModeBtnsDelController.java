package com.example.demo1.controller;

import com.example.demo1.dto.StuffDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ModeBtnsDelController {

    @FXML private Button disposeBtn;

    private TableCell<?, ?> cell;

    // 초기화 메서드
    public void init(TableCell<?, ?> cell) {
        this.cell = cell;
    }

    @FXML
    public void initialize() {
        disposeBtn.setOnAction(event -> {
            if (cell == null) return;  // 안전체크
            StuffDTO stuff = (StuffDTO) cell.getTableView().getItems().get(cell.getIndex());

            String url = "http://localhost:8080/itemStock/delete?stockId=" + stuff.getStockId();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        System.out.println("폐기 응답: " + response.body());

                        javafx.application.Platform.runLater(() -> {
                            cell.getTableView().getItems().remove(stuff);
                            cell.getTableView().refresh();
                        });
                    })
                    .exceptionally(e -> {
                        e.printStackTrace();
                        return null;
                    });
        });
    }
}


