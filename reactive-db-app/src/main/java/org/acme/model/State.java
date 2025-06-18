package org.acme.model;

import java.io.Serializable;

public class State implements Serializable {

    private int currentStep;

    public State() {
        this.currentStep = 0; // Default step
    }
    public int getCurrentStep() {
        return currentStep;
    }
    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }
}
