package org.example.treatment;

import org.example.common.ResourceNotFoundException;
import org.example.medication.Medication;
import org.example.medication.MedicationService;
import org.example.pet.Pet;
import org.example.pet.PetService;
import org.example.treatment.dto.CreateTreatmentRequest;
import org.springframework.stereotype.Service;
import java.util.List;

import org.example.treatment.dto.UpdateTreatmentRequest;

@Service
public class TreatmentService {
    private final TreatmentRepository treatmentRepository;
    private final PetService petService;
    private final MedicationService medicationService;

    public TreatmentService(TreatmentRepository treatmentRepository, PetService petService, MedicationService medicationService){
        this.treatmentRepository = treatmentRepository;
        this.petService = petService;
        this.medicationService = medicationService;
    }
    public List<Treatment> findAll(){
    return treatmentRepository.findAll();
    }
    public Treatment findById(Long id){
        Treatment treatment = treatmentRepository.findTreatment(id);
        if (treatment == null) {
            throw new ResourceNotFoundException("Treatment not found:"+ id);
        }
        return treatment;
    }
    public Treatment create(CreateTreatmentRequest request){
        Pet pet = petService.findById(request.getPetId());
        Medication medication = medicationService.findById(request.getMedicationId());

        Treatment treatment = new Treatment(pet, medication, request.getDosage(), request.getFrequency(),request.getStartDate(),request.getEndDate());
        return treatmentRepository.save(treatment);

    }

    public Treatment update(Long id, UpdateTreatmentRequest request){
        Treatment treatment= findById(id);

        if(request.getPetId() != null){
            Pet pet = petService.findById(request.getPetId());
            treatment.setPet(pet);
        }

        if(request.getMedicationId() != null){
            Medication medication = medicationService.findById(request.getMedicationId());
            treatment.setMedication(medication);
        }

        if(request.getDosage() != null){
            treatment.setDosage(request.getDosage());
        }

        if(request.getFrequency() != null){
            treatment.setFrequency(request.getFrequency());
        }

        if(request.getStartDate() != null){
            treatment.setStartDate(request.getStartDate());
        }
        if(request.getEndDate() != null){
            treatment.setEndDate(request.getEndDate());
        }
        return  treatmentRepository.save(treatment);
    }
    public void deleteById(Long id){
        findById(id);
        treatmentRepository.deleteById(id);
    }

}

