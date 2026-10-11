package org.example.treatment.state;
import org.example.treatment.Treatment;
import org.example.treatment.TreatmentStatus;

public class ActiveTreatmentState implements TreatmentState {
    @Override
    public TreatmentStatus getStatus(){
        return TreatmentStatus.ACTIVE;
    }

    @Override
    public void complete(Treatment treatment){
        treatment.setStatus(TreatmentStatus.COMPLETED);
    }

    @Override
    public void cancel(Treatment treatment){
        treatment.setStatus(TreatmentStatus.CANCELLED);
    }

    @Override
    public void reactivate(Treatment treatment){
        throw new IllegalStateException("Active treatment is already active.");
    }
}
