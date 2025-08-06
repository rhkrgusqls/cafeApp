package com.example.demo1.controller;

import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.StuffDTO;
import com.example.demo1.properties.ConfigLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class StuffManagementController implements Initializable {

    @FXML private TableView<StuffDTO> stuffTable;

    @FXML private TableColumn<StuffDTO, Integer> colNumber;
    @FXML private TableColumn<StuffDTO, String> colItemID;
    @FXML private TableColumn<StuffDTO, String> colName;
    @FXML private TableColumn<StuffDTO, String> colCategory;
    @FXML private TableColumn<StuffDTO, Integer> colQuantity;
    @FXML private TableColumn<StuffDTO, String> colExpireDate;
    @FXML private TableColumn<StuffDTO, String> colReceivedDate;
    @FXML private TableColumn<StuffDTO, String> colStatus;
    @FXML private TableColumn<StuffDTO, String> colAffiliationCode;
    @FXML private TableColumn<StuffDTO, String> colMode;

    @FXML private Button logoutBtn;
    @FXML private Button requestBtn;
    @FXML private Button historyBtn;
    @FXML private Text affiliationNum;

    private String loginAffiliationCode;    // 로그인한 사용자
    private String viewAffiliationCode;     // 조회 대상 분점

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNumber.setCellValueFactory(new PropertyValueFactory<>("stockId"));
        colItemID.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("itemName"));   // 추가
        colCategory.setCellValueFactory(new PropertyValueFactory<>("itemCategory")); // 추가
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colReceivedDate.setCellValueFactory(new PropertyValueFactory<>("receivedDate"));
        colExpireDate.setCellValueFactory(new PropertyValueFactory<>("expireDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setSortable(false);
        colAffiliationCode.setCellValueFactory(new PropertyValueFactory<>("affiliationCode"));
        colMode.setCellValueFactory(new PropertyValueFactory<>("mode"));

        ContextMenu statusFilterMenu = new ContextMenu();
        MenuItem allItem = new MenuItem("All");
        MenuItem availableItem = new MenuItem("Available");
        MenuItem defectiveItem = new MenuItem("Defective");
        MenuItem depletedItem = new MenuItem("Depleted");

        allItem.setOnAction(e -> loadStuffListWithState(null));
        availableItem.setOnAction(e -> loadStuffListWithState("available"));
        defectiveItem.setOnAction(e -> loadStuffListWithState("defective"));
        depletedItem.setOnAction(e -> loadStuffListWithState("depleted"));

        statusFilterMenu.getItems().addAll(allItem, availableItem, defectiveItem, depletedItem);

        Label statusHeader = new Label("Status ▼");
        statusHeader.setOnMouseClicked(event ->
                statusFilterMenu.show(statusHeader, event.getScreenX(), event.getScreenY())
        );
        colStatus.setGraphic(statusHeader);

        colStatus.setCellFactory(column -> new TableCell<StuffDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("defective".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        stuffTable.setRowFactory(tv -> {
            TableRow<StuffDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    StuffDTO clickedRow = row.getItem();

                    // 101(본점)이 아닐 때만 itemlist.fxml 팝업 띄우기
                    if (!"101".equals(loginAffiliationCode)) {
                        openItemInfoPopup(clickedRow.getItemId());
                    }
                }
            });
            return row;
        });

        //stuffTable.getScene().setUserData(this);

        colMode.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                StuffDTO dto = getTableView().getItems().get(getIndex());

                if ("available".equalsIgnoreCase(dto.getStatus())) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/modeBtnsUD.fxml"));
                        AnchorPane pane = loader.load();

                        ModeBtnsUDController controller = loader.getController();
                        controller.init(this, StuffManagementController.this);

                        setGraphic(pane);
                    } catch (IOException e) {
                        e.printStackTrace();
                        setGraphic(null);
                    }
                }else if ("defective".equalsIgnoreCase(dto.getStatus())) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/modeBtnsDel.fxml"));
                        AnchorPane pane = loader.load(); // 먼저 로드

                        ModeBtnsDelController controller = loader.getController(); // 컨트롤러 인스턴스 가져오기
                        controller.init(this); // cell 주입

                        setGraphic(pane);
                    } catch (IOException e) {
                        e.printStackTrace();
                        setGraphic(null);
                    }
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void openItemInfoPopup(int itemId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/iteminfo.fxml"));
            Parent root = loader.load();

            // Controller 연결 및 데이터 전달
            ItemInfoController controller = loader.getController();
            controller.setItemId(itemId); // 해당 itemId 설정 및 정보 조회

            Stage stage = new Stage();
            stage.setTitle("상품 정보");
            stage.setScene(new Scene(root));
            stage.setResizable(false); // 창 크기 고정
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAffiliationContext(String loginCode, String viewCode) {
        this.loginAffiliationCode = loginCode;
        this.viewAffiliationCode = viewCode;

        this.affiliationNum.setText(viewAffiliationCode);

        if ((ConfigLoader.getManagerCode().equals(loginCode) && !loginCode.equals(viewCode)) || loginCode.equals(ConfigLoader.getManagerCode())) { // 101일때 직접 물품추가하는 프론트가 필요
            logoutBtn.setVisible(false);
            requestBtn.setVisible(false);
            historyBtn.setVisible(false);
            logoutBtn.setManaged(false);
        } else {
            logoutBtn.setVisible(true);
            requestBtn.setVisible(true);
            historyBtn.setVisible(true);
            logoutBtn.setManaged(true);
        }

        loadStuffList();
    }

    public void loadStuffList() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/itemStock/list");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());
                // JSON 바디 작성
                String jsonBody = String.format("{\"affiliationCode\":\"%s\"}", viewAffiliationCode);
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    InputStream is = conn.getInputStream();
                    ObjectMapper mapper = new ObjectMapper();
                    StuffDTO[] items = mapper.readValue(is, StuffDTO[].class);

                    Platform.runLater(() -> stuffTable.getItems().setAll(items));
                } else {
                    Platform.runLater(() ->
                            stuffTable.setPlaceholder(new Label("서버 오류: " + responseCode))
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        stuffTable.setPlaceholder(new Label("불러오기 실패"))
                );
            }
        }).start();
    }

    private void loadStuffListWithState(String state) {
        new Thread(() -> {
            try {
                String query = (state != null && !state.isEmpty()) ? "?state=" + state : "";
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/itemStock/list" + query);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                String jsonBody = String.format("{\"affiliationCode\":\"%s\"}", viewAffiliationCode);
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes("utf-8"));
                }

                if (conn.getResponseCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    StuffDTO[] items = mapper.readValue(conn.getInputStream(), StuffDTO[].class);
                    Platform.runLater(() -> stuffTable.getItems().setAll(items));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    public void loadAllStock() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/itemStock/listAll");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Cookie", Cookie.getSessionCookie());

                logoutBtn.setVisible(false);
                requestBtn.setVisible(false);
                historyBtn.setVisible(false);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    InputStream is = conn.getInputStream();
                    ObjectMapper mapper = new ObjectMapper();
                    StuffDTO[] items = mapper.readValue(is, StuffDTO[].class);

                    Platform.runLater(() -> stuffTable.getItems().setAll(items));
                } else {
                    Platform.runLater(() ->
                            stuffTable.setPlaceholder(new Label("서버 오류: " + responseCode))
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        stuffTable.setPlaceholder(new Label("전체 조회 실패"))
                );
            }
        }).start();
    }

    @FXML
    private void onHistoryBtn() {
        try {
            // `affiliationCode`를 전달하여 재고 기록 화면을 연다
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/affiliationRequestList.fxml"));
            Parent history = loader.load();
            AffiliationLogController controller = loader.getController();
            controller.setAffiliationContext(loginAffiliationCode); // 로그인한 사용자 점포 코드 전달

            Stage stage = new Stage();
            stage.setTitle("재고 기록");
            stage.setScene(new Scene(history));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.setOnHidden(e -> {
                // loginAffiliationCode가 101이 아닌 경우에만 새로고침
                if (!"101".equals(loginAffiliationCode)) {
                    loadStuffList();
                }
            });
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRequestBtn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/itemlist.fxml"));
            Parent root = loader.load();

            ItemlistController controller = loader.getController();
            controller.setLoginAffiliationCode(loginAffiliationCode);//로그인할때 분점코드를 넣어야 요청가능

            // controller 넘겨줄 필요가 있다면 이곳에서 setItemId(itemId) 가능
            // ItemlistController controller = loader.getController();
            // controller.setItemId(itemId); // 예시

            Stage stage = new Stage();
            stage.setTitle("아이템 목록");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }  // 현재 로그인한 점포 코드 전달
    }

    @FXML
    private void onLogout() {
        try {
            // 현재 열려 있는 모든 창 닫기
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

            // CafeApplication과 동일하게 설정
            loginStage.getIcons().add(new Image(getClass().getResourceAsStream("/coffee.png")));
            loginStage.setTitle("Cafe Stuff Management System");
            loginStage.setResizable(false);
            loginStage.centerOnScreen();

            loginStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

// 아이템 리스트에서 아이템 아이디 받아와서, 여기 아이템id칸에 고정시킬 예정
//    private void openItemRequestPopup(String affiliationCode) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/requestForm.fxml"));
//            Parent root = loader.load();
//
//            RequestFormController controller = loader.getController();
//            controller.setLoginAffiliationCode(affiliationCode); // 이제는 itemId, quantity는 입력 받음
//
//            Stage stage = new Stage();
//            stage.setTitle("재고 요청");
//            stage.setScene(new Scene(root));
//            stage.setResizable(false);
//            stage.centerOnScreen();
//            stage.show();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    private void openItemListPopup(int itemId) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/itemlist.fxml"));
//            Parent root = loader.load();
//
//            // controller 넘겨줄 필요가 있다면 이곳에서 setItemId(itemId) 가능
//            // ItemlistController controller = loader.getController();
//            // controller.setItemId(itemId); // 예시
//
//            Stage stage = new Stage();
//            stage.setTitle("아이템 목록");
//            stage.setScene(new Scene(root));
//            stage.setResizable(false);
//            stage.centerOnScreen();
//            stage.show();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
