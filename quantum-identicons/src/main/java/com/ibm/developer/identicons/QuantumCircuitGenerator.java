package com.ibm.developer.identicons;

import java.util.ArrayList;
import java.util.List;

public class QuantumCircuitGenerator {

    public Circuit generate(String input) {
        byte[] hash = HashProcessor.hash(input);
        int qubits = 2 + (hash[0] & 0x03); // 2–5
        int gateCount = 3 + (hash[1] & 0x07); // 3–10

        List<QuantumGate> gates = new ArrayList<>();
        for (int i = 0; i < gateCount; i++) {
            int b = Byte.toUnsignedInt(hash[2 + i]);
            int typeIndex = b % QuantumGateType.values().length;
            QuantumGateType type = QuantumGateType.values()[typeIndex];
            int qubit = b % qubits;
            int step = i;

            if (type == QuantumGateType.CNOT) {
                int control = (b + 1) % qubits;
                gates.add(new QuantumGate(type, qubit, step, control));
            } else {
                gates.add(new QuantumGate(type, qubit, step));
            }
        }

        return new Circuit(qubits, gates);
    }
}