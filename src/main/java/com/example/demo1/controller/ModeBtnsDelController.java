package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.StuffDTO;
import com.example.demo1.properties.ConfigLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

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

            // 삭제 확인 알림창
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("폐기 확인");
            alert.setHeaderText("정말로 이 재고를 폐기하시겠습니까?");
            alert.setContentText("Stock ID: " + stuff.getStockId());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String url = "http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort()
                        + "/itemStock/delete?stockId=" + stuff.getStockId();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .header("Cookie", Cookie.getSessionCookie()) // 쿠키 추가
                        .build();

                HttpClient client = HttpClient.newHttpClient();
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            System.out.println("폐기 응답: " + response.body());

                            javafx.application.Platform.runLater(() -> {
                                // 서버 응답 메시지를 Alert로 표시
                                Alert respAlert = new Alert(Alert.AlertType.INFORMATION);
                                respAlert.setTitle("폐기 결과");
                                respAlert.setHeaderText(null);
                                respAlert.setContentText(response.body());
                                respAlert.showAndWait();

                                // 폐기 성공 시 테이블에서 삭제
                                if (response.statusCode() == 200 && response.body().contains("성공")) {
                                    cell.getTableView().getItems().remove(stuff);
                                    cell.getTableView().refresh();
                                }
                            });
                        })
                        .exceptionally(e -> {
                            e.printStackTrace();
                            return null;
                        });
            }
        });
    }
}


