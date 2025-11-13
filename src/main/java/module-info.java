module com.example.table {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires javafx.graphics;
    requires org.tinylog.api;
    requires java.desktop;
    requires com.google.gson;
    requires annotations;

    opens hu.unideb.table to javafx.fxml;
    exports hu.unideb.table.controller;
    opens hu.unideb.table.controller to javafx.fxml;
    exports hu.unideb.table.view;
    opens hu.unideb.table.view to javafx.fxml;
    opens hu.unideb.table.model.persistence to com.google.gson;
    exports hu.unideb.table.model;
    exports com.example.table;
    exports hu.unideb.table.model.persistence;
    opens com.example.table to javafx.fxml;
}