package com.example.demo1.cell;

import com.example.demo1.controller.ModeBtnsController;
import com.example.demo1.controller.StoreManagementController;
import com.example.demo1.dto.StoreDTO;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableCell;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class ModeButtonCell extends TableCell<StoreDTO, Void> {

    private AnchorPane pane;

    public ModeButtonCell(StoreManagementController mainController) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/modeBtns.fxml"));
            loader.setController(new ModeBtnsController(mainController, this));
            pane = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || getIndex() >= getTableView().getItems().size()) {
            setGraphic(null);
        } else {
            setGraphic(pane);
        }
    }
}