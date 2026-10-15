package org.example.treatment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.medication.Medication;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "treatment_medications")
@Getter
@Setter
@NoArgsConstructor
public class TreatmentMedication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treatment_id", nullable = false)
    private Treatment treatment;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(nullable = false)
    private String dosage;

    @Column(nullable = false)
    private String frequency;

    public TreatmentMedication(Treatment treatment, Medication medication, String dosage, String frequency){
        this.treatment = treatment;
        this.medication = medication;
        this.dosage = dosage;
        this.frequency = frequency;
    }
}
