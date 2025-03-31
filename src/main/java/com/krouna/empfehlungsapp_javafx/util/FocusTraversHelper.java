package com.krouna.empfehlungsapp_javafx.util;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;

public class FocusTraversHelper {

    static private ListChangeListener<Node> listChangeListener;
    static {
        listChangeListener = c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (Node added : c.getAddedSubList()) {
                        if (added != null && (added instanceof Parent || added instanceof Button)) {
                            cancelFocusTravers(added);
                        }
                    }
                }
            }
        };
    }

    static private void registerAddEvent(Parent parent) {
        parent.getChildrenUnmodifiable().removeListener(listChangeListener);
        parent.getChildrenUnmodifiable().addListener(listChangeListener);
    }

    static public void cancelFocusTravers(Node node) {
        if (node instanceof Parent) {
            registerAddEvent((Parent) node);
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                cancelFocusTravers(child);
            }
        }
        if (node instanceof Button) {
            node.setFocusTraversable(false);
        }
    }
}

