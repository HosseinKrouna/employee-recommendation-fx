package com.krouna.empfehlungsapp_javafx.util; // Oder ein anderer passender Ort

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class BackendProcessManager {

    private Process backendProcess;
    private final String jarPath; // Pfad zur Spring Boot JAR-Datei
    private final String healthCheckUrl; // z.B. "http://localhost:8080/actuator/health"
    private final int maxWaitSeconds;
    private final boolean redirectOutput; // Sollen Logs umgeleitet werden?

    public BackendProcessManager(String jarPath, String healthCheckUrl, int maxWaitSeconds, boolean redirectOutput) {
        // Normalisiere den Pfad, um plattformunabhängiger zu sein
        this.jarPath = Paths.get(jarPath).toAbsolutePath().toString();
        this.healthCheckUrl = healthCheckUrl;
        this.maxWaitSeconds = maxWaitSeconds;
        this.redirectOutput = redirectOutput;
    }

    public boolean startBackend() {
        if (isBackendRunning()) {
            System.out.println("Backend scheint bereits zu laufen (Port oder Health Check erfolgreich). Überspringe Start.");
            return true;
        }

        System.out.println("Versuche, Backend zu starten von: " + jarPath);
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarPath);

        // Optional: Arbeitsverzeichnis setzen (falls die JAR relative Pfade erwartet)
        // Path jarDirectory = Paths.get(jarPath).getParent();
        // if (jarDirectory != null) {
        //     pb.directory(jarDirectory.toFile());
        // }

        if (redirectOutput) {
            // Leite die Standardausgabe und Fehlerausgabe des Backend-Prozesses
            // an die Ausgabe des aktuellen JavaFX-Prozesses weiter.
            // Alternativ: Schreibe in eine Log-Datei.
            pb.redirectOutput(Redirect.INHERIT);
            pb.redirectError(Redirect.INHERIT);
            System.out.println("Backend Logs werden auf dieser Konsole ausgegeben.");
        } else {
            System.out.println("Backend Logs werden nicht umgeleitet.");
            // Standardmäßig werden die Streams nicht angezeigt,
            // es sei denn, man liest sie manuell aus (siehe unten für ein Beispiel).
        }


        try {
            backendProcess = pb.start();
            System.out.println("Backend-Prozess gestartet (PID könnte sein: " + backendProcess.pid() + ")"); // pid() ab Java 9

            // Warte darauf, dass das Backend bereit ist
            boolean ready = waitForBackendReady();
            if (!ready) {
                System.err.println("Backend ist nach " + maxWaitSeconds + " Sekunden nicht bereit. Stoppe Prozess.");
                stopBackend(); // Versuche, den fehlgeschlagenen Prozess zu stoppen
                return false;
            }
            System.out.println("Backend erfolgreich gestartet und bereit.");
            return true;

        } catch (IOException e) {
            System.err.println("Fehler beim Starten des Backend-Prozesses: " + e.getMessage());
            e.printStackTrace();
            backendProcess = null; // Stelle sicher, dass der Prozess null ist
            return false;
        } catch (InterruptedException e) {
            System.err.println("Warten auf Backend wurde unterbrochen.");
            Thread.currentThread().interrupt(); // Wichtig: Interrupt-Status wiederherstellen
            stopBackend(); // Versuche aufzuräumen
            return false;
        }
    }

    private boolean waitForBackendReady() throws InterruptedException {
        System.out.println("Warte auf Backend unter " + healthCheckUrl + " (max. " + maxWaitSeconds + "s)...");
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // Kurzer Timeout für jeden Check
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(healthCheckUrl))
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (maxWaitSeconds * 1000L);

        while (System.currentTimeMillis() < endTime) {
            if (backendProcess != null && !backendProcess.isAlive()) {
                System.err.println("Backend-Prozess ist unerwartet beendet worden.");
                return false; // Prozess ist weg
            }
            try {
                // Sende den Health Check Request
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                // Prüfe, ob Status 200 OK ist (oder ein anderer erwarteter Code)
                if (response.statusCode() == 200) {
                    // Optional: Prüfe den Body auf "UP"
                    // if (response.body() != null && response.body().contains("\"status\":\"UP\"")) {
                    System.out.println("Backend Health Check erfolgreich (Status: " + response.statusCode() + ").");
                    return true;
                    // } else {
                    //     System.out.println("Backend Health Check Status 200, aber Body nicht 'UP': " + response.body());
                    // }
                } else {
                    System.out.println("Backend Health Check fehlgeschlagen (Status: " + response.statusCode() + ")");
                }
            } catch (ConnectException e) {
                // Server noch nicht erreichbar (normal während des Starts)
                System.out.print("."); // Fortschritt anzeigen
            } catch (IOException e) {
                // Andere Netzwerkfehler
                System.err.println("\nIOException beim Health Check: " + e.getMessage());
                // Hier könnte man entscheiden, ob man weiter wartet oder abbricht
            }

            // Warte kurz, bevor der nächste Check erfolgt
            Thread.sleep(1000); // 1 Sekunde warten
        }

        System.err.println("\nTimeout: Backend nicht innerhalb von " + maxWaitSeconds + " Sekunden bereit.");
        return false; // Timeout erreicht
    }

    // Methode zum Prüfen, ob der Port bereits belegt ist ODER der Health Check erfolgreich ist
    public boolean isBackendRunning() {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(1)) // Sehr kurzer Timeout
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(healthCheckUrl))
                .timeout(Duration.ofSeconds(1))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200; // Oder ein anderer Erfolgscode
        } catch (IOException | InterruptedException e) {
            // Fehler bedeutet wahrscheinlich, dass es nicht läuft (oder Netzwerkproblem)
            return false;
        }
    }


    public void stopBackend() {
        if (backendProcess != null && backendProcess.isAlive()) {
            System.out.println("Versuche, Backend-Prozess zu stoppen...");
            backendProcess.destroy(); // Sendet SIGTERM (normales Beenden)
            try {
                // Warte kurz, ob der Prozess von selbst endet
                boolean exited = backendProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
                if (!exited) {
                    System.out.println("Backend-Prozess nicht normal beendet, erzwinge Stopp (destroyForcibly)...");
                    backendProcess.destroyForcibly(); // Sendet SIGKILL
                }
                System.out.println("Backend-Prozess gestoppt.");
            } catch (InterruptedException e) {
                System.err.println("Warten auf Prozessende unterbrochen.");
                backendProcess.destroyForcibly(); // Im Zweifel hart beenden
                Thread.currentThread().interrupt();
            }
        } else {
            System.out.println("Backend-Prozess war nicht aktiv oder bereits gestoppt.");
        }
        backendProcess = null; // Referenz entfernen
    }

    // Alternative, um Output manuell zu lesen (wenn redirectOutput = false)
    private void readProcessOutput(Process process) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Backend OUT: " + line);
                }
            } catch (IOException e) {
                // Prozess wahrscheinlich beendet
            }
        }).start();

        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("Backend ERR: " + line);
                }
            } catch (IOException e) {
                // Prozess wahrscheinlich beendet
            }
        }).start();
    }
}