package org.example.owner;
import org.example.common.ResourceNotFoundException;
import org.example.owner.dto.CreateOwnerRequest;
import org.example.treatment.Treatment;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.example.pet.Pet;
import org.example.pet.PetRepository;
import org.example.treatment.TreatmentRepository;
import org.springframework.transaction.annotation.Transactional;
import org.example.notification.EmailMessage;
import org.example.notification.EmailProducer;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

@Service
public class OwnerService {
    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;
    private final TreatmentRepository treatmentRepository;
    private final EmailProducer emailProducer;

    public OwnerService(OwnerRepository ownerRepository, PetRepository petRepository, TreatmentRepository treatmentRepository, EmailProducer emailProducer) {
        this.ownerRepository = ownerRepository;
        this.petRepository = petRepository;
        this.treatmentRepository = treatmentRepository;
        this.emailProducer = emailProducer;
    }
    @Cacheable(value = "owners")
    public List<Owner> findAll() {
        return ownerRepository.findByActiveTrue();
    }

    @Cacheable(value = "owners", key = "#id")
    public Owner findById(Long id){
        Optional<Owner> ownerOptional = ownerRepository.findById(id);

        if(ownerOptional.isEmpty()) {
            String errorMessage = "Owner not found:" + id;
            throw new ResourceNotFoundException(errorMessage);
        }

        Owner owner = ownerOptional.get();
        return owner;
    }

    @CacheEvict(value = "owners", allEntries = true)
    public Owner create(CreateOwnerRequest request){
        if (ownerRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException("Email already exists");
        }
        if (ownerRepository.existsByPhoneNumber(request.getPhoneNumber())){
            throw new IllegalArgumentException("Phone number already exists");
        }
        Owner owner = new Owner(request.getFirstName(), request.getLastName(), request.getEmail(), request.getPhoneNumber());
        Owner savedOwner = save(owner);

        EmailMessage emailMessage = new EmailMessage(savedOwner.getEmail(),"PetMedTrack'e hoş geldiniz",
                "Merhaba" + savedOwner.getFirstName() + ",\n\n"
                + "PetMedTrack hesabınız başarıyla oluşturuldu. \n\n"
                + "Detayları görmek için sisteme giriş yapın.\n"
                + "PetMedTrack Ekibi");
        emailProducer.sendEmail(emailMessage);
        return savedOwner;


    }

    @CacheEvict(value = "owners", allEntries = true)
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

    @CacheEvict(value = "owners", allEntries = true)
    public void deactivateById(Long id) {
    Owner owner = findById(id);
    owner.setActive(false);
    ownerRepository.save(owner);
    }

    @CacheEvict(value = "owners", allEntries = true)
    public void activateById(Long id) {
        Optional<Owner> ownerOptional = ownerRepository.findById(id);
        if(ownerOptional.isEmpty()){
            throw new ResourceNotFoundException("Owner not found: " + id);
        }
        Owner owner = ownerOptional.get();
        owner.setActive(true);
        ownerRepository.save(owner);
    }

    @Caching(evict = {
            @CacheEvict(value = "owners", allEntries = true),
            @CacheEvict(value = "pets", allEntries = true),
            @CacheEvict(value = "petsByOwner", allEntries = true),
            @CacheEvict(value = "treatments", allEntries = true),
            @CacheEvict(value = "treatmentsByPet", allEntries = true)
    })
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
