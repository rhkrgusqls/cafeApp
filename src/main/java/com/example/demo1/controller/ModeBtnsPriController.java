package com.example.demo1.controller;

import com.example.demo1.dto.StoreDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

public class ModeBtnsPriController {

    @FXML private Button viewButton;
    @FXML private Button viewAllButton;

    private final StoreManagementController mainController;  // StoreManagementController 참조
    private final TableCell<?, ?> cell;

    // Constructor now takes StuffManagementController as parameter
    public ModeBtnsPriController(StoreManagementController mainController, StuffManagementController stuffManagementController, TableCell<?, ?> cell) {
        this.mainController = mainController;
        this.cell = cell;
    }

    @FXML
    public void initialize() {
        // 점포 정보 조회
        viewButton.setOnAction(event -> {
            StoreDTO store = (StoreDTO) cell.getTableView().getItems().get(cell.getIndex());
            mainController.openStuffManagement(store);  // 점포 조회
        });

        // 전체 점포 조회 (본점에서만 사용)
        viewAllButton.setOnAction(event -> {
            mainController.openAllStuffManagement();  // 본점 전체 점포 조회
        });
    }
}
