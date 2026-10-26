package org.example.pet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PetRepository extends JpaRepository<Pet, Long>, JpaSpecificationExecutor<Pet> {

    @Query("SELECT pet FROM Pet pet WHERE pet.id = :id")
    Pet findPet(@Param("id") Long id);

    List<Pet> findByOwnerId(Long ownerId);
    Pet findPetByIdAndOwnerClinicId(Long petId, Long clinicId);
    List<Pet> findByOwnerClinicId(Long clinicId);
    Pet findPetByIdAndOwnerId(Long petId, Long ownerId);

}
