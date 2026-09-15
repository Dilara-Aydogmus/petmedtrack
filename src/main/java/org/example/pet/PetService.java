package org.example.pet;
import org.example.pet.dto.CreatePetRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PetService {

    private final PetRepository petRepository;

    public PetService(PetRepository petRepository){
        this.petRepository = petRepository;
    }

    public List<Pet> findAll() {
        return petRepository.findAll();
    }

    public Pet findById(Long id) {
        return petRepository.findById(id).orElseThrow(()-> new RuntimeException("Pet not found: " + id));
    }

    public Pet create(CreatePetRequest request){
        Pet pet = new Pet(request.getName(), request.getSpecies(), request.getBreed(), request.getBirthDate());
        return save(pet);
    }
    public Pet save(Pet pet) {
        return petRepository.save(pet);
    }

    public Pet update(Long id, CreatePetRequest request) {
        Pet pet = findById(id);

        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setBirthDate(request.getBirthDate());

        return save(pet);
    }

    public void deleteById(Long id) {
        petRepository.deleteById(id);
    }
}

