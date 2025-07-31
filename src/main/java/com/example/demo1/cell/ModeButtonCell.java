package com.example.demo1.cell;

import com.example.demo1.controller.ModeBtnsController;
import com.example.demo1.controller.StoreManagementController;
import com.example.demo1.dto.StoreDTO;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class ModeButtonCell extends TableCell<StoreDTO, Void> {

    private AnchorPane pane;
    private final StoreManagementController mainController;

    public ModeButtonCell(StoreManagementController mainController) {
        this.mainController = mainController;
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
            return;
        }

        StoreDTO store = getTableView().getItems().get(getIndex());
        if ("101".equals(store.getAffiliationCode())) {
            // 본점이면 "본점"이라는 텍스트 라벨만 보여줌
            Label label = new Label("본점");
            label.setStyle("-fx-text-fill: #888; -fx-font-weight: bold;");
            setGraphic(label);
        } else {
            // 분점이면 버튼 패널 표시
            setGraphic(pane);
        }
    }
}
