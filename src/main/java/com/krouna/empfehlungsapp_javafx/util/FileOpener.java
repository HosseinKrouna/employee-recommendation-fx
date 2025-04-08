package com.krouna.empfehlungsapp_javafx.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class FileOpener {

    public static void openFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("Datei nicht gefunden: " + filePath);
                return;
            }

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                System.err.println("Desktop-Unterstützung fehlt auf diesem System.");
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Öffnen der Datei: " + e.getMessage());
        }
    }
}
