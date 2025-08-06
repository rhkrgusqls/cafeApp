package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.ItemDTO;

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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ResourceBundle;


public class ItemlistController implements Initializable {

    @FXML private TableView<ItemDTO> tableView;
    @FXML private TableColumn<ItemDTO, Integer> colItemID;
    @FXML private TableColumn<ItemDTO, String> colName;
    @FXML private TableColumn<ItemDTO, String> colCategory;
    @FXML private TableColumn<ItemDTO, String> colState;

    @FXML private Button addBtn;
    @FXML private Text infoText;

    //private boolean requestMode = false; // 기본은 요청 모드 아님
    private String loginAffiliationCode;
    private boolean priStockMode = false;

    private ObservableList<ItemDTO> originalData = FXCollections.observableArrayList();

    public void setLoginAffiliationCode(String loginAffiliationCode) {
    //    this.requestMode = true; // 요청 모드 켜기
        this.loginAffiliationCode = loginAffiliationCode;
        addBtn.setVisible("101".equals(loginAffiliationCode));
        infoText.setVisible("101".equals(loginAffiliationCode));
    }

    public void enablePriStockMode() {
        this.priStockMode = true;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colItemID.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colState.setCellValueFactory(new PropertyValueFactory<>("state"));
        colState.setSortable(false);
        // 필터 메뉴
        ContextMenu filterMenu = new ContextMenu();
        String[] states = {"전체", "available", "unavailable"};
        for (String state : states) {
            MenuItem menuItem = new MenuItem(state);
            menuItem.setOnAction(e -> filterByState(state.equals("전체") ? null : state));
            filterMenu.getItems().add(menuItem);
        }
        Label stateHeader = new Label("State ▼");
        stateHeader.setOnMouseClicked(e -> filterMenu.show(stateHeader, e.getScreenX(), e.getScreenY()));
        colState.setGraphic(stateHeader);

        tableView.setRowFactory(tv -> {
            TableRow<ItemDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    ItemDTO selectedItem = row.getItem();

                    if (priStockMode) {
                        // 본점 전용 요청 모드 → unavailable이면 차단
                        if ("unavailable".equalsIgnoreCase(selectedItem.getState())) {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("요청 불가");
                            alert.setHeaderText(null);
                            alert.setContentText("요청할 수 없는 재고입니다.");
                            alert.showAndWait();
                            return; // 요청창 안 띄움
                        }
                        openPriStockRequest(selectedItem.getItemId());
                        return;
                    }

                    if ("101".equals(loginAffiliationCode)) {
                        // 본점 일반 모드 → 상태 변경
                        String nextState =
                                "available".equalsIgnoreCase(selectedItem.getState()) ? "unavailable" : "available";
                        showConfirmAndChangeState(selectedItem.getItemId(), nextState);
                    } else {
                        // 분점 모드
                        if ("unavailable".equalsIgnoreCase(selectedItem.getState())) {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("요청 불가");
                            alert.setHeaderText(null);
                            alert.setContentText("요청할 수 없는 재고입니다.");
                            alert.showAndWait();
                            return;
                        }
                        openRequestFormWithItemId(selectedItem.getItemId());
                    }
                }
            });
            return row;
        });

        addBtn.setOnAction(e -> openItemInfo());
        loadItemList();
    }

    private void filterByState(String state) {
        if (state == null) { // 전체 보기
            tableView.setItems(FXCollections.observableArrayList(originalData));
        } else {
            ObservableList<ItemDTO> filtered = originalData.filtered(
                    item -> state.equalsIgnoreCase(item.getState())
            );
            tableView.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    private void showConfirmAndChangeState(int itemId, String newState) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("상태 변경 확인");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("정말 상태를 '" + newState + "'(으)로 변경하시겠습니까?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                changeItemState(itemId, newState);
            }
        });
    }

    private void changeItemState(int itemId, String state) {
        new Thread(() -> {
            try {
                String urlStr = String.format(
                        "http://%s:%s/items/setState?itemId=%d&state=%s",
                        ConfigLoader.getIp(),
                        ConfigLoader.getPort(),
                        itemId,
                        java.net.URLEncoder.encode(state, "UTF-8") // state 인코딩
                );

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                int responseCode = conn.getResponseCode();
                InputStream is = (responseCode >= 200 && responseCode < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                String responseMessage = (is != null) ? new String(is.readAllBytes()) : "응답 없음";

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("상태 변경");
                    alert.setHeaderText(null);
                    alert.setContentText(responseMessage);
                    alert.showAndWait();

                    if (responseCode == 200) {
                        loadItemList(); // 성공 시 새로고침
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("오류");
                    alert.setHeaderText(null);
                    alert.setContentText("상태 변경 중 오류: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    //아이템 리스트에서 항목을 더블클릭하면 해당 물품을 요청하는 페이지 생성
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

    private void openPriStockRequest(int itemId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/pristockrequest.fxml"));
            Parent root = loader.load();

            PriStockRequestController controller = loader.getController();
            controller.setItemId(itemId);

            Stage stage = new Stage();
            stage.setTitle("본점 전용 물품 요청");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void loadItemList() {
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
                        originalData.setAll(items); // 원본 리스트 저장
                        tableView.setItems(FXCollections.observableArrayList(originalData)); // 테이블에 표시
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
    // 본사에서 Add 버튼 눌렀을 때
    private void openItemInfo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/iteminfo.fxml"));
            Parent root = loader.load();

            ItemInfoController controller = loader.getController();
            controller.setLoginAffiliationCode(loginAffiliationCode); // 본사 코드 전달
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("아이템 추가");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
