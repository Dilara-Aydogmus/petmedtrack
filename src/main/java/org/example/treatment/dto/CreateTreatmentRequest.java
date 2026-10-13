package org.example.treatment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class CreateTreatmentRequest {

    @NotNull(message = "Pet id is required")
    private Long petId;

    @NotEmpty(message = "At least one disease is required")
    private Set<Long> diseaseIds = new HashSet<>();

    @Valid
    @NotEmpty(message = "At least one medication is required")
    private List<TreatmentMedicationRequest> medications;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime nextCheckupAt;
}
