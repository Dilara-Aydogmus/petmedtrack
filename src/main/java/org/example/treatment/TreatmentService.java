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

import java.time.LocalDate;
import org.example.notification.EmailMessage;
import org.example.notification.EmailProducer;

@Service
public class TreatmentService {
    private final TreatmentRepository treatmentRepository;
    private final PetService petService;
    private final MedicationService medicationService;
    private final EmailProducer emailProducer;

    public TreatmentService(TreatmentRepository treatmentRepository, PetService petService, MedicationService medicationService, EmailProducer emailProducer) {
        this.treatmentRepository = treatmentRepository;
        this.petService = petService;
        this.medicationService = medicationService;
        this.emailProducer = emailProducer;
    }

    public List<Treatment> findAll() {
        return treatmentRepository.findAll();
    }

    public Treatment findById(Long id) {
        Treatment treatment = treatmentRepository.findTreatment(id);
        if (treatment == null) {
            throw new ResourceNotFoundException("Treatment not found:" + id);
        }
        return treatment;
    }

    public Treatment create(CreateTreatmentRequest request) {
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        Pet pet = petService.findById(request.getPetId());
        Medication medication = medicationService.findById(request.getMedicationId());

        Treatment treatment = new Treatment(pet, medication, request.getDosage(), request.getFrequency(), request.getStartDate(), request.getEndDate());
        Treatment savedTreatment = treatmentRepository.save(treatment);
        EmailMessage emailMessage = new EmailMessage(savedTreatment.getPet().getOwner().getEmail(),
                "PetMedTrack tedavi bildirimi", "Merhaba"
                + savedTreatment.getPet().getOwner().getFirstName() + ",\n\n"
                + savedTreatment.getPet().getName()
                + " adlı evcil hayvanınız için yeni bir tedavi kaydı oluşturuldu. \n\n"
                + "Detayları görmek için PetMedTrack hesabınıza giriş yapın:\n"
                + "PetMedTrack Ekibi");

        emailProducer.sendEmail(emailMessage);
        return savedTreatment;
    }

    public Treatment update(Long id, UpdateTreatmentRequest request){
        Treatment treatment= findById(id);
        LocalDate startDate = request.getStartDate() != null
                ? request.getStartDate()
                : treatment.getStartDate();
        LocalDate endDate = request.getEndDate() != null
                ? request.getEndDate()
                : treatment.getEndDate();
        if(endDate != null && endDate.isBefore(startDate)){
            throw new IllegalArgumentException("End date cannot be before start date");
        }

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
        Treatment savedTreatment = treatmentRepository.save(treatment);
        EmailMessage emailMessage = new EmailMessage(
                savedTreatment.getPet().getOwner().getEmail(),
                "PetMedTrack tedavi güncellemesi",
                        "Merhaba "
                        + savedTreatment.getPet().getOwner().getFirstName()
                        + ",\n\n"
                        + savedTreatment.getPet().getName()
                        + " adlı evcil hayvanınızın tedavi bilgileri "
                        + "veterineriniz tarafından güncellendi.\n\n"
                        + "Detayları görmek için PetMedTrack hesabınıza giriş yapın.\n"
                        + "PetMedTrack Ekibi"
        );
        emailProducer.sendEmail(emailMessage);
        return savedTreatment;
    }


    public void deleteById(Long id){
        findById(id);
        treatmentRepository.deleteById(id);
    }

    public List<Treatment> findByPetId(Long petId){
        petService.findById(petId);
        return treatmentRepository.findByPetId(petId);
    }

}

