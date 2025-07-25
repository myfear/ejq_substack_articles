package com.ibm.developer.identicons;

public class QuantumGate {
    public final QuantumGateType type;
    public final int qubitIndex;
    public final int timeStep;
    public final int controlQubit; // for multi-qubit gates

    public QuantumGate(QuantumGateType type, int qubitIndex, int timeStep) {
        this(type, qubitIndex, timeStep, -1);
    }

    public QuantumGate(QuantumGateType type, int qubitIndex, int timeStep, int controlQubit) {
        this.type = type;
        this.qubitIndex = qubitIndex;
        this.timeStep = timeStep;
        this.controlQubit = controlQubit;
    }
}
