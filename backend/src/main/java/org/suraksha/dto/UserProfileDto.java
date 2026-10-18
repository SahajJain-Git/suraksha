package org.suraksha.dto;

import org.suraksha.entity.Role;
import org.suraksha.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Safe User Profile DTO without confidential fields (no passwordHash).
 */
public class UserProfileDto {

    private Long id;
    private String fullName;
    private String email;
    private Set<String> roles;
    private Boolean enabled;

    public UserProfileDto() {}

    public UserProfileDto(Long id, String fullName, String email, Set<String> roles, Boolean enabled) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.roles = roles;
        this.enabled = enabled;
    }

    public static UserProfileDto fromEntity(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        return new UserProfileDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                roleNames,
                user.getEnabled()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
