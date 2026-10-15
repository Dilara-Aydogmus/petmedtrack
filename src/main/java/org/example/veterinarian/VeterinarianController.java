package org.example.veterinarian;
import jakarta.validation.Valid;
import org.example.veterinarian.dto.CreateVeterinarianRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veterinarians")
public class VeterinarianController {
    private final VeterinarianService veterinarianService;

    public VeterinarianController(VeterinarianService veterinarianService){
        this.veterinarianService = veterinarianService;
    }

    @GetMapping
    public List<Veterinarian> getAllVeterinarians(){
        return veterinarianService.findAll();
    }

    @GetMapping("/{id}")
    public Veterinarian getVeterinarianById(@PathVariable Long id){
        return veterinarianService.findById(id);
    }

    @GetMapping("/clinic/{clinicId}")
    public List<Veterinarian> getVeterinariansByClinic(@PathVariable Long clinicId){
        return veterinarianService.findByClinicId(clinicId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Veterinarian createVeterinarian(@Valid @RequestBody CreateVeterinarianRequest request){
        return veterinarianService.create(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getClinicId()
        );
    }

    @PutMapping("/{id}")
    public Veterinarian updateVeterinarian(@PathVariable Long id, @Valid @RequestBody CreateVeterinarianRequest request){
        return veterinarianService.update(id, request.getFirstName(),
                request.getLastName(), request.getEmail(),
                request.getPhoneNumber(), request.getClinicId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVeterinarian(@PathVariable Long id){
        veterinarianService.deleteById(id);
    }

}
