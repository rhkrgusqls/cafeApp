package com.example.demo1.controller;

import com.example.demo1.cell.ModeButtonCell;
import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.*;
import com.example.demo1.properties.ConfigLoader;
import com.example.demo1.refresh.StoreManagementRefresh;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class StoreManagementController implements Initializable {

    @FXML private TableView<StoreDTO> storeTable;
    @FXML private TableColumn<StoreDTO, String> colAffiliationCode;
    @FXML private TableColumn<StoreDTO, String> colStoreName;
    @FXML private TableColumn<StoreDTO, Void> colMode;
    @FXML private TableColumn<StoreDTO, String> colAlarm;

    @FXML private TextField affiliationCodeField;
    @FXML private TextField passwordField;
    @FXML private TextField storeNameField;

    @FXML private Button priStockBtn;
    @FXML private Button itemsBtn;

    @FXML private Button shipmentBtn;

    private String loginAffiliationCode;

    private StuffManagementController stuffManagementController;

    public StuffManagementController getStuffManagementController() {
        return stuffManagementController;
    }

//    private void initializeStuffManagementController() {
//        // stuffManagementController가 제대로 초기화되는지 확인
//        stuffManagementController = new StuffManagementController();
//    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        StoreManagementRefresh.registerController(this);
        colAffiliationCode.setCellValueFactory(new PropertyValueFactory<>("affiliationCode"));
        colStoreName.setCellValueFactory(new PropertyValueFactory<>("storeName"));

        StoreManagementRefresh.refresh();

        colMode.setCellFactory(param -> new ModeButtonCell(this));

        colAlarm.setCellFactory(column -> new TableCell<StoreDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                StoreDTO dto = getTableView().getItems().get(getIndex());
                String alarm = dto.getAlarmState();

                if ("request".equals(alarm)) {
                    Circle redDot = new Circle(5);
                    redDot.setStyle("-fx-fill: red;");
                    setGraphic(redDot);
                } else {
                    setGraphic(null);
                }
            }
        });

        storeTable.setRowFactory(tv -> {
            TableRow<StoreDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    StoreDTO clickedStore = row.getItem();
                    String affiliationCode = clickedStore.getAffiliationCode();
                    openRequestListPopup(affiliationCode);
                }
            });
            return row;
        });

        itemsBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/itemlist.fxml"));
                Parent root = loader.load();

                // 컨트롤러 가져오기
                ItemlistController controller = loader.getController();
                // 로그인한 코드 전달 (본사면 "101" 전달)
                controller.setLoginAffiliationCode(loginAffiliationCode);

                Stage stage = new Stage();
                stage.setTitle("아이템 목록");
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.centerOnScreen();
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        priStockBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/itemlist.fxml"));
                Parent root = loader.load();

                ItemlistController controller = loader.getController();
                controller.setLoginAffiliationCode("999"); // 본점이지만 상태 변경 불가하게, 101로 두면 망해요
                controller.enablePriStockMode(); // 전용 요청 모드 활성화

                Stage stage = new Stage();
                stage.setTitle("아이템 목록");
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.centerOnScreen();
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        shipmentBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/graphs/shipmentGraph.fxml"));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("출고량 그래프");
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.centerOnScreen();
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        loadStoreList(); // 초기 테이블 데이터 불러오기
    }

    public void setLoginAffiliationCode(String code) {
        this.loginAffiliationCode = code;
    }

    public void loadStoreList() {
        new Thread(() -> {
            List<StoreDTO> storeList = fetchStoresFromApi();
            Platform.runLater(() -> {
                if (storeTable != null) {
                    storeTable.getItems().setAll(storeList);
                }
            });
        }).start();
    }

    private List<StoreDTO> fetchStoresFromApi() {
        try {
            URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/affiliation/list");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Cookie", Cookie.getSessionCookie());
            InputStream is = conn.getInputStream();

            // 빈 응답 확인
            if (is.available() == 0) {
                return List.of();  // 빈 리스트 반환
            }

            ObjectMapper mapper = new ObjectMapper();
            AffiliationDTO response = mapper.readValue(is, AffiliationDTO.class);
            List<StoreDTO> storeList = response.getAffiliationList();

            // 점포별 주문 상태 확인 추가
            for (StoreDTO store : storeList) {
                String affiliationCode = store.getAffiliationCode();

                try {
                    URL orderUrl = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/ordering/display");
                    HttpURLConnection orderConn = (HttpURLConnection) orderUrl.openConnection();
                    orderConn.setRequestMethod("POST");
                    orderConn.setRequestProperty("Content-Type", "application/json");
                    orderConn.setRequestProperty("Cookie", Cookie.getSessionCookie());
                    orderConn.setDoOutput(true);

                    String requestBody = "{\"affiliationCode\":\"" + affiliationCode + "\"}";
                    orderConn.getOutputStream().write(requestBody.getBytes("UTF-8"));

                    if (orderConn.getResponseCode() == 200) {
                        InputStream orderStream = orderConn.getInputStream();
                        OrderDTO[] orders = mapper.readValue(orderStream, OrderDTO[].class);
                        for (OrderDTO order : orders) {
                            String state = order.getState();
                            if ("wait".equalsIgnoreCase(state) || "re-review-needed".equalsIgnoreCase(state)) {
                                store.setAlarmState("request");
                                break; // 하나라도 있으면 표시
                            }
                        }
                    }
                } catch (Exception innerEx) {
                    innerEx.printStackTrace();
                    store.setAlarmState("error");
                }
            }
            return storeList;
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> storeTable.setPlaceholder(new Label("데이터를 가져올 수 없습니다.")));
            return List.of();
        }
    }

    @FXML
    private void onPriItemStockBtn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/totalItemList.fxml"));
            Parent root = loader.load();

            TotalItemListController controller = loader.getController();
            controller.setLoginAffiliationCode("101"); // 본점 로그인 기준 전달

            Stage stage = new Stage();
            stage.setTitle("전체 재고 보기 (본점)");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAddStore() {
        String code = affiliationCodeField.getText().trim();
        String password = passwordField.getText().trim();
        String name = storeNameField.getText().trim();

        if (code.isEmpty() || password.isEmpty() || name.isEmpty()) {
            showAlert("모든 항목을 입력하세요.");
            return;
        }

        SignUpDTO dto = new SignUpDTO(code, password, name);

        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/register/signup");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                ObjectMapper mapper = new ObjectMapper();
                try (OutputStream os = conn.getOutputStream()) {
                    mapper.writeValue(os, dto);
                }

                int status = conn.getResponseCode();
                if (status == 200 || status == 201) {
                    Platform.runLater(() -> {
                        showAlert("지점 등록 성공!");
                        clearFields();
                        loadStoreList();
                    });
                } else {
                    Platform.runLater(() ->
                            showAlert("등록 실패: 상태 코드 " + status)
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showAlert("등록 중 오류 발생")
                );
            }
        }).start();
    }

    @FXML
    private void onLogout(ActionEvent event) {
        try {
            // 모든 창 닫기
            List<Window> windows = new ArrayList<>(Window.getWindows());
            for (Window window : windows) {
                if (window instanceof Stage) {
                    ((Stage) window).close();
                }
            }

            // 새 로그인 Stage 생성
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));

            // CafeApplication과 동일한 설정 복사
            loginStage.getIcons().add(new Image(getClass().getResourceAsStream("/coffee.png")));
            loginStage.setTitle("Cafe Stuff Management System");
            loginStage.setResizable(false);
            loginStage.centerOnScreen();

            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openRequestListPopup(String affiliationCode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/requestlist.fxml"));
            Parent root = loader.load();

            RequestlistController controller = loader.getController();

            // FXML 로딩 완료 이후 실행되도록 보장
            Platform.runLater(() -> controller.setAffiliationCode(affiliationCode));

            Stage stage = new Stage();
            stage.setTitle("주문 요청 - " + affiliationCode);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.setOnHidden(e -> loadStoreList());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openAllStuffManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/stuffManagement.fxml"));
            Parent root = loader.load();
            StuffManagementController controller = loader.getController();

            controller.setAffiliationContext(ConfigLoader.getManagerCode(), ConfigLoader.getManagerCode());
            controller.setViewingAllStock(true);
            controller.loadAllStock(); // 전체 조회 메서드 호출

            Stage stage = new Stage();
            stage.setTitle("Stuff Management");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openStuffManagement(StoreDTO store) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/stuffManagement.fxml"));
            Parent root = loader.load();

            StuffManagementController controller = loader.getController();

            // 본점이 로그인한 상태에서 store 테이블에서 다른 분점 클릭했을 경우
            controller.setAffiliationContext(this.loginAffiliationCode, store.getAffiliationCode());

            Stage stage = new Stage();
            stage.setTitle("Stuff Management");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void confirmDelete(StoreDTO store) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("삭제 확인");
        alert.setHeaderText("정말 이 가게를 삭제하시겠습니까?");
        alert.setContentText(store.getStoreName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteStore(store);
        }
    }

    private void deleteStore(StoreDTO store) {
        // 1. 화면에서 먼저 제거
        storeTable.getItems().remove(store);

        // 2. 서버에 삭제 요청 보내기
        new Thread(() -> {
            try {
                String code = store.getAffiliationCode();
                String urlStr = "http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/affiliation/delete?affiliationCode=" +
                        java.net.URLEncoder.encode(code, "UTF-8");
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    InputStream is = conn.getInputStream();
                    String response = new String(is.readAllBytes());
                    Platform.runLater(() -> showAlert("삭제 성공: " + response));
                } else {
                    Platform.runLater(() -> showAlert("삭제 실패: 상태코드 " + responseCode));
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("삭제 중 오류 발생"));
            }
        }).start();
    }

    private void clearFields() {
        affiliationCodeField.clear();
        passwordField.clear();
        storeNameField.clear();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("알림");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
