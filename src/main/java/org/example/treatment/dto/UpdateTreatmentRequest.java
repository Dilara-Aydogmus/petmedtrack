package org.example.treatment.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class UpdateTreatmentRequest {
    @Positive(message = "Pet id must be positive")
    private Long petId;
    @Positive(message = "Medication id must be positive")
    private Long medicationId;
    @Size(max = 100, message= "Dosage cannot exceed 100 characters")
    private String dosage;
    @Size(max = 100, message = "Frequency cannot exceed 100 characters")
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;

}
