package com.ibm.developer.identicons;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class CircuitRenderer {

    public BufferedImage render(Circuit circuit) {
        int width = 400;
        int height = 100 + circuit.qubits * 40;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(IBMColorPalette.GRAY_10);
        g.fillRect(0, 0, width, height);

        // Draw qubit wires
        for (int i = 0; i < circuit.qubits; i++) {
            int y = 50 + i * 40;
            g.setColor(IBMColorPalette.BLUE_60);
            g.drawLine(20, y, width - 20, y);
        }

        // Draw gates
        for (QuantumGate gate : circuit.gates) {
            int x = 40 + gate.timeStep * 40;
            int y = 50 + gate.qubitIndex * 40;

            switch (gate.type) {
                case HADAMARD -> drawGate(g, x, y, "H", IBMColorPalette.BLUE_50);
                case PAULI_X -> drawGate(g, x, y, "X", IBMColorPalette.TEAL_50);
                case CNOT -> drawCNOT(g, x, y, 50 + gate.controlQubit * 40);
                default -> drawGate(g, x, y, gate.type.name().substring(0, 1), IBMColorPalette.BLUE_50);
            }
        }

        g.dispose();
        return image;
    }

    private void drawGate(Graphics2D g, int x, int y, String label, Color color) {
        g.setColor(color);
        g.fillRect(x - 10, y - 10, 20, 20);
        g.setColor(IBMColorPalette.GRAY_90);
        g.drawString(label, x - 4, y + 5);
    }

    private void drawCNOT(Graphics2D g, int targetX, int targetY, int controlY) {
        g.setColor(IBMColorPalette.TEAL_50);
        g.drawLine(targetX, controlY, targetX, targetY);
        g.fillOval(targetX - 4, controlY - 4, 8, 8);
        g.drawOval(targetX - 10, targetY - 10, 20, 20);
        g.drawLine(targetX, targetY - 8, targetX, targetY + 8);
        g.drawLine(targetX - 8, targetY, targetX + 8, targetY);
    }
}