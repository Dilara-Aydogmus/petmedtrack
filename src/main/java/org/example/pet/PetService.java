package org.example.pet;
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
        Long clinicId = currentUserService.getCurrentClinicId();
        return petRepository.findByOwnerClinicId(clinicId);
    }

    @Cacheable(value = "pets", key = "#id")
    public Pet findById(Long id) {
    Long clinicId = currentUserService.getCurrentClinicId();
    Pet pet = petRepository.findPetByIdAndOwnerClinicId(id, clinicId);
    if(pet == null){
        throw new ResourceNotFoundException("Pet not found in current clinic: " + id);
    }

    return pet;
    }

    @Caching(evict = {
            @CacheEvict(value = "pets", allEntries = true),
            @CacheEvict(value = "petsByOwner", allEntries = true)
    })
    public Pet create(CreatePetRequest request){
        Owner owner = ownerService.findById(request.getOwnerId());
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
        Pet savedPet = save(pet);
        return savedPet;
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
        Owner owner = ownerService.findById(request.getOwnerId());
        pet.setOwner(owner);
        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setAgeYears(request.getAgeYears());
        pet.setAgeMonths(request.getAgeMonths());
        pet.setWeight(request.getWeight());
        pet.setHeight(request.getHeight());
        pet.setNeutered(request.isNeutered());

        Pet updatedPet = save(pet);
        return updatedPet;
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

















































































































































































































































































































































































































































































































































































































































































































































































































































































