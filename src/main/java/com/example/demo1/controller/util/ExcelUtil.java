package com.example.demo1.controller.util;
import com.example.demo1.dto.ShipmentDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFFillProperties;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData.Series;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDPt;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieSer;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;

import java.util.Date;
import java.util.List;

public class ExcelUtil {

    public static void createSingleColorLineChart(XSSFWorkbook workbook, XSSFSheet sheet, List<Date> dates, List<Double> values, String hexColor) {
        // 날짜/수량 데이터 입력
        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-MM-dd"));

        // 헤더
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Date");
        header.createCell(1).setCellValue("Value");

        for (int i = 0; i < dates.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Cell dateCell = row.createCell(0);
            dateCell.setCellValue(dates.get(i));
            dateCell.setCellStyle(dateStyle);
            row.createCell(1).setCellValue(values.get(i));
        }

        // 차트 생성
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 3, 1, 15, 20);
        XSSFChart chart = drawing.createChart(anchor);

        chart.setTitleText("Single Color Line Chart");
        chart.setTitleOverlay(false);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.BOTTOM);

        // 데이터 소스
        int lastRow = dates.size();
        XDDFCategoryDataSource xs = XDDFDataSourcesFactory.fromStringCellRange(sheet, new CellRangeAddress(1, lastRow, 0, 0));
        XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(1, lastRow, 1, 1));

        XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE,
                chart.createCategoryAxis(AxisPosition.BOTTOM),
                chart.createValueAxis(AxisPosition.LEFT));

        XDDFLineChartData.Series series = (XDDFLineChartData.Series) data.addSeries(xs, ys);
        series.setTitle("Value", null);

        // 선을 직선으로 설정
        series.setSmooth(false);

        // 색상 변환 (#RRGGBB → byte[])
        byte[] rgb = hexStringToByteArray(hexColor);
        XDDFColor color = XDDFColor.from(rgb);

        // 선 색상 설정
        XDDFSolidFillProperties fill = new XDDFSolidFillProperties(color);
        XDDFLineProperties lineProperties = new XDDFLineProperties();
        lineProperties.setFillProperties(fill);
        series.setLineProperties(lineProperties);

        // 마커 모양, 크기, 색상 설정
        series.setMarkerStyle(MarkerStyle.CIRCLE);
        series.setMarkerSize((short) 6);

        // 점 채우기 색상 설정
        // POI 5.x 버전에서는 직접 MarkerFill API가 없어 아래처럼 내부 XML 접근이 필요하지만 위험할 수 있음.
        // 대신 기본 색상 유지 혹은 마커 색상 무시 가능.
        // 필요하면 아래 코드 참고 (XMLBeans 직접 조작)
    /*
    CTLineSer ctLineSer = series.getCTLineSer();
    CTMarker ctMarker = ctLineSer.isSetMarker() ? ctLineSer.getMarker() : ctLineSer.addNewMarker();
    CTSolidColorFillProperties solidFill = ctMarker.isSetSpPr() ? ctMarker.getSpPr().getSolidFill() : ctMarker.getSpPr().addNewSolidFill();
    CTSRgbColor srgbClr = solidFill.isSetSrgbClr() ? solidFill.getSrgbClr() : solidFill.addNewSrgbClr();
    srgbClr.setVal(rgb);
    */

        chart.plot(data);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    // 2. 막대그래프 (날짜, 수량) - 이전보다 많으면 파란색, 적으면 빨간색, 최초는 파란색
    public static void createColoredBarChart(XSSFWorkbook workbook, XSSFSheet sheet, List<Date> dates, List<Double> values) {
        // 데이터 입력 및 헤더 작성
        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-MM-dd"));

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Date");
        header.createCell(1).setCellValue("Value");

        for (int i = 0; i < dates.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Cell dateCell = row.createCell(0);
            dateCell.setCellValue(dates.get(i));
            dateCell.setCellStyle(dateStyle);
            row.createCell(1).setCellValue(values.get(i));
        }

        // 차트 생성
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 3, 1, 15, 20);
        XSSFChart chart = drawing.createChart(anchor);

        chart.setTitleText("Colored Bar Chart");
        chart.setTitleOverlay(false);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.BOTTOM);

        // 바 차트 축
        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFBarChartData data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        data.setBarDirection(BarDirection.COL);

        // 날짜 카테고리

        XDDFCategoryDataSource xs = XDDFDataSourcesFactory.fromStringCellRange(sheet, new CellRangeAddress(1, dates.size(), 0, 0));

        // 각 값마다 시리즈를 별도 생성 (개별 색상 지정 위해)
        Double prevValue = null;
        for (int i = 0; i < values.size(); i++) {
            double val = values.get(i);

            XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(i + 1, i + 1, 1, 1));
            XDDFBarChartData.Series series = (XDDFBarChartData.Series) data.addSeries(xs, ys);

            series.setTitle("Value " + (i + 1), null);

            // 색상 결정
            byte[] colorBytes;
            if (prevValue == null || val >= prevValue) {
                // 파란색
                colorBytes = new byte[]{0, 0, (byte) 255};
            } else {
                // 빨간색
                colorBytes = new byte[]{(byte) 255, 0, 0};
            }
            prevValue = val;

            series.setFillProperties(new XDDFSolidFillProperties(XDDFColor.from(colorBytes)));
        }

        chart.plot(data);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    // 3. 점선그래프 (날짜, 수량, 소모타입) - 소모타입에 따라 색상 지정
    public static void createTypeColoredLineChart(XSSFWorkbook workbook, XSSFSheet sheet,
                                                  List<Date> dates, List<Double> values, List<String> types,
                                                  Map<String, String> typeColorMap) {
        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-MM-dd"));

        // 헤더
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Date");
        header.createCell(1).setCellValue("Value");
        header.createCell(2).setCellValue("Type");

        // 데이터 작성
        for (int i = 0; i < dates.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Cell dateCell = row.createCell(0);
            dateCell.setCellValue(dates.get(i));
            dateCell.setCellStyle(dateStyle);
            row.createCell(1).setCellValue(values.get(i));
            row.createCell(2).setCellValue(types.get(i));
        }

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 1, 20, 25);
        XSSFChart chart = drawing.createChart(anchor);

        chart.setTitleText("Type Colored Line Chart");
        chart.setTitleOverlay(false);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.BOTTOM);

        // 소모 타입 별로 시리즈 생성
        Map<String, List<Integer>> typeIndicesMap = new HashMap<>();
        for (int i = 0; i < types.size(); i++) {
            typeIndicesMap.computeIfAbsent(types.get(i), k -> new ArrayList<>()).add(i);
        }

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);

        XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

        for (Map.Entry<String, List<Integer>> entry : typeIndicesMap.entrySet()) {
            String type = entry.getKey();
            List<Integer> indices = entry.getValue();

            // 각 타입 데이터만 별도의 영역에 씀
            int startRow = sheet.getLastRowNum() + 2;

            Row headerRow = sheet.createRow(startRow);
            headerRow.createCell(0).setCellValue("Date");
            headerRow.createCell(1).setCellValue("Value");

            for (int i = 0; i < indices.size(); i++) {
                int idx = indices.get(i);
                Row row = sheet.createRow(startRow + i + 1);
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(dates.get(idx));
                dateCell.setCellStyle(dateStyle);
                row.createCell(1).setCellValue(values.get(idx));
            }
            XDDFCategoryDataSource xs = XDDFDataSourcesFactory.fromStringCellRange(sheet, new CellRangeAddress(startRow + 1, startRow + indices.size(), 0, 0));
            XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                    new CellRangeAddress(startRow + 1, startRow + indices.size(), 1, 1));

            XDDFLineChartData.Series series = (XDDFLineChartData.Series) data.addSeries(xs, ys);
            series.setTitle(type, null);

            // 타입 색상 설정
            String hexColor = typeColorMap.getOrDefault(type, "#000000");
            byte[] rgb = hexStringToByteArray(hexColor);
            XDDFSolidFillProperties fill = new XDDFSolidFillProperties(XDDFColor.from(rgb));
            XDDFLineProperties lineProperties = new XDDFLineProperties();
            lineProperties.setFillProperties(fill);
            series.setLineProperties(lineProperties);

            // 마커
            series.setMarkerStyle(MarkerStyle.CIRCLE);
            series.setMarkerSize((short) 6);
        }

        chart.plot(data);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    // 4. 원형 그래프 (수량, 소모타입) - 타입별 색상 지정
    public static void createPieChart(XSSFWorkbook workbook, XSSFSheet sheet,
                                      List<String> types, List<Double> values,
                                      Map<String, String> typeColorMap) {

        // 소모타입별 총합 계산
        Map<String, Double> sumMap = new HashMap<>();
        for (int i = 0; i < types.size(); i++) {
            sumMap.put(types.get(i), sumMap.getOrDefault(types.get(i), 0.0) + values.get(i));
        }

        // 헤더
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Type");
        header.createCell(1).setCellValue("Sum");

        int rowIdx = 1;
        for (Map.Entry<String, Double> entry : sumMap.entrySet()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }

        // 차트 생성
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 3, 1, 13, 20);
        XSSFChart chart = drawing.createChart(anchor);

        chart.setTitleText("Pie Chart by Type");
        chart.setTitleOverlay(false);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.RIGHT);

        int lastRow = sumMap.size();

        XDDFCategoryDataSource categories = XDDFDataSourcesFactory.fromStringCellRange(sheet, new CellRangeAddress(1, lastRow, 0, 0));
        XDDFNumericalDataSource<Double> valuesData = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(1, lastRow, 1, 1));

        XDDFPieChartData data = (XDDFPieChartData) chart.createData(ChartTypes.PIE, null, null);

        XDDFPieChartData.Series series = (XDDFPieChartData.Series) data.addSeries(categories, valuesData);
        series.setTitle("Types", null);
// 각 파이 조각 색상 지정
        CTPieSer ctPieSer = ((XDDFPieChartData.Series) series).getCTPieSer();
        int idx = 0;
        for (String type : sumMap.keySet()) {
            String hex = typeColorMap.getOrDefault(type, "#000000");
            byte[] rgb = hexStringToByteArray(hex);

            CTDPt dPt;
            if (idx < ctPieSer.sizeOfDPtArray()) {
                dPt = ctPieSer.getDPtArray(idx);
            } else {
                dPt = ctPieSer.addNewDPt();
                dPt.addNewIdx().setVal(idx);
            }


            CTShapeProperties spPr = dPt.isSetSpPr() ? dPt.getSpPr() : dPt.addNewSpPr();

            CTSolidColorFillProperties solidFill = spPr.isSetSolidFill() ? spPr.getSolidFill() : spPr.addNewSolidFill();
            CTSRgbColor rgbColor = solidFill.isSetSrgbClr() ? solidFill.getSrgbClr() : solidFill.addNewSrgbClr();

            rgbColor.setVal(rgb);

            idx++;
        }

        chart.plot(data);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

    }


    // 헬퍼: #FFFFFF 같은 16진수 문자열을 byte[3] RGB 배열로 변환
    private static byte[] hexStringToByteArray(String s) {
        s = s.replace("#", "");
        if (s.length() != 6) throw new IllegalArgumentException("Invalid hex color: " + s);
        byte[] bytes = new byte[3];
        for (int i = 0; i < 3; i++) {
            bytes[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    // 테스트용 main 예제
    public static void main(String[] args) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // 임의 데이터 (나중에 교체)
        List<Date> dates = Arrays.asList(new Date(), new Date(System.currentTimeMillis() - 86400000L * 1), new Date(System.currentTimeMillis() - 86400000L * 2));
        List<Double> values = Arrays.asList(10.0, 20.0, 15.0);
        List<String> types = Arrays.asList("소모", "폐기", "소모");

        Map<String, String> typeColorMap = new HashMap<>();
        typeColorMap.put("소모", "#0000FF");
        typeColorMap.put("폐기", "#FF0000");

        // 시트별로 함수 호출 (빈 시트 생성 후)
        createSingleColorLineChart(workbook, workbook.createSheet("LineChart1"), dates, values, "#000000");
        createColoredBarChart(workbook, workbook.createSheet("BarChart"), dates, values);
        createTypeColoredLineChart(workbook, workbook.createSheet("LineChart2"), dates, values, types, typeColorMap);
        createPieChart(workbook, workbook.createSheet("PieChart"), types, values, typeColorMap);

        try (FileOutputStream out = new FileOutputStream("chart-example.xlsx")) {
            workbook.write(out);
        }
        workbook.close();
    }
}