package com.example.demo1.controller;

import com.example.demo1.cell.ModeButtonCell;
import com.example.demo1.controller.util.Cookie;
import com.example.demo1.dto.AffiliationDTO;
import com.example.demo1.dto.SignUpDTO;
import com.example.demo1.dto.StoreDTO;
import com.example.demo1.properties.ConfigLoader;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class StoreManagementController implements Initializable {

    @FXML private TableView<StoreDTO> storeTable;
    @FXML private TableColumn<StoreDTO, String> colAffiliationCode;
    @FXML private TableColumn<StoreDTO, String> colStoreName;
    @FXML private TableColumn<StoreDTO, Void> colMode;

    @FXML private TextField affiliationCodeField;
    @FXML private TextField passwordField;
    @FXML private TextField storeNameField;

    @FXML private Button itemsBtn;

    private String loginAffiliationCode;

    private StuffManagementController stuffManagementController;

    public StuffManagementController getStuffManagementController() {
        return stuffManagementController;
    }

    private void initializeStuffManagementController() {
        // stuffManagementController가 제대로 초기화되는지 확인
        stuffManagementController = new StuffManagementController();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colAffiliationCode.setCellValueFactory(new PropertyValueFactory<>("affiliationCode"));
        colStoreName.setCellValueFactory(new PropertyValueFactory<>("storeName"));

        colMode.setCellFactory(param -> new ModeButtonCell(this));

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


        loadStoreList(); // 초기 테이블 데이터 불러오기
    }

    public void setLoginAffiliationCode(String code) {
        this.loginAffiliationCode = code;
    }

    private void loadStoreList() {
        new Thread(() -> {
            List<StoreDTO> storeList = fetchStoresFromApi();
            Platform.runLater(() -> {
                if (storeTable != null) {
                    storeTable.getItems().setAll(storeList);
                }
            });
        }).start();
    }

    private void openItemList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/itemlist.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("아이템 목록");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private List<StoreDTO> fetchStoresFromApi() {
        try {
            URL url = new URL("http://" + ConfigLoader.getIp() + ":" + ConfigLoader.getPort() + "/affiliation/list");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Cookie", Cookie.getSessionCookie());
            InputStream is = conn.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            AffiliationDTO response = mapper.readValue(is, AffiliationDTO.class);
            return response.getAffiliationList();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() ->
                    storeTable.setPlaceholder(new Label("데이터를 가져올 수 없습니다."))
            );
            return List.of();
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
        URL fxmlUrl = getClass().getResource("/com/example/demo1/login.fxml");

        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openRequestListPopup(String affiliationCode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/requestlist.fxml"));
            Parent root = loader.load();

            RequestlistController controller = loader.getController();

            // FXML 주입 완료 이후 실행되도록 보장
            Platform.runLater(() -> controller.setAffiliationCode(affiliationCode));

            Stage stage = new Stage();
            stage.setTitle("주문 요청 - " + affiliationCode);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
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
