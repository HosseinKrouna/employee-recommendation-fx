package com.krouna.empfehlungsapp_javafx.util;

public class HttpResponse {

    private final int statusCode; // Ist wahrscheinlich private
    private final String body;    // Ist wahrscheinlich private

    public HttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    // --- GETTER hinzufügen (oder sicherstellen, dass sie public sind) ---

    /**
     * Gibt den HTTP-Statuscode der Antwort zurück.
     * @return Der Statuscode.
     */
    public int getStatusCode() { // <-- Public Getter für statusCode
        return statusCode;
    }

    /**
     * Gibt den Body der HTTP-Antwort als String zurück.
     * @return Der Body, oder null.
     */
    public String getBody() { // <-- Public Getter für body
        return body;
    }

    /**
     * Hilfsmethode, um zu prüfen, ob die Anfrage erfolgreich war (z.B. 2xx Statuscode).
     * @return true, wenn der Statuscode im Bereich 200-299 liegt, sonst false.
     */
    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
}