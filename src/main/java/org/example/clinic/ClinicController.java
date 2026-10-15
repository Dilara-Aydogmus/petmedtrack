package org.example.clinic;

import jakarta.validation.Valid;
import org.example.clinic.dto.CreateClinicRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinics")
public class ClinicController {
    private final ClinicService clinicService;

    public ClinicController(ClinicService clinicService){
        this.clinicService = clinicService;
    }

    @GetMapping
    public List<Clinic> getAllClinics(){
        return clinicService.findAll();
    }

    @GetMapping("/{id}")
    public Clinic getClinicById(@PathVariable Long id){
        return clinicService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Clinic createClinic(@Valid @RequestBody CreateClinicRequest request){
        return clinicService.create(request.getName(), request.getAddress(), request.getPhoneNumber());
    }

    @PutMapping("/{id}")
    public Clinic updateClinic(@PathVariable Long id, @Valid @RequestBody CreateClinicRequest request){
        return clinicService.update(id, request.getName(), request.getAddress(), request.getPhoneNumber());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClinic(@PathVariable Long id ){
        clinicService.deleteById(id);
    }
}
