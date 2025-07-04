package com.krouna.empfehlungsapp_javafx.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.file.Paths;
import java.time.Duration;

public class BackendProcessManager {

    private Process backendProcess;
    private final String jarPath;
    private final String healthCheckUrl;
    private final int maxWaitSeconds;
    private final boolean redirectOutput;

    public BackendProcessManager(String jarPath, String healthCheckUrl, int maxWaitSeconds, boolean redirectOutput) {

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



        if (redirectOutput) {

            pb.redirectOutput(Redirect.INHERIT);
            pb.redirectError(Redirect.INHERIT);
            System.out.println("Backend Logs werden auf dieser Konsole ausgegeben.");
        } else {
            System.out.println("Backend Logs werden nicht umgeleitet.");

        }


        try {
            backendProcess = pb.start();
            System.out.println("Backend-Prozess gestartet (PID könnte sein: " + backendProcess.pid() + ")"); // pid() ab Java 9


            boolean ready = waitForBackendReady();
            if (!ready) {
                System.err.println("Backend ist nach " + maxWaitSeconds + " Sekunden nicht bereit. Stoppe Prozess.");
                stopBackend();
                return false;
            }
            System.out.println("Backend erfolgreich gestartet und bereit.");
            return true;

        } catch (IOException e) {
            System.err.println("Fehler beim Starten des Backend-Prozesses: " + e.getMessage());
            e.printStackTrace();
            backendProcess = null;
            return false;
        } catch (InterruptedException e) {
            System.err.println("Warten auf Backend wurde unterbrochen.");
            Thread.currentThread().interrupt();
            stopBackend();
            return false;
        }
    }

    private boolean waitForBackendReady() throws InterruptedException {
        System.out.println("Warte auf Backend unter " + healthCheckUrl + " (max. " + maxWaitSeconds + "s)...");
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
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
                return false;
            }
            try {

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {

                    System.out.println("Backend Health Check erfolgreich (Status: " + response.statusCode() + ").");
                    return true;

                } else {
                    System.out.println("Backend Health Check fehlgeschlagen (Status: " + response.statusCode() + ")");
                }
            } catch (ConnectException e) {

                System.out.print(".");
            } catch (IOException e) {

                System.err.println("\nIOException beim Health Check: " + e.getMessage());

            }


            Thread.sleep(1000);
        }

        System.err.println("\nTimeout: Backend nicht innerhalb von " + maxWaitSeconds + " Sekunden bereit.");
        return false;
    }


    public boolean isBackendRunning() {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(1))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(healthCheckUrl))
                .timeout(Duration.ofSeconds(1))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {

            return false;
        }
    }


    public void stopBackend() {
        if (backendProcess != null && backendProcess.isAlive()) {
            System.out.println("Versuche, Backend-Prozess zu stoppen...");
            backendProcess.destroy();
            try {

                boolean exited = backendProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
                if (!exited) {
                    System.out.println("Backend-Prozess nicht normal beendet, erzwinge Stopp (destroyForcibly)...");
                    backendProcess.destroyForcibly();
                }
                System.out.println("Backend-Prozess gestoppt.");
            } catch (InterruptedException e) {
                System.err.println("Warten auf Prozessende unterbrochen.");
                backendProcess.destroyForcibly();
                Thread.currentThread().interrupt();
            }
        } else {
            System.out.println("Backend-Prozess war nicht aktiv oder bereits gestoppt.");
        }
        backendProcess = null;
    }


    private void readProcessOutput(Process process) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Backend OUT: " + line);
                }
            } catch (IOException e) {

            }
        }).start();

        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("Backend ERR: " + line);
                }
            } catch (IOException e) {

            }
        }).start();
    }
}