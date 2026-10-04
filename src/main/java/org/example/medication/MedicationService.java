package org.example.medication;
import org.example.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.example.medication.dto.CreateMedicationRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;

    public MedicationService(MedicationRepository medicationRepository){
        this.medicationRepository = medicationRepository;
    }

    @Cacheable(value = "medications")
    public List<Medication> findAll()
    {
        List<Medication> medications = medicationRepository.findAll();
        return medications;
    }

    @Cacheable(value = "medications", key = "#id")
    public Medication findById(Long id){
    Medication medication = medicationRepository.findMedication(id);
        if(medication == null){
            String errorMessage = "Medication not found" + id;
            throw new ResourceNotFoundException(errorMessage);
        }
        return medication;
    }

    public Medication save(Medication medication){
        Medication savedMedication = medicationRepository.save(medication);
        return savedMedication;
    }

    @CacheEvict(value = "medications", allEntries = true)
    public void deleteById(Long id){
        findById(id);
    medicationRepository.deleteById(id);
    }

    @CacheEvict(value = "medications", allEntries = true)
    public Medication create(CreateMedicationRequest request){
        if (medicationRepository.existsByName(request.getName())){
            throw new IllegalArgumentException("Medication name already exists");
        }
        Medication medication = new Medication(request.getName());
        return save(medication);
    }

    @CacheEvict(value = "medications", allEntries = true)
    public Medication update(Long id, CreateMedicationRequest request){
        Medication medication = findById(id);
        if (medicationRepository.existsByNameAndIdNot(request.getName(), id)){
            throw new IllegalArgumentException("Medication name already exists");
        }
        medication.setName(request.getName());
        return save(medication);
    }
}

