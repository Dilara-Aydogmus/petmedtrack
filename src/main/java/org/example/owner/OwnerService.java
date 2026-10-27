package org.example.owner;
import org.example.auth.User;
import org.example.common.ResourceNotFoundException;
import org.example.owner.dto.CreateOwnerRequest;
import org.example.treatment.Treatment;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import org.example.pet.Pet;
import org.example.pet.PetRepository;
import org.example.treatment.TreatmentRepository;
import org.springframework.transaction.annotation.Transactional;
import org.example.notification.EmailMessage;
import org.example.notification.EmailProducer;
import org.springframework.cache.annotation.CacheEvict;
import org.example.owner.specification.OwnerActiveSpecification;
import org.example.owner.specification.OwnerNameSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.example.clinic.Clinic;
import org.example.clinic.ClinicService;
import org.example.auth.CurrentUserService;
import org.example.owner.specification.OwnerClinicSpecification;

@Service
public class OwnerService {
    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;
    private final TreatmentRepository treatmentRepository;
    private final EmailProducer emailProducer;
    private final ClinicService clinicService;
    private final CurrentUserService currentUserService;

    public OwnerService(OwnerRepository ownerRepository, PetRepository petRepository,
                        TreatmentRepository treatmentRepository, EmailProducer emailProducer,
                        ClinicService clinicService, CurrentUserService currentUserService) {
        this.ownerRepository = ownerRepository;
        this.petRepository = petRepository;
        this.treatmentRepository = treatmentRepository;
        this.emailProducer = emailProducer;
        this.clinicService = clinicService;
        this.currentUserService = currentUserService;
    }
    public List<Owner> findAll() {
        User currentUser = currentUserService.getCurrentUser();
        if("USER".equalsIgnoreCase(currentUser.getRole())){
            Owner owner = currentUserService.getCurrentOwner();
            List<Owner> owners = new ArrayList<>();
            owners.add(owner);
            return owners;
        }

        Long clinicId = currentUserService.getCurrentClinicId();
        return ownerRepository.findByClinicIdAndActiveTrue(clinicId);
    }

    public Owner findById(Long id){
        User currentUser = currentUserService.getCurrentUser();
        if("USER".equalsIgnoreCase(currentUser.getRole())){
            Owner currentOwner = currentUserService.getCurrentOwner();
            if(!currentOwner.getId().equals(id)){
                throw new ResourceNotFoundException("Owner not found in current scope: " + id);
            }
            return currentOwner;
        }

        Long clinicId = currentUserService.getCurrentClinicId();
        Owner owner = ownerRepository.findOwnerByIdAndClinicId(id, clinicId);

        if(owner == null){
            throw new ResourceNotFoundException("Owner not found in current clinic: " + id);
        }

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
        Long currentClinicId = currentUserService.getCurrentClinicId();
        if(!currentClinicId.equals(request.getClinicId())){
            throw new IllegalArgumentException("You connot create an owner for another clinic");
        }
        Clinic clinic = clinicService.findById(currentClinicId);
        Owner owner = new Owner(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhoneNumber(),
                clinic
        );

        Owner savedOwner = save(owner);

        EmailMessage emailMessage = new EmailMessage(savedOwner.getEmail(),"PetMedTrack'e hoş geldiniz",
                "Merhaba" + savedOwner.getFirstName() + ",\n\n"
                + "PetMedTrack hesabınız başarıyla oluşturuldu. \n\n"
                + "Detayları görmek için sisteme giriş yapın.\n"
                + "PetMedTrack Ekibi");
        emailProducer.sendEmail(emailMessage);
        return savedOwner;


    }

    public Owner createForUserRegistration(CreateOwnerRequest request, Clinic clinic){
        if(ownerRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException("Email already exists");
        }
        if(ownerRepository.existsByPhoneNumber(request.getPhoneNumber())){
            throw new IllegalArgumentException("Phone number already exists");
        }
        Owner owner = new Owner(request.getFirstName(),
                                request.getLastName(),
                                request.getEmail(),
                                request.getPhoneNumber(),
                                clinic);
        Owner savedOwner = ownerRepository.save(owner);
        EmailMessage emailMessage = new EmailMessage(
                savedOwner.getEmail(), "PetMedTrack'e hoşgeldiniz",
                "Merhaba" + savedOwner.getFirstName() + ",\n\nPetMedTrack hesabınız başarıyla oluşturuldu."
        );

        emailProducer.sendEmail(emailMessage);
        return savedOwner;
    }



    @CacheEvict(value = "owners", allEntries = true)
    public Owner update(Long id, CreateOwnerRequest request){
        Owner owner = findById(id);
        Clinic clinic = clinicService.findById(request.getClinicId());
        owner.setClinic(clinic);

        if(ownerRepository.existsByEmailAndIdNot(request.getEmail(), id)){
            throw new IllegalArgumentException("Email already exists");
        }

        if(ownerRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), id)){
            throw new IllegalArgumentException("Phone number already exists");}

        Long currentClinicId = currentUserService.getCurrentClinicId();
        if(!currentClinicId.equals(request.getClinicId())){
            throw new IllegalArgumentException("You cannot move an owner to another clinic");
        }
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
      Owner owner = ownerRepository.findOwnerById(id);
      if(owner == null){
          throw new ResourceNotFoundException("Owner not found: " + id);
      }
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
        Owner owner = ownerRepository.findOwnerById(id);
        if(owner==null){
            throw new ResourceNotFoundException("Owner not found: " + id);
        }
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

    public List<Owner> search(String name, Boolean active) {
        User currentUser = currentUserService.getCurrentUser();
        if("USER".equalsIgnoreCase(currentUser.getRole())){
            Owner currentOwner = currentUserService.getCurrentOwner();
            boolean nameMatches = true;
            boolean activeMatches = true;

            if(name != null && !name.isBlank()){
                String searchText = name.toLowerCase();
                String firstName = currentOwner.getFirstName().toLowerCase();
                String lastName = currentOwner.getLastName().toLowerCase();

                nameMatches = firstName.contains(searchText) || lastName.contains(searchText);
            }

            if(active != null){
                activeMatches = currentOwner.isActive() == active;
            }

            List<Owner> owners = new ArrayList<>();
            if(nameMatches && activeMatches){
                owners.add(currentOwner);
            }
            return owners;
        }

        Long clinicId = currentUserService.getCurrentClinicId();

        Specification<Owner> specification =
                new OwnerNameSpecification(name)
                        .and(new OwnerActiveSpecification(active))
                        .and(OwnerClinicSpecification.belongsToClinic(clinicId));

        return ownerRepository.findAll(specification);
    }

    public Owner findByName(String firstName, String lastName){
        Long clinicId = currentUserService.getCurrentClinicId();
        Owner owner = ownerRepository.findByFirstNameAndLastNameAndClinicId(firstName, lastName, clinicId);
        if(owner == null){
            throw new ResourceNotFoundException("Owner not found: " + firstName + " " + lastName);
        }
        return owner;
    }
}
