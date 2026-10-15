package org.example.pet.specification;
import jakarta.persistence.criteria.*;
import org.example.clinic.Clinic;
import org.example.owner.Owner;
import org.example.pet.Pet;
import org.springframework.data.jpa.domain.Specification;

public class PetClinicSpecification implements Specification<Pet>{

    private final Long clinicId;

    public PetClinicSpecification(Long clinicId){
            this.clinicId = clinicId;
    }

    @Override
    public Predicate toPredicate(Root<Pet> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder){
        Join<Pet, Owner> ownerJoin = root.join("owner");

        Join<Owner,Clinic> clinicJoin = ownerJoin.join("clinic");

        return criteriaBuilder.equal(clinicJoin.get("id"), clinicId);
    }

}
