package com.practise.userservice.dto;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long id;
    private String name;
    private String email;

    public AuthResponse(String accessToken, String refreshToken, Long id, String name, String email) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
