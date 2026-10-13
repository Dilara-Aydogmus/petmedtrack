package org.example.treatment.dto;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
public class UpdateTreatmentRequest {
    private Long petId;
    private Set<Long> diseaseIds;
    @Valid
    private List<TreatmentMedicationRequest> medications;
    private LocalDate startDate;
    private LocalDate endDate;

    private LocalDateTime nextCheckupAt;
}
