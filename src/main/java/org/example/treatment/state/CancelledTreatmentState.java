package org.example.treatment.state;
import org.example.treatment.Treatment;
import org.example.treatment.TreatmentStatus;

public class CancelledTreatmentState implements TreatmentState{
    @Override
    public TreatmentStatus getStatus(){
        return TreatmentStatus.CANCELLED;
    }

    @Override
    public void complete(Treatment treatment){
        throw new IllegalStateException("Cancelled treatment cannot be completed");
    }
    @Override
    public void cancel(Treatment treatment){
        throw new IllegalStateException("Treatment is already cancelled");
    }
    @Override
    public void reactivate(Treatment treatment){
        treatment.setStatus(TreatmentStatus.ACTIVE);
    }
}
