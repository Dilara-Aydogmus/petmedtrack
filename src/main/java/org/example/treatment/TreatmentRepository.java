package org.example.treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {
    @Query("SELECT treatment FROM Treatment treatment WHERE treatment.id = :id")
    Treatment findTreatment(@Param("id") Long id);
}
