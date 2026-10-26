package org.example.clinic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicRepository extends JpaRepository<Clinic, Long>{
    Clinic findByName(String name);
}
