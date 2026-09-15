package org.example.pet;
import jakarta.validation.Valid;
import org.example.pet.dto.CreatePetRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    public List<Pet> getAllPets() {
        return petService.findAll();
    }

    @GetMapping("/{id}")
    public Pet getPetById(@PathVariable Long id) {
        return petService.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePet(@PathVariable Long id){
        petService.deleteById(id);
    }

    @PutMapping("/{id}")
    public Pet updatePet(@PathVariable Long id, @Valid @RequestBody CreatePetRequest request){
        return petService.update(id, request);
    }

}
