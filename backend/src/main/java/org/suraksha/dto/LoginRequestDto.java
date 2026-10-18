package org.suraksha.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Login request payload containing user credentials.
 */
public class LoginRequestDto {

    @NotBlank(message = "Email address is required")
    @Email(message = "Email address must be a valid format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequestDto() {}

    public LoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
