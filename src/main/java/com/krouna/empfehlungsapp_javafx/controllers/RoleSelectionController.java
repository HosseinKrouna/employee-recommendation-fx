package com.krouna.empfehlungsapp_javafx.controllers;


import com.krouna.empfehlungsapp_javafx.util.SceneUtil;
import javafx.event.ActionEvent;


public class RoleSelectionController {

    public void handleEmployeeLogin(ActionEvent event) {
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-login-view.fxml");
    }

    public void handleHRLogin(ActionEvent event) {
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/hr-login-view.fxml");
    }

}
