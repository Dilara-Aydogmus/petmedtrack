package org.example.pet;
import org.example.common.ResourceNotFoundException;
import org.example.owner.Owner;
import org.example.owner.OwnerService;
import org.example.pet.dto.CreatePetRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final OwnerService ownerService;

    public PetService(PetRepository petRepository, OwnerService ownerService){
        this.petRepository = petRepository;
        this.ownerService = ownerService;
    }

    public List<Pet> findAll() {
        List<Pet> pets = petRepository.findAll();
        return pets;
    }

    public Pet findById(Long id) {
        Pet pet = petRepository.findPet(id);
        if(pet == null) {
            String errorMessage = "Pet not found:" + id;
            throw new ResourceNotFoundException(errorMessage);
        }
        return pet;
    }

    public Pet create(CreatePetRequest request){
        Owner owner = ownerService.findById(request.getOwnerId());
        Pet pet = new Pet(request.getName(), request.getSpecies(), request.getBreed(), request.getBirthDate(), owner);
        Pet savedPet = save(pet);
        return savedPet;
    }
    public Pet save(Pet pet) {
        return petRepository.save(pet);
    }

    public Pet update(Long id, CreatePetRequest request) {
        Pet pet = findById(id);
        Owner owner = ownerService.findById(request.getOwnerId());
        pet.setOwner(owner);
        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setBirthDate(request.getBirthDate());

        Pet updatedPet = save(pet);
        return updatedPet;
    }

    public void deleteById(Long id) {
        petRepository.deleteById(id);
    }
}

