package com.krouna.empfehlungsapp_javafx.util;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class SkillInput {
    private CheckBox checkBox;
    private TextField percentField;
    private TextField nameField;

    public SkillInput(CheckBox checkBox, TextField percentField) {
        this(checkBox, percentField, null);
    }

    public SkillInput(CheckBox checkBox, TextField percentField, TextField nameField) {
        this.checkBox = checkBox;
        this.percentField = percentField;
        this.nameField = nameField;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public TextField getPercentField() {
        return percentField;
    }

    public TextField getNameField() {
        return nameField;
    }
}

