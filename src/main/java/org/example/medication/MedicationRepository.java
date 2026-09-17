package org.example.medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
    @Query("SELECT medication FROM Medication medication WHERE medication.id = :id")
    Medication findMedication(@Param("id") Long id);
}
