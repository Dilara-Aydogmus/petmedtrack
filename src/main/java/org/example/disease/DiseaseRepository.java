package org.example.disease;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiseaseRepository extends JpaRepository<Disease, Long> {
    @Query("SELECT d FROM Disease d WHERE d.id = :id")
    Disease findDisease(@Param("id") Long id);
    Disease findByName(String name);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}
