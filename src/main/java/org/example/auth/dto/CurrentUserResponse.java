package org.example.auth.dto;
import lombok.Getter;

@Getter
public class CurrentUserResponse {

    private final Long userId;
    private final String username;
    private final String role;
    private final Long veterinarianId;
    private final String veterinarianName;
    private final Long clinicId;
    private final String clinicName;

    public CurrentUserResponse(
            Long userId,
            String username,
            String role,
            Long veterinarianId,
            String veterinarianName,
            Long clinicId,
            String clinicName
    ) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.veterinarianId = veterinarianId;
        this.veterinarianName = veterinarianName;
        this.clinicId = clinicId;
        this.clinicName = clinicName;
    }
}
