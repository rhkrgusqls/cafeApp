module com.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires tyrus.standalone.client;

    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.ooxml.schemas;
    requires org.apache.xmlbeans;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.net.http;
    requires org.json;
    requires com.fasterxml.jackson.databind;
    requires spring.context;
    requires spring.boot;
    requires java.desktop;
    requires spring.core;
    requires java.sql;

    opens com.example.demo1 to javafx.fxml;
    opens com.example.demo1.controller to javafx.fxml;

    opens com.example.demo1.dto to com.fasterxml.jackson.databind, javafx.base;

    exports com.example.demo1.refresh to tyrus.standalone.client;
    exports com.example.demo1;
    exports com.example.demo1.controller;
    exports com.example.demo1.controller.util;
    opens com.example.demo1.controller.util to javafx.fxml;
}
