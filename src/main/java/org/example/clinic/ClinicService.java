package org.example.clinic;
import org.example.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClinicService {

    private final ClinicRepository clinicRepository;

    public ClinicService(ClinicRepository clinicRepository) {
        this.clinicRepository = clinicRepository;
    }

    public List<Clinic> findAll() {
        return clinicRepository.findAll();
    }

    public Clinic findById(Long id) {
        return clinicRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Clinic not found: " + id));
    }

    public Clinic create(String name, String address, String phoneNumber) {
        Clinic clinic = new Clinic(name, address, phoneNumber);
        return clinicRepository.save(clinic);
    }

    public Clinic update(Long id, String name, String address, String phoneNumber) {
        Clinic clinic = findById(id);

        clinic.setName(name);
        clinic.setAddress(address);
        clinic.setPhoneNumber(phoneNumber);

        return clinicRepository.save(clinic);
    }

    public void deleteById(Long id) {
        Clinic clinic = findById(id);
        clinicRepository.delete(clinic);
    }
}
