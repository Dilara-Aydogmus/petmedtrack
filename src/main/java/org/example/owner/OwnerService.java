package org.example.owner;
import org.example.common.ResourceNotFoundException;
import org.example.owner.dto.CreateOwnerRequest;
import org.example.treatment.Treatment;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.example.pet.Pet;
import org.example.pet.PetRepository;
import org.example.treatment.TreatmentRepository;
import org.springframework.transaction.annotation.Transactional;


@Service
public class OwnerService {
    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;
    private final TreatmentRepository treatmentRepository;

    public OwnerService(OwnerRepository ownerRepository, PetRepository petRepository, TreatmentRepository treatmentRepository) {
        this.ownerRepository = ownerRepository;
        this.petRepository = petRepository;
        this.treatmentRepository = treatmentRepository;
    }
    public List<Owner> findAll() {
        return ownerRepository.findByActiveTrue();
    }

    public Owner findById(Long id){
        Optional<Owner> ownerOptional = ownerRepository.findById(id);

        if(ownerOptional.isEmpty()) {
            String errorMessage = "Owner not found:" + id;
            throw new ResourceNotFoundException(errorMessage);
        }

        Owner owner = ownerOptional.get();
        return owner;
    }

    public Owner create(CreateOwnerRequest request){
        if (ownerRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException("Email already exists");
        }
        if (ownerRepository.existsByPhoneNumber(request.getPhoneNumber())){
            throw new IllegalArgumentException("Phone number already exists");
        }
        Owner owner = new Owner(request.getFirstName(), request.getLastName(), request.getEmail(), request.getPhoneNumber());
        return save(owner);
    }

    public Owner update(Long id, CreateOwnerRequest request){
        Owner owner = findById(id);

        if(ownerRepository.existsByEmailAndIdNot(request.getEmail(), id)){
            throw new IllegalArgumentException("Email already exists");
        }

        if(ownerRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), id)){
            throw new IllegalArgumentException("Phone number already exists");}

        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setEmail(request.getEmail());
        owner.setPhoneNumber(request.getPhoneNumber());
        return save(owner);
    }

    public Owner save(Owner owner){
        Owner savedOwner = ownerRepository.save(owner);
        return savedOwner;

    }

    public void deactivateById(Long id) {
    Owner owner = findById(id);
    owner.setActive(false);
    ownerRepository.save(owner);
    }
    public void activateById(Long id) {
        Optional<Owner> ownerOptional = ownerRepository.findById(id);
        if(ownerOptional.isEmpty()){
            throw new ResourceNotFoundException("Owner not found: " + id);
        }
        Owner owner = ownerOptional.get();
        owner.setActive(true);
        ownerRepository.save(owner);
    }

    @Transactional
    public void permanentlyDeleteById(Long id){
        Optional<Owner> ownerOptional = ownerRepository.findById(id);
        if (ownerOptional.isEmpty()){
            throw new ResourceNotFoundException("Owner not found" + id);
        }
        Owner owner = ownerOptional.get();
        if(owner.isActive()){
            throw new IllegalArgumentException("Owner must be inactive before permanent deletion");
        }

        List<Pet> pets = petRepository.findByOwnerId(id);
        for (Pet pet : pets){
            treatmentRepository.deleteById(pet.getId());
        }
        petRepository.deleteAll(pets);
        ownerRepository.delete(owner);
    }
}
