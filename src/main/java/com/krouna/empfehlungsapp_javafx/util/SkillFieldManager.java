package com.krouna.empfehlungsapp_javafx.util;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

/**
 * Utility class for managing skill-related UI controls
 */
public class SkillFieldManager {

    /**
     * Sets up the relationship between a skill checkbox and its related fields
     *
     * @param checkBox The checkbox that toggles visibility
     * @param percentField Field for skill percentage (may be null)
     * @param nameField Field for skill name (may be null)
     */
    public void setupSkillCheckbox(CheckBox checkBox, TextField percentField, TextField nameField) {
        // Initialize visibility
        if (percentField != null) percentField.setVisible(false);
        if (nameField != null) nameField.setVisible(false);

        // Add action listener
        checkBox.setOnAction(e -> {
            boolean selected = checkBox.isSelected();
            if (percentField != null) percentField.setVisible(selected);
            if (nameField != null) nameField.setVisible(selected);
        });
    }
}
