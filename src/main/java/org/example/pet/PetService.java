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

@Service
public class PetService {

    private final PetRepository petRepository;
    private final OwnerService ownerService;

    public PetService(PetRepository petRepository, OwnerService ownerService){
        this.petRepository = petRepository;
        this.ownerService = ownerService;
    }

    @Cacheable(value = "pets")
    public List<Pet> findAll() {
        List<Pet> pets = petRepository.findAll();
        return pets;
    }

    @Cacheable(value = "pets", key = "#id")
    public Pet findById(Long id) {
        Pet pet = petRepository.findPet(id);
        if(pet == null) {
            String errorMessage = "Pet not found:" + id;
            throw new ResourceNotFoundException(errorMessage);
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
        findById(id);
        petRepository.deleteById(id);
    }

    public List<Pet> search(String name, String species){
        Specification<Pet> specification = new PetNameSpecification(name).and(new PetSpeciesSpecification(species));
        return petRepository.findAll(specification);
    }

    @Cacheable(value = "petsByOwner", key = "#ownerId")
    public List<Pet> findByOwnerId(Long ownerId){
        ownerService.findById(ownerId);
        return petRepository.findByOwnerId(ownerId);
    }
}

