package org.suraksha.dto;

/**
 * Authentication response payload returning the signed JWT and safe user details.
 */
public class AuthResponseDto {

    private String token;
    private String tokenType = "Bearer";
    private long expiresInMs;
    private UserProfileDto user;

    public AuthResponseDto() {}

    public AuthResponseDto(String token, long expiresInMs, UserProfileDto user) {
        this.token = token;
        this.tokenType = "Bearer";
        this.expiresInMs = expiresInMs;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public long getExpiresInMs() { return expiresInMs; }
    public void setExpiresInMs(long expiresInMs) { this.expiresInMs = expiresInMs; }

    public UserProfileDto getUser() { return user; }
    public void setUser(UserProfileDto user) { this.user = user; }
}
