package org.example.pet.specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.example.pet.Pet;
import org.springframework.data.jpa.domain.Specification;

public class PetSpeciesSpecification implements Specification<Pet> {
    private final String species;

    public PetSpeciesSpecification(String species){
        this.species = species;
    }
    @Override
    public Predicate toPredicate(Root<Pet> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder){
        if(species == null || species.isBlank()){
            return criteriaBuilder.conjunction();
        }
        String searchText = "%" + species.trim().toLowerCase() + "%";
        return criteriaBuilder.like(criteriaBuilder.lower(root.get("species")), searchText);
    }
}
