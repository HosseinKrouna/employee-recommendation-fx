package com.krouna.empfehlungsapp_javafx.util;


public class UserSession {

    private static UserSession instance;

    private Long userId;
    private String username;
    private String token;


    private UserSession() {}


    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }


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


    public String getToken() { // <-- Die fehlende Methode
        return token;
    }


    public void setToken(String token) { // <-- Methode zum Speichern des Tokens
        this.token = token;
    }


    public void clear() {
        this.userId = null;
        this.username = null;
        this.token = null;

    }


    public boolean isLoggedIn() {
        return this.token != null && !this.token.isBlank();
    }
}