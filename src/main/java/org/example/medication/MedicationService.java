package org.example.medication;
import org.example.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.example.medication.dto.CreateMedicationRequest;

import java.util.List;

@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;

    public MedicationService(MedicationRepository medicationRepository){
        this.medicationRepository = medicationRepository;
    }

    public List<Medication> findAll()
    {
        List<Medication> medications = medicationRepository.findAll();
        return medications;
    }

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

    public void deleteById(Long id){
    medicationRepository.deleteById(id);
    }

    public Medication create(CreateMedicationRequest request){
        Medication medication = new Medication(request.getName());
        return save(medication);
    }

    public Medication update(Long id, CreateMedicationRequest request){
        Medication medication = findById(id);
        medication.setName(request.getName());
        return save(medication);
    }
}

