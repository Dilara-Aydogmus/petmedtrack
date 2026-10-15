package org.example.owner.specification;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.example.clinic.Clinic;
import org.example.owner.Owner;
import org.springframework.data.jpa.domain.Specification;

public class OwnerClinicSpecification
{
    private OwnerClinicSpecification(){
    }

    public static Specification<Owner> belongsToClinic(Long clinicId){
        return (root, query, criteriaBuilder) -> {
            Join<Owner, Clinic> clinicJoin = root.join("clinic");
            Predicate clinicIdPredicate = criteriaBuilder.equal(clinicJoin.get("id"), clinicId);
            return clinicIdPredicate;
        };
    }
}
