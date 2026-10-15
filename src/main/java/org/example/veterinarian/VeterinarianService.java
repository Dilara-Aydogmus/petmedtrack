package org.example.veterinarian;

import org.example.clinic.Clinic;
import org.example.clinic.ClinicService;
import org.example.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VeterinarianService {

    private final VeterinarianRepository veterinarianRepository;
    private final ClinicService clinicService;

    public VeterinarianService(
            VeterinarianRepository veterinarianRepository,
            ClinicService clinicService
    ) {
        this.veterinarianRepository = veterinarianRepository;
        this.clinicService = clinicService;
    }

    public List<Veterinarian> findAll() {
        return veterinarianRepository.findAll();
    }

    public Veterinarian findById(Long id) {
        return veterinarianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Veterinarian not found: " + id
                ));
    }

    public List<Veterinarian> findByClinicId(Long clinicId) {
        Clinic clinic = clinicService.findById(clinicId);

        return veterinarianRepository.findByClinicId(clinic.getId());
    }

    public Veterinarian create(
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            Long clinicId
    ) {
        Clinic clinic = clinicService.findById(clinicId);

        Veterinarian veterinarian = new Veterinarian(
                firstName,
                lastName,
                email,
                phoneNumber,
                clinic
        );

        return veterinarianRepository.save(veterinarian);
    }

    public Veterinarian update(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            Long clinicId
    ) {
        Veterinarian veterinarian = findById(id);
        Clinic clinic = clinicService.findById(clinicId);

        veterinarian.setFirstName(firstName);
        veterinarian.setLastName(lastName);
        veterinarian.setEmail(email);
        veterinarian.setPhoneNumber(phoneNumber);
        veterinarian.setClinic(clinic);

        return veterinarianRepository.save(veterinarian);
    }

    public void deleteById(Long id) {
        Veterinarian veterinarian = findById(id);
        veterinarianRepository.delete(veterinarian);
    }
}
