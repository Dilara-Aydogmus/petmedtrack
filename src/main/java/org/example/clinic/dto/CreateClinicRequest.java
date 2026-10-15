package org.example.clinic.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateClinicRequest {
    @NotBlank(message = "Clinic name is required")
    private String name;

    private String address;
    @NotBlank(message = "Clinic phone number is required")
    private String phoneNumber;
}
