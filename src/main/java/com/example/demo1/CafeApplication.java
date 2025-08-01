package com.example.demo1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class CafeApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CafeApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/coffee.png")));
        stage.setTitle("Cafe Stuff Management System");
        stage.setScene(scene);
        stage.setResizable(false); // 화면 크기 고정
        stage.centerOnScreen(); // 초기 위치(모니터 가운데)
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}