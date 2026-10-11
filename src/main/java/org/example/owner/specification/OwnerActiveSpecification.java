package org.example.owner.specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.example.owner.Owner;
import org.springframework.data.jpa.domain.Specification;

public class OwnerActiveSpecification implements Specification<Owner> {
    private final Boolean active;
    public OwnerActiveSpecification(Boolean active){
        this.active = active;
    }

    @Override
    public Predicate toPredicate(
            Root<Owner> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder
    ){
        if(active == null){
            return criteriaBuilder.conjunction();
        }
        return criteriaBuilder.equal(root.get("active"), active);
    }
}
