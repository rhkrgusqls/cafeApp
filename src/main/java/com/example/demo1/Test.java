package com.example.demo1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;

import java.util.Random;

public class Test {

    @FXML
    private LineChart<Number, Number> lineChart;

    @FXML
    private NumberAxis xAxis, yAxis;

    @FXML
    public void initialize() {
        String[] colors = {"Red", "Green", "Blue"};
        String[] assignedColors = new String[10];
        XYChart.Series<Number, Number>[] seriesArray = new XYChart.Series[10];
        Random random = new Random();

        int prevY = random.nextInt(100); // 첫 번째 y값

        for (int i = 0; i < 10; i++) {
            // 랜덤 색상 선택
            assignedColors[i] = colors[random.nextInt(colors.length)];

            // 현재 시리즈 초기화
            seriesArray[i] = new XYChart.Series<>();

            // 다음 y값 생성
            int currentY = random.nextInt(100);

            // 현재 시리즈에는 (i, prevY) → (i+1, currentY) 선 추가
            seriesArray[i].getData().add(new XYChart.Data<>(i, prevY));
            seriesArray[i].getData().add(new XYChart.Data<>(i + 1, currentY));

            // 차트에 추가
            lineChart.getData().add(seriesArray[i]);

            // 다음 루프를 위해 y값 업데이트
            prevY = currentY;
        }


        // 시리즈 스타일 지정 - UI 렌더링 이후에 수행
        Platform.runLater(() -> {
            for (int i = 0; i < 10; i++) {
                String style;
                switch (assignedColors[i]) {
                    case "Red":
                        style = "-fx-stroke: #FF0000; -fx-stroke-dash-array: 8 5;";
                        break;
                    case "Green":
                        style = "-fx-stroke: #00FF00; -fx-stroke-dash-array: 8 5;";
                        break;
                    case "Blue":
                        style = "-fx-stroke: #0000FF; -fx-stroke-dash-array: 8 5;";
                        break;
                    default:
                        style = "-fx-stroke: #000000; -fx-stroke-dash-array: 8 5;";
                        break;
                }
                applySeriesStyle(i, style);
            }
        });
    }


    private void applySeriesStyle(int seriesIndex, String style) {
        Node line = lineChart.lookup(".chart-series-line.series" + seriesIndex);
        if (line != null) {
            line.setStyle(style);
        }
    }
}
