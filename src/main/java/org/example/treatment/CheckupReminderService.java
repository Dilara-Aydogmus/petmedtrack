package org.example.treatment;
import org.example.notification.EmailMessage;
import org.example.notification.EmailProducer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CheckupReminderService {
    private final TreatmentRepository treatmentRepository;
    private final EmailProducer emailProducer;

    public CheckupReminderService(TreatmentRepository treatmentRepository, EmailProducer emailProducer){
        this.treatmentRepository = treatmentRepository;
        this.emailProducer = emailProducer;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendDueCheckupReminders(){
    List<Treatment> treatments = treatmentRepository.findTreatmentsDueForCheckup(LocalDateTime.now());
    for (Treatment treatment : treatments){
        String ownerEmail = treatment.getPet().getOwner().getEmail();
        String ownerName = treatment.getPet().getOwner().getFirstName();
        String petName = treatment.getPet().getName();

        EmailMessage email = new EmailMessage(ownerEmail, "PetMedTrack kontrol hatırlatması\n",
                "Merhaba " + ownerName + ",\n\n" + petName + " isimli hayvanınızın  kontrol zamanı gelmiştir.");

        emailProducer.sendEmail(email);
        treatment.setCheckupReminderSent(true);
    }

    treatmentRepository.saveAll(treatments);
    }
}
