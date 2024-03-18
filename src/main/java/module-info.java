module com.example.driveclient {
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

    //needed for HTTP
    requires unirest.java.core;
    requires unirest.modules.gson;

    //needed for JSON
    requires gson;
    requires java.sql;

    requires lombok;

    requires tus.java.client;
    requires tree;
    requires com.google.common;

    opens com.example.driveclient to javafx.fxml;
    exports com.example.driveclient;
    exports com.example.driveclient.dto;
    opens com.example.driveclient.dto to gson;
}