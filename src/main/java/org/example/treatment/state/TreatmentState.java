package org.example.treatment.state;
import org.example.treatment.Treatment;
import org.example.treatment.TreatmentStatus;

public interface TreatmentState {
    TreatmentStatus getStatus();

    void complete(Treatment treatment);
    void cancel(Treatment treatment);
    void reactivate(Treatment treatment);
}
