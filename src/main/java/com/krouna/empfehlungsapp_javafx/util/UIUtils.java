package com.krouna.empfehlungsapp_javafx.util;

import javafx.scene.Node;

/**
 * Utility class for common UI operations
 */
public class UIUtils {

    /**
     * Sets both visible and managed properties of a JavaFX node
     *
     * @param node The node to modify
     * @param value Value to set for both visible and managed properties
     */
    public static void setVisibilityAndManaged(Node node, boolean value) {
        node.setVisible(value);
        node.setManaged(value);
    }
}