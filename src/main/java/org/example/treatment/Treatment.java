package org.example.treatment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.medication.Medication;
import org.example.pet.Pet;
import org.example.treatment.state.ActiveTreatmentState;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.example.treatment.state.ActiveTreatmentState;
import org.example.treatment.state.TreatmentState;
import org.example.treatment.state.TreatmentStateFactory;

@Entity
@Table(name="treatments")
@Getter
@Setter
@NoArgsConstructor
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TreatmentStatus status = TreatmentStatus.ACTIVE;

    @Transient
    private TreatmentState state = new ActiveTreatmentState();

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(nullable = false)
    private String dosage;

    @Column(nullable = false)
    private String frequency;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Treatment(Pet pet, Medication medication, String dosage, String frequency, LocalDate startDate, LocalDate endDate){
        this.pet = pet;
        this.medication = medication;
        this.dosage = dosage;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = LocalDateTime.now();
    }

    @PostLoad
    private void restoreState(){
        this.state = TreatmentStateFactory.create(this.status);
    }

    public TreatmentStatus getStatus(){
        return status;
    }

    public void setStatus(TreatmentStatus status){
        this.status =status;
    }

    public void complete(){
        state.complete(this);
        state = TreatmentStateFactory.create(status);
    }

    public void cancel(){
        state.cancel(this);
        state = TreatmentStateFactory.create(status);
    }

    public void reactivate(){
        state.reactivate(this);
        state = TreatmentStateFactory.create(status);
    }
}
