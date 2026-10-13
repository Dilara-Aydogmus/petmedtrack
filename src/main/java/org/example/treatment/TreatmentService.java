package org.example.treatment;

import org.example.common.ResourceNotFoundException;
import org.example.disease.Disease;
import org.example.disease.DiseaseRepository;
import org.example.medication.Medication;
import org.example.medication.MedicationService;
import org.example.notification.EmailMessage;
import org.example.notification.EmailProducer;
import org.example.pet.Pet;
import org.example.pet.PetService;
import org.example.treatment.dto.CreateTreatmentRequest;
import org.example.treatment.dto.TreatmentMedicationRequest;
import org.example.treatment.dto.UpdateTreatmentRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
public class TreatmentService {
    private final TreatmentRepository treatmentRepository;
    private final PetService petService;
    private final MedicationService medicationService;
    private final EmailProducer emailProducer;
    private final DiseaseRepository diseaseRepository;

    public TreatmentService(TreatmentRepository treatmentRepository, PetService petService, MedicationService medicationService, EmailProducer emailProducer, DiseaseRepository diseaseRepository) {
        this.treatmentRepository = treatmentRepository;
        this.petService = petService;
        this.medicationService = medicationService;
        this.emailProducer = emailProducer;
        this.diseaseRepository = diseaseRepository;
    }

    @Cacheable(value = "treatments")
    public List<Treatment> findAll() {
        return treatmentRepository.findAll();
    }

    @Cacheable(value = "treatments", key = "#id")
    public Treatment findById(Long id) {
        Treatment treatment = treatmentRepository.findTreatment(id);
        if (treatment == null) {
            throw new ResourceNotFoundException("Treatment not found:" + id);
        }
        return treatment;
    }

    @Caching(evict = {
            @CacheEvict(value = "treatments", allEntries = true),
            @CacheEvict(value = "treatmentsByPet", allEntries = true)
    })
    @Transactional
    public Treatment create(CreateTreatmentRequest request) {
        validateDates(request.getStartDate(), request.getEndDate());
        Pet pet = petService.findById(request.getPetId());
        Set<Disease> diseases = findDiseases(request.getDiseaseIds());

        Treatment treatment = new Treatment(
                pet,
                diseases,
                request.getStartDate(),
                request.getEndDate(),
                request.getNextCheckupAt()
        );
        addMedications(treatment, request.getMedications());
        Treatment savedTreatment = treatmentRepository.save(treatment);
        sendCreatedEmail(savedTreatment);
        return savedTreatment;
    }

    @Caching(evict = {
            @CacheEvict(value = "treatments", allEntries = true),
            @CacheEvict(value = "treatmentsByPet", allEntries = true)
    })
    @Transactional
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

        if(request.getDiseaseIds() != null){
          Set<Disease> diseases = findDiseases(request.getDiseaseIds());
          treatment.setDiseases(diseases);
        }

        if(request.getMedications() != null){
            treatment.getTreatmentMedications().clear();
            addMedications(treatment, request.getMedications());
        }

        if(request.getStartDate() != null){
            treatment.setStartDate(request.getStartDate());
        }
        if(request.getEndDate() != null){
            treatment.setEndDate(request.getEndDate());
        }
        if(request.getNextCheckupAt() != null){
            treatment.scheduleCheckup(request.getNextCheckupAt());
        }
        Treatment savedTreatment = treatmentRepository.save(treatment);
        sendUpdatedEmail(savedTreatment);
        return savedTreatment;

    }

    @Caching(evict = {
            @CacheEvict(value = "treatments", allEntries = true),
            @CacheEvict(value = "treatmentsByPet", allEntries = true)
    })
    public Treatment complete(Long id){
        Treatment treatment = findById(id);
        treatment.complete();
        return treatmentRepository.save(treatment);
    }

    @Caching(evict = {
            @CacheEvict(value = "treatments", allEntries = true),
            @CacheEvict(value = "treatmentsByPet", allEntries = true)
    })
    public Treatment cancel(Long id){
        Treatment treatment = findById(id);
        treatment.cancel();
        return treatmentRepository.save(treatment);
    }

    @Caching(evict = {
            @CacheEvict(value = "treatments", allEntries = true),
            @CacheEvict(value = "treatmentsByPet", allEntries = true)
    })
    public Treatment reactivate(Long id){
        Treatment treatment = findById(id);
        treatment.reactivate();
        return treatmentRepository.save(treatment);
    }


    @Caching(evict = {
            @CacheEvict(value = "treatments", allEntries = true),
            @CacheEvict(value = "treatmentsByPet", allEntries = true)
    })
    public void deleteById(Long id){
        findById(id);
        treatmentRepository.deleteById(id);
    }

    @Cacheable(value = "treatmentsByPet", key = "#petId")
    public List<Treatment> findByPetId(Long petId){
        petService.findById(petId);
        return treatmentRepository.findByPetId(petId);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate){
        if(endDate != null && endDate.isBefore(startDate)){
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
    }

    private Set<Disease> findDiseases(Set<Long> diseaseIds){
        Set<Disease> diseases = new HashSet<>();
        for(Long diseaseId : diseaseIds){
            Optional<Disease> diseaseOptional = diseaseRepository.findById(diseaseId);
            if(diseaseOptional.isEmpty()){
                throw new ResourceNotFoundException("Disease not found:" + diseaseId);
            }
            diseases.add(diseaseOptional.get());
        }
        return diseases;
    }

    private void addMedications(Treatment treatment, List<TreatmentMedicationRequest> requests){
        for(TreatmentMedicationRequest request : requests){
            Medication medication = medicationService.findById(request.getMedicationId());
            treatment.addMedication(medication, request.getDosage(), request.getFrequency());
        }
    }

    private void sendCreatedEmail(Treatment treatment){
        EmailMessage emailMessage = new EmailMessage(treatment.getPet().getOwner().getEmail(),
                "PetMedTrack tedavi bildirimi",
                "Merhaba "
                        + treatment.getPet()
                        .getOwner()
                        .getFirstName()
                        + ",\n\n"
                        + treatment.getPet().getName()
                        + " adlı evcil hayvanınız için "
                        + "yeni bir tedavi oluşturuldu."
                );
            emailProducer.sendEmail(emailMessage);
    }

    private void sendUpdatedEmail(Treatment treatment){
        EmailMessage emailMessage = new EmailMessage(
                treatment.getPet().getOwner().getEmail(),
                "PetMedTrack tedavi güncellemesi",
                "Merhaba "
                        + treatment.getPet()
                        .getOwner()
                        .getFirstName()
                        + ",\n\n"
                        + treatment.getPet().getName()
                        + " adlı evcil hayvanınızın tedavi bilgileri "
                        + "güncellendi."
        );
        emailProducer.sendEmail(emailMessage);
    }
}
