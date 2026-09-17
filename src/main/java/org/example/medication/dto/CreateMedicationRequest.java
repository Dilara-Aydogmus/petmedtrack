package org.example.medication.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateMedicationRequest {
    @NotBlank(message = "Medication name is required")
    private String name;
}
