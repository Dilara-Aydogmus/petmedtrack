package org.example.medication;
import jakarta.validation.Valid;
import org.example.medication.dto.CreateMedicationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {
    private final MedicationService medicationService;
    public MedicationController(MedicationService medicationService){
        this.medicationService = medicationService;
    }

    @GetMapping
    public List<Medication> getAllMedications(){
        return medicationService.findAll();
    }

    @GetMapping("/{id}")
    public Medication getMedicationById(@PathVariable Long id){
        return medicationService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
        public Medication createMedication(@Valid @RequestBody CreateMedicationRequest request){
            return medicationService.create(request);
        }


    @PutMapping("/{id}")
    public Medication updateMedication(@PathVariable Long id, @Valid @RequestBody CreateMedicationRequest request){
        return medicationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedication(@PathVariable Long id){
        medicationService.deleteById(id);
    }
}
