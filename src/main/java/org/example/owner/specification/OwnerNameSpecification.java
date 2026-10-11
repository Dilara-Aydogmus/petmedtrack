package org.example.owner.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.example.owner.Owner;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public class OwnerNameSpecification implements Specification<Owner> {
    private final String name;

    public OwnerNameSpecification(String name){
        this.name = name;
    }

    @Override
    public Predicate toPredicate(Root<Owner> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder){
        if(name == null || name.isBlank()){
            return criteriaBuilder.conjunction();
        }

        String searchText = "%" + name.trim().toLowerCase() + "%";
        Predicate firstNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchText);
        Predicate lastNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchText);

        return criteriaBuilder.or(firstNamePredicate, lastNamePredicate);
    }
}
