package org.example.pet;
import org.example.auth.User;
import org.example.common.ResourceNotFoundException;
import org.example.owner.Owner;
import org.example.owner.OwnerService;
import org.example.pet.dto.CreatePetRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.example.pet.specification.PetNameSpecification;
import org.example.pet.specification.PetSpeciesSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.example.auth.CurrentUserService;
import org.example.pet.specification.PetClinicSpecification;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final OwnerService ownerService;
    private final CurrentUserService currentUserService;

    public PetService(PetRepository petRepository, OwnerService ownerService, CurrentUserService currentUserService){
        this.currentUserService = currentUserService;
        this.petRepository = petRepository;
        this.ownerService = ownerService;
    }

    @Cacheable(value = "pets")
    public List<Pet> findAll() {
        User currentUser = currentUserService.getCurrentUser();
        if("USER".equalsIgnoreCase(currentUser.getRole())){
            Long ownerId = currentUserService.getCurrentOwner().getId();
            return petRepository.findByOwnerId(ownerId);
        }
        Long clinicId = currentUserService.getCurrentClinicId();
        return petRepository.findByOwnerClinicId(clinicId);
    }

    @Cacheable(value = "pets", key = "#id")
    public Pet findById(Long id) {
        User currentUser = currentUserService.getCurrentUser();
        Pet pet;

        if("USER".equalsIgnoreCase(currentUser.getRole())){
            Long ownerId = currentUserService.getCurrentOwner().getId();
            pet = petRepository.findPetByIdAndOwnerId(id, ownerId);
        } else{
            Long clinicId = currentUserService.getCurrentClinicId();
            pet = petRepository.findPetByIdAndOwnerClinicId(id, clinicId);
        }

        if(pet == null){
            throw new ResourceNotFoundException("Pet not found in current access scope:"  + id);
        }
    return pet;
    }

    @Caching(evict = {
            @CacheEvict(value = "pets", allEntries = true),
            @CacheEvict(value = "petsByOwner", allEntries = true)
    })
    public Pet create(CreatePetRequest request){
        Owner owner = ownerService.findByName(request.getOwnerFirstName(), request.getOwnerLastName());
        Pet pet = new Pet(
                request.getName(),
                request.getSpecies(),
                request.getBreed(),
                request.getAgeYears(),
                request.getAgeMonths(),
                request.getWeight(),
                request.getHeight(),
                request.isNeutered(),
                owner
        );
        return save(pet);
    }


    public Pet save(Pet pet) {
        return petRepository.save(pet);
    }

    @Caching(evict = {
            @CacheEvict(value = "pets", allEntries = true),
            @CacheEvict(value = "petsByOwner", allEntries = true)
    })


    public Pet update(Long id, CreatePetRequest request) {
        Pet pet = findById(id);
        Owner owner = ownerService.findByName(request.getOwnerFirstName(), request.getOwnerLastName());
        pet.setOwner(owner);
        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setAgeYears(request.getAgeYears());
        pet.setAgeMonths(request.getAgeMonths());
        pet.setWeight(request.getWeight());
        pet.setHeight(request.getHeight());
        pet.setNeutered(request.isNeutered());

        return save(pet);
    }


    @Caching(evict = {
            @CacheEvict(value = "pets", allEntries = true),
            @CacheEvict(value = "petsByOwner", allEntries = true)
    })
    public void deleteById(Long id) {
        Pet pet = findById(id);
        petRepository.delete(pet);
    }

    public List<Pet> search(String name, String species){
        Long clinicId = currentUserService.getCurrentClinicId();

        Specification<Pet> nameSpecification = new PetNameSpecification(name);

        Specification<Pet> speciesSpecification = new PetSpeciesSpecification(species);

        Specification<Pet> clinicSpecification = new PetClinicSpecification(clinicId);

        Specification<Pet> combinedSpecification = nameSpecification.and(speciesSpecification).and(clinicSpecification);

        return petRepository.findAll(combinedSpecification);
    }

    @Cacheable(value = "petsByOwner", key = "#ownerId")
    public List<Pet> findByOwnerId(Long ownerId){
        Owner owner = ownerService.findById(ownerId);
        return petRepository.findByOwnerId(owner.getId());
    }
}

















































































































































































































































































































































































































































































































































































































































































































































































































































































