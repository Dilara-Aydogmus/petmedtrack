package org.example.treatment.state;

import org.example.treatment.Treatment;
import org.example.treatment.TreatmentStatus;

public class CompletedTreatmentState implements TreatmentState {
    @Override
    public TreatmentStatus getStatus(){
        return TreatmentStatus.COMPLETED;
    }

    @Override
    public void complete(Treatment treatment){
        throw new IllegalStateException("Completed treatment cannot be completed again");
    }

    @Override
    public void cancel(Treatment treatment){
        throw new IllegalStateException("Completed treatment cannot be cancelled");
    }

    @Override
    public void reactivate(Treatment treatment){
        treatment.setStatus(TreatmentStatus.ACTIVE);
    }

}
