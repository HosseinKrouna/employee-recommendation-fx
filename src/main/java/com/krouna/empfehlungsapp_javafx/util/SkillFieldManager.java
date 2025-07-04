package com.krouna.empfehlungsapp_javafx.util;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;


public class SkillFieldManager {


    public void setupSkillCheckbox(CheckBox checkBox, TextField percentField, TextField nameField) {

        if (percentField != null) percentField.setVisible(false);
        if (nameField != null) nameField.setVisible(false);

        checkBox.setOnAction(e -> {
            boolean selected = checkBox.isSelected();
            if (percentField != null) percentField.setVisible(selected);
            if (nameField != null) nameField.setVisible(selected);
        });
    }
}
