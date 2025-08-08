package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.ItemDTO;
import com.example.demo1.dto.ItemLimitDTO;
import com.example.demo1.dto.ItemLimitViewDTO;
import com.example.demo1.properties.ConfigLoader;
import com.example.demo1.refresh.TotalItemListRefresh;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

    private static boolean suppressLowStockPopup = false; // 로그인 후부터 유효, 팝업창 다시 보지 않기

    private String loginAffiliationCode; // 로그인 후 주입받는 값

    public void setLoginAffiliationCode(String code) {
        this.loginAffiliationCode = code;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        suppressLowStockPopup = false; // 로그인 시 초기화

        itemInfoMap = fetchItemInfoMap();

        colItemId.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colRealQty.setCellValueFactory(new PropertyValueFactory<>("realQuantity"));
        colLimitQty.setCellValueFactory(new PropertyValueFactory<>("limitQuantity"));
        colWithinLimit.setCellValueFactory(new PropertyValueFactory<>("withinLimit"));

        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tableView.getSelectionModel().getSelectedItem() != null) {
                ItemLimitViewDTO selected = tableView.getSelectionModel().getSelectedItem();
                openLimitEditDialog(selected.getItemId());
            }
        });

        colWithinLimit.setCellFactory(col -> new TableCell<>() {
            private final Button requestBtn = new Button("요청");

            {
                requestBtn.setOnAction(e -> {
                    ItemLimitViewDTO dto = getTableView().getItems().get(getIndex());
                    openRequestForm(dto.getItemId());
                });
            }

            @Override
            protected void updateItem(Boolean withinLimit, boolean empty) {
                super.updateItem(withinLimit, empty);

                if (empty || withinLimit == null || !withinLimit) {
                    setGraphic(null);  // false이거나 비었으면 버튼 없음
                } else {
                    setGraphic(requestBtn); // true면 버튼 표시
                }
                setText(null); // 텍스트는 제거
            }
        });

        TotalItemListRefresh.registerController(this);

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

    private void openRequestForm(int itemId) {
        try {
            FXMLLoader loader;
            Parent root;
            Stage currentStage = (Stage) tableView.getScene().getWindow(); // 현재 전체재고 창

            if (ConfigLoader.getManagerCode().equals(loginAffiliationCode)) {
                loader = new FXMLLoader(getClass().getResource("/com/example/demo1/priStockRequest.fxml"));
                root = loader.load();

                PriStockRequestController controller = loader.getController();
                controller.setItemId(itemId);
                controller.setParentStage(currentStage); // 부모 창 전달

            } else {
                loader = new FXMLLoader(getClass().getResource("/com/example/demo1/requestForm.fxml"));
                root = loader.load();

                RequestFormController controller = loader.getController();
                controller.setItemId(itemId);
                controller.setLoginAffiliationCode(loginAffiliationCode);
                controller.setParentStage(currentStage); // 부모 창 전달
            }

            Stage popupStage = new Stage();
            popupStage.setTitle("재고 요청");
            popupStage.setScene(new Scene(root));
            popupStage.setResizable(false);
            popupStage.centerOnScreen();
            popupStage.show();

        } catch (Exception e) {
            e.printStackTrace();
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

    public void loadItemLimitData() {
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

                if (itemLimits.length == 0) {
                    Platform.runLater(() -> tableView.getItems().clear());
                    return;
                }

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

                // 테이블 갱신
                Platform.runLater(() -> tableView.getItems().setAll(viewList));

                var lowStockList = Arrays.stream(itemLimits)
                        .filter(ItemLimitDTO::isWithinLimit) // 부족한 재고만 필터링
                        .map(dto -> {
                            ItemDTO itemInfo = itemInfoMap.get(dto.getItemId());
                            return itemInfo != null ? itemInfo.getName() : "Unknown";
                        })
                        .toList();

                if (!suppressLowStockPopup && !lowStockList.isEmpty()) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("재고 부족 경고");
                        alert.setHeaderText(null);

                        // 기존 메시지
                        String msg = String.join(", ", lowStockList) + " 재고가 부족합니다.\n재고 요청을 고려해보세요.";
                        Label messageLabel = new Label(msg);
                        messageLabel.setWrapText(true); // 텍스트 줄바꿈 가능하게

                        // 체크박스 추가
                        javafx.scene.control.CheckBox suppressBox = new javafx.scene.control.CheckBox("다음 로그인 시까지 보지 않기");

                        // VBox로 묶기
                        VBox content = new VBox(10, messageLabel, suppressBox);
                        alert.getDialogPane().setContent(content);

                        alert.showAndWait();

                        // 체크된 경우 suppress
                        if (suppressBox.isSelected()) {
                            suppressLowStockPopup = true;
                        }
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
    private void openLimitEditDialog(int itemId) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("제한 수량 설정");
        dialog.setHeaderText("아이템 ID: " + itemId);
        dialog.setContentText("새 제한 수량을 입력하세요:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int quantity = Integer.parseInt(input);
                updateItemLimit(itemId, quantity);
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "유효한 숫자를 입력하세요.").showAndWait();
            }
        });
    }

    private void updateItemLimit(int itemId, int quantity) {
        new Thread(() -> {
            try {
                String urlStr = String.format("http://%s:%s/itemStock/alarm/update?itemId=%d&quantity=%d",
                        ConfigLoader.getIp(), ConfigLoader.getPort(), itemId, quantity);
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    boolean success = new ObjectMapper().readValue(conn.getInputStream(), Boolean.class);
                    Platform.runLater(() -> {
                        if (success) {
                            showAlert("성공", "제한 수량이 성공적으로 수정되었습니다.");
                            loadItemList(); // 테이블 갱신
                        } else {
                            showAlert("실패", "제한 수량 수정 실패.");
                        }
                    });
                } else {
                    Platform.runLater(() -> showAlert("오류", "서버 오류: " + responseCode));
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("예외 발생", e.getMessage()));
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
