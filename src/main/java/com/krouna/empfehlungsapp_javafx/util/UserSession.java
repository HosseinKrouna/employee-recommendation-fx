package com.krouna.empfehlungsapp_javafx.util;

// Annahme: UserSession ist ein Singleton
public class UserSession {

    private static UserSession instance;

    private Long userId;
    private String username;
    private String token; // <-- NEUES FELD für das Authentifizierungs-Token

    // Privater Konstruktor für Singleton
    private UserSession() {}

    // Methode, um die Singleton-Instanz zu erhalten
    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // --- Bestehende Getter/Setter für userId und username ---
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // --- NEUE Methoden für das Token ---

    /**
     * Gibt das gespeicherte Authentifizierungs-Token zurück.
     * @return Das Token als String, oder null, wenn kein Token gesetzt ist.
     */
    public String getToken() { // <-- Die fehlende Methode
        return token;
    }

    /**
     * Setzt das Authentifizierungs-Token für die aktuelle Session.
     * Dies sollte nach erfolgreichem Login aufgerufen werden.
     * @param token Das vom Backend empfangene Token.
     */
    public void setToken(String token) { // <-- Methode zum Speichern des Tokens
        this.token = token;
    }

    /**
     * Löscht alle Benutzerdaten aus der Session (z.B. beim Logout).
     */
    public void clear() {
        this.userId = null;
        this.username = null;
        this.token = null; // <-- Auch das Token löschen
        // Setze die Instanz nicht auf null, nur die Daten darin
    }

    /**
     * Prüft, ob ein Benutzer (anhand des Tokens) angemeldet ist.
     * @return true, wenn ein Token vorhanden ist, sonst false.
     */
    public boolean isLoggedIn() {
        return this.token != null && !this.token.isBlank();
    }
}