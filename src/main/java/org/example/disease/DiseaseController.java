package org.example.disease;

import jakarta.validation.Valid;
import org.example.disease.dto.CreateDiseaseRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diseases")
public class DiseaseController {

    private final DiseaseService diseaseService;

    public DiseaseController(DiseaseService diseaseService){
        this.diseaseService = diseaseService;
    }

    @GetMapping
    public List<Disease> getAllDiseases(){
        return diseaseService.findAll();
    }

    @GetMapping("/{id}")
    public Disease getDiseaseById(@PathVariable Long id){
        return diseaseService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Disease createDisease(@Valid @RequestBody CreateDiseaseRequest request){
        return diseaseService.create(request);
    }

    @PutMapping("/{id}")
    public Disease updateDisease(@PathVariable Long id, @Valid @RequestBody CreateDiseaseRequest request){
        return diseaseService.update(id,request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDisease(@PathVariable Long id){
        diseaseService.deleteById(id);
    }

}
