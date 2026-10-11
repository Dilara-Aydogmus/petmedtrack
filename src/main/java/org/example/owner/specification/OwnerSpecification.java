package org.example.owner.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.example.owner.Owner;
import org.example.pet.Pet;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public class OwnerSpecification {
    private OwnerSpecification() {
    }
        public static Specification<Owner> nameContains(final String name){
            return new Specification<Owner>(){

                @Override
                public Predicate toPredicate(
                        Root<Owner> root,
                        CriteriaQuery<?> query,
                        CriteriaBuilder criteriaBuilder){
                    if(name == null || name.isBlank()){
                        return criteriaBuilder.conjunction();
                }
                    String searchText = "%" + name.trim().toLowerCase() + "%";

                    return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchText);
                }

            };

        }
        public static Specification<Pet> speciesContains(final String species){
        return new Specification<Pet>(){
            @Override
            public Predicate toPredicate(Root<Pet> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder){
                if(species == null || species.isBlank()){
                    return criteriaBuilder.conjunction();
                }
                String searchText = "%" + species.trim().toLowerCase() + "%";
                return criteriaBuilder.like(criteriaBuilder.lower(root.get("species")), searchText);
            }
        };
        }
}
