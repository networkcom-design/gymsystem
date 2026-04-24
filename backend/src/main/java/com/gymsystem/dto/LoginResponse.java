package com.gymsystem.dto;
public class LoginResponse {
    private final String token;
    private final String username;
    private final String nombre;
    public LoginResponse(String token, String username, String nombre) {
        this.token = token; this.username = username; this.nombre = nombre;
    }
    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getNombre() { return nombre; }
}
