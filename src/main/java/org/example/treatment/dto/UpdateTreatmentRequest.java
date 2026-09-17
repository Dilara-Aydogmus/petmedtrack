package org.example.treatment.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UpdateTreatmentRequest {
    private Long petId;
    private Long medicationId;
    private String dosage;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;

}
