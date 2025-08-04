package com.example.demo1.cell;

import com.example.demo1.controller.ModeBtnsController;
import com.example.demo1.controller.ModeBtnsPriController;
import com.example.demo1.controller.StoreManagementController;
import com.example.demo1.dto.StoreDTO;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableCell;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class ModeButtonCell extends TableCell<StoreDTO, Void> {

    private final StoreManagementController mainController;

    public ModeButtonCell(StoreManagementController mainController) {
        this.mainController = mainController;
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || getIndex() >= getTableView().getItems().size()) {
            setGraphic(null);
            return;
        }

        StoreDTO store = getTableView().getItems().get(getIndex());

        try {
            FXMLLoader loader;
            AnchorPane pane;

            if ("101".equals(store.getAffiliationCode())) {
                // 본점: 조회 + 전체조회 버튼
                loader = new FXMLLoader(getClass().getResource("/com/example/demo1/modeBtnsPri.fxml"));
                // 본점에서 StuffManagementController를 전달
                loader.setController(new ModeBtnsPriController(mainController, mainController.getStuffManagementController(), this));  // pass StuffManagementController
                pane = loader.load();
            } else {
                // 분점: 조회 + 삭제 버튼
                loader = new FXMLLoader(getClass().getResource("/com/example/demo1/modeBtns.fxml"));
                loader.setController(new ModeBtnsController(mainController, this));  // 분점용 컨트롤러 사용
                pane = loader.load();
            }
            setGraphic(pane);
        } catch (IOException e) {
            e.printStackTrace();
            setGraphic(null);
        }
    }
}
