package com.ibm.developer.identicons;

import java.util.List;

public class Circuit {
    public final int qubits;
    public final List<QuantumGate> gates;

    public Circuit(int qubits, List<QuantumGate> gates) {
        this.qubits = qubits;
        this.gates = gates;
    }
}
