module com.krouna.empfehlungsapp_javafx {
    requires javafx.controls;
    requires javafx.fxml;


    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires com.fasterxml.jackson.databind;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires java.net.http;

    exports com.krouna.empfehlungsapp_javafx.dto;

    opens com.krouna.empfehlungsapp_javafx to javafx.fxml;
    exports com.krouna.empfehlungsapp_javafx;
    exports com.krouna.empfehlungsapp_javafx.controllers;
    opens com.krouna.empfehlungsapp_javafx.controllers to javafx.fxml;
}