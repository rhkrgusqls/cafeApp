package com.example.demo1.controller;

import com.example.demo1.controller.util.RefreshAll;
import com.example.demo1.dto.StuffDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ModeBtnsUDController {

    @FXML private Button updateBtn;
    @FXML private Button decreaseBtn;

    private TableCell<?, ?> parentCell;
    private StuffManagementController parentController;

    public void init(TableCell<?, ?> cell, StuffManagementController parentController) {
        this.parentCell = cell;
        this.parentController = parentController;
    }

    @FXML
    private void onDecrease() {
        try {
            StuffDTO dto = (StuffDTO) parentCell.getTableView().getItems().get(parentCell.getIndex());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/StuffDecrease.fxml"));
            AnchorPane root = loader.load();

            StuffDecreaseController controller = loader.getController();
            controller.setItemData(dto, dto.getAffiliationCode(), parentController);

            Stage stage = new Stage();
            stage.setTitle("재고 감소");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onUpdate() {
        try {
            StuffDTO dto = (StuffDTO) parentCell.getTableView().getItems().get(parentCell.getIndex());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/StuffUpdate.fxml"));
            AnchorPane root = loader.load();

            StuffUpdateController controller = loader.getController();
            controller.setItemData(dto, parentController);

            Stage stage = new Stage();
            stage.setTitle("재고 수정");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
            new RefreshAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
