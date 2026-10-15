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
        Disease disease = diseaseRepository.findDisease(id);
        if(disease == null){
            throw new ResourceNotFoundException("Disease not found: " + id);
        }
        return disease;
    }

    public Disease create(CreateDiseaseRequest request){
        if(diseaseRepository.existsByName(request.getName())){
            throw new IllegalArgumentException("Disease name already exists");
        }
        Disease disease = new Disease(request.getName());
        return diseaseRepository.save(disease);
    }

    public Disease update(Long id, CreateDiseaseRequest request){
        Disease disease = findById(id);
        if(diseaseRepository.existsByNameAndIdNot(request.getName(),id)){
            throw new IllegalArgumentException("Disease name already exists");
        }
        disease.setName(request.getName());
        return diseaseRepository.save(disease);
    }

    public void deleteById(Long id){
        Disease disease = findById(id);
        diseaseRepository.delete(disease);
    }
}
