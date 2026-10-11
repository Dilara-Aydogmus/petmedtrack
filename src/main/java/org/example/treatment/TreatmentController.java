package org.example.treatment;
import jakarta.validation.Valid;
import org.example.treatment.dto.CreateTreatmentRequest;
import org.example.treatment.dto.UpdateTreatmentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/treatments")
public class TreatmentController {
    private final TreatmentService treatmentService;

    public TreatmentController(TreatmentService treatmentService) {
        this.treatmentService = treatmentService;
    }

    @GetMapping
    public List<Treatment> getAllTreatments() {
        return treatmentService.findAll();
    }

    @GetMapping("/{id}")
    public Treatment getTreatmentById(@PathVariable Long id) {
        return treatmentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Treatment createTreatment(@Valid @RequestBody CreateTreatmentRequest request) {
        return treatmentService.create(request);
    }

    @PatchMapping("/{id}")
    public Treatment updateTreatment(@PathVariable Long id,@Valid @RequestBody UpdateTreatmentRequest request){
        return treatmentService.update(id, request);
    }

    @PatchMapping("/{id}/complete")
    public Treatment completeTreatment(@PathVariable Long id){
        return treatmentService.complete(id);
    }

    @PatchMapping("/{id}/cancel")
    public Treatment cancelTreatment(@PathVariable Long id){
        return treatmentService.cancel(id);
    }

    @PatchMapping("/{id}/reactivate")
    public Treatment reactivateTreatment(@PathVariable Long id){
        return treatmentService.reactivate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTreatment(@PathVariable Long id){
        treatmentService.deleteById(id);
    }

    @GetMapping("/pet/{petId}")
    public List<Treatment> getTreatmentsByPet(@PathVariable Long petId){
        return treatmentService.findByPetId(petId);
    }

}


