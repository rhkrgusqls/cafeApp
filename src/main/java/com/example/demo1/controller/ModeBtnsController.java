package com.example.demo1.controller;

import com.example.demo1.dto.StoreDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

public class ModeBtnsController {
    @FXML private Button viewButton;
    @FXML private Button deleteButton;

    private final StoreManagementController mainController;
    private final TableCell<?, ?> cell;

    public ModeBtnsController(StoreManagementController mainController, TableCell<?, ?> cell) {
        this.mainController = mainController;
        this.cell = cell;
    }

    @FXML
    public void initialize() {
        viewButton.setOnAction(event -> {
            StoreDTO store = (StoreDTO) cell.getTableView().getItems().get(cell.getIndex());
            mainController.openStuffManagement(store);
        });

        deleteButton.setOnAction(event -> {
            StoreDTO store = (StoreDTO) cell.getTableView().getItems().get(cell.getIndex());
            mainController.confirmDelete(store);
        });
    }
}
