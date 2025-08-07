package com.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;

public class Test {

    @FXML
    private LineChart<Number, Number> lineChart;

    @FXML
    private NumberAxis xAxis, yAxis;

    @FXML
    public void initialize() {
        // 첫 구간 (빨간 점선)
        XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
        series1.getData().add(new XYChart.Data<>(0, 1));
        series1.getData().add(new XYChart.Data<>(1, 3));
        series1.getData().add(new XYChart.Data<>(2, 2));

        // 두 번째 구간 (파란 점선)
        XYChart.Series<Number, Number> series2 = new XYChart.Series<>();
        series2.getData().add(new XYChart.Data<>(2, 2));
        series2.getData().add(new XYChart.Data<>(3, 4));
        series2.getData().add(new XYChart.Data<>(4, 3));

        lineChart.getData().addAll(series1, series2);

        // 점선 스타일 적용 (lookup은 UI 렌더링 이후 호출 필요 → runLater 사용)
        javafx.application.Platform.runLater(() -> {
            applySeriesStyle(0, "-fx-stroke: red; -fx-stroke-dash-array: 8 5;");
            applySeriesStyle(1, "-fx-stroke: blue; -fx-stroke-dash-array: 4 6;");
        });
    }

    private void applySeriesStyle(int seriesIndex, String style) {
        Node line = lineChart.lookup(".chart-series-line.series" + seriesIndex);
        if (line != null) {
            line.setStyle(style);
        }
    }
}
