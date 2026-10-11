package org.example.treatment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.disease.Disease;
import org.example.medication.Medication;
import org.example.pet.Pet;
import org.example.treatment.state.ActiveTreatmentState;
import org.example.treatment.state.TreatmentState;
import org.example.treatment.state.TreatmentStateFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="treatments")
@Getter
@Setter
@NoArgsConstructor
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToMany
    @JoinTable(name = "treatment_diseases", joinColumns = @JoinColumn(name = "treatment_id"), inverseJoinColumns = @JoinColumn(name = "disease_id"))
    private Set<Disease> diseases = new HashSet<>();

    @OneToMany(mappedBy = "treatment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TreatmentMedication> treatmentMedications = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TreatmentStatus status = TreatmentStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Transient
    @JsonIgnore
    private TreatmentState state = new ActiveTreatmentState();

    public Treatment(Pet pet, Set<Disease> diseases, LocalDate startDate, LocalDate endDate){
        this.pet = pet;
        this.diseases = diseases;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = LocalDateTime.now();
        this.status = TreatmentStatus.ACTIVE;
        this.state = new ActiveTreatmentState();
    }

    public void addMedication(Medication medication, String dosage, String frequency){
        TreatmentMedication treatmentMedication = new TreatmentMedication(this, medication, dosage, frequency);
        treatmentMedications.add(treatmentMedication);
    }

    @PostLoad
    private void restoreState(){
        this.state = TreatmentStateFactory.create(this.status);
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