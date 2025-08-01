package com.example.demo1.controller;

import com.example.demo1.dto.StuffDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
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
import java.net.URLEncoder;
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
        colAffiliationCode.setCellValueFactory(new PropertyValueFactory<>("affiliationCode"));
        colMode.setCellValueFactory(new PropertyValueFactory<>("mode"));
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
                    openItemInfoPopup(clickedRow.getItemId());
                }
            });
            return row;
        });

    }

    private void openItemInfoPopup(int itemId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/itemlist.fxml"));
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

        if ("101".equals(loginCode) && !loginCode.equals(viewCode)) {
            logoutBtn.setVisible(false);
            requestBtn.setVisible(false);
            logoutBtn.setManaged(false);
        } else {
            logoutBtn.setVisible(true);
            requestBtn.setVisible(true);
            logoutBtn.setManaged(true);
        }

        loadStuffList();
    }

    private void loadStuffList() {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/itemStock/list");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

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

    private void openItemRequestPopup(String affiliationCode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/requestForm.fxml"));
            Parent root = loader.load();

            RequestFormController controller = loader.getController();
            controller.setAffiliationContext(affiliationCode); // 이제는 itemId, quantity는 입력 받음

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

    @FXML
    private void onRequestBtn() {
        openItemRequestPopup(loginAffiliationCode);  // 현재 로그인한 점포 코드 전달
    }



    @FXML
    private void onLogout() {
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/com/example/demo1/login.fxml"));
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(login));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
