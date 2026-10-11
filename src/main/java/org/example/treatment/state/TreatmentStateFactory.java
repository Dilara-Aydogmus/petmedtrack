package org.example.treatment.state;
import org.example.treatment.Treatment;
import org.example.treatment.TreatmentStatus;

public class TreatmentStateFactory {
    public static TreatmentState create(TreatmentStatus status){
        switch (status){
            case ACTIVE:
                return new ActiveTreatmentState();
            case COMPLETED:
                return new CompletedTreatmentState();
            case CANCELLED:
                return new CancelledTreatmentState();
            default:
                throw new IllegalStateException("Unknown treatment status:" + status);
        }
    }
}
