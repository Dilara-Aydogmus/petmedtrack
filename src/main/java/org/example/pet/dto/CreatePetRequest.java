package org.example.pet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CreatePetRequest {

    @NotBlank(message = "Pet name is required")
    private String name;

    @NotBlank(message = "Species is required")
    private String species;

    private String breed;

    private Integer ageYears;

    private Integer ageMonths;

    private BigDecimal weight;

    private BigDecimal height;

    private boolean neutered;

   @NotBlank(message = "Owner first name is required")
    private String ownerFirstName;

   @NotBlank(message = "Owner last name is required")
    private String ownerLastName;
}
