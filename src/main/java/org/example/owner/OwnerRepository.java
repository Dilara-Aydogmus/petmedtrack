package org.example.owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface OwnerRepository extends JpaRepository<Owner, Long>, JpaSpecificationExecutor<Owner> {
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);

    List<Owner> findByActiveTrue();
    List<Owner> findByClinicIdAndActiveTrue(Long clinicId);
    Owner findOwnerByIdAndClinicId(Long id, Long clinicId);
    Owner findOwnerById(Long id);
}
