package com.finaccess.api.DTO;

import com.finaccess.api.model.Role;
import com.finaccess.api.model.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        UserResponse dto = new UserResponse();
        dto.id = user.getId();
        dto.username = user.getUsername();
        dto.email = user.getEmail();
        dto.role = user.getRole();
        dto.active = user.isActive();
        dto.createdAt = user.getCreatedAt();
        return dto;
    }
}
