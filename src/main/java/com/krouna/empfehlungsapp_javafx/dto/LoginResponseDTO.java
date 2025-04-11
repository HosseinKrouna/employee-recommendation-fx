package com.krouna.empfehlungsapp_javafx.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Optional, aber nützlich

// Ignoriert unbekannte Felder, falls Backend mehr sendet als Frontend kennt
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponseDTO {
    private Long id;
    private String username;
    private String role;
    private String token;

    // Leerer Konstruktor (wichtig für Jackson)
    public LoginResponseDTO() {}

    // Optional: Konstruktor mit Feldern
    public LoginResponseDTO(Long id, String username, String role, String token) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.token = token;
    }

    // Getter und Setter für alle Felder
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}