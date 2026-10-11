package org.example.disease;

import org.example.common.ResourceNotFoundException;
import org.example.disease.dto.CreateDiseaseRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiseaseService
{
    private final DiseaseRepository diseaseRepository;
    public DiseaseService(DiseaseRepository diseaseRepository){
        this.diseaseRepository = diseaseRepository;
    }

    public List<Disease> findAll(){
        return diseaseRepository.findAll();
    }
    public Disease findById(Long id){
        return diseaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Disease not found:" + id
        )
        );    }

    public Disease create(CreateDiseaseRequest request){
        if(diseaseRepository.existsByName(request.getName())){
            throw new IllegalArgumentException("Disease name already exists");
        }
        Disease disease = new Disease(request.getName());
        return diseaseRepository.save(disease);
    }
}
