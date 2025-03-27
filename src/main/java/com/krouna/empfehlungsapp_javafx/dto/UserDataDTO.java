package com.krouna.empfehlungsapp_javafx.dto;

public class UserDataDTO {
    private final long id;
    private final String username;

    public UserDataDTO(long id, String username) {
        this.id = id;
        this.username = username;
    }

    public long getId() { return id; }
    public String getUsername() { return username; }
}
