package org.example.auth.dto;
import lombok.Getter;

@Getter
public class UserResponse {
    private final Long id;
    private final String username;
    private final String role;

    public UserResponse(Long id, String username, String role){
        this.id = id;
        this.username = username;
        this.role = role;
    }
}
