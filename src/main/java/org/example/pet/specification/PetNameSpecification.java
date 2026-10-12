package org.example.pet.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.example.pet.Pet;
import org.springframework.data.jpa.domain.Specification;

public class PetNameSpecification
        implements Specification<Pet> {

    private final String name;

    public PetNameSpecification(String name) {
        this.name = name;
    }

    @Override
    public Predicate toPredicate(
            Root<Pet> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder
    ) {
        if (name == null || name.isBlank()) {
            return criteriaBuilder.conjunction();
        }

        String searchText =
                "%" + name.trim().toLowerCase() + "%";

        return criteriaBuilder.like(
                criteriaBuilder.lower(
                        root.get("name")
                ),
                searchText
        );
    }
}
