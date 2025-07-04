package com.krouna.empfehlungsapp_javafx.util;

import javafx.scene.Node;


public class UIUtils {


    public static void setVisibilityAndManaged(Node node, boolean value) {
        node.setVisible(value);
        node.setManaged(value);
    }
}