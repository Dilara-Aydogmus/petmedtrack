package org.example.disease.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateDiseaseRequest {

    @NotBlank(message = "Disease name is required")
    private String name;
}
