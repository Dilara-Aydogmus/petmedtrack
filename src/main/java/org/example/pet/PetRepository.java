package org.example.pet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {

    @Query("SELECT pet FROM Pet pet WHERE pet.id = :id")
    Pet findPet(@Param("id") Long id);

    List<Pet> findByOwnerId(Long ownerId);

}
