package org.example;

public class HyperLogLog {

    private final int p; // Precision
    private final int m; // Number of registers, m = 2^p
    private final byte[] registers;
    private final double alphaM;

    public HyperLogLog(int p) {
        if (p < 4 || p > 16) {
            throw new IllegalArgumentException("Precision p must be between 4 and 16.");
        }
        this.p = p;
        this.m = 1 << p; // 2^p
        this.registers = new byte[m];
        this.alphaM = getAlpha(m);
    }

    // Hash function placeholder (in a real app, use Murmur3)
    private int hash(Object o) {
        return o.hashCode();
    }

    // Pre-calculated constant for bias correction
    private double getAlpha(int m) {
        switch (m) {
            case 16:
                return 0.673;
            case 32:
                return 0.697;
            case 64:
                return 0.709;
            default:
                return 0.7213 / (1 + 1.079 / m);
        }
    }

    public long getMemoryUsageBytes() {
        return this.m; // Each register is a byte
    }

    public void add(Object o) {
        int hash = hash(o);

        // 1. Determine the register index from the first 'p' bits
        int registerIndex = hash >>> (Integer.SIZE - p);

        // 2. Get the rest of the hash for counting leading zeros
        int valueForCounting = hash << p;

        // 3. Count leading zeros (+1 because we count from 1)
        byte leadingZeros = (byte) (Integer.numberOfLeadingZeros(valueForCounting) + 1);

        // 4. Update the register with the maximum value seen
        registers[registerIndex] = (byte) Math.max(registers[registerIndex], leadingZeros);
    }

    public double estimate() {
        double sum = 0.0;
        int zeroRegisters = 0;

        for (byte registerValue : registers) {
            if (registerValue == 0) {
                zeroRegisters++;
            }
            sum += 1.0 / (1 << registerValue); // sum of 2^(-R_j)
        }

        double rawEstimate = alphaM * m * m / sum;

        // Small and large range corrections (important for accuracy!)
        if (rawEstimate <= 2.5 * m) {
            if (zeroRegisters > 0) {
                // Small range correction
                return Math.round(m * Math.log((double) m / zeroRegisters));
            } else {
                return Math.round(rawEstimate);
            }
        } else if (rawEstimate > (1.0 / 30.0) * Math.pow(2, 32)) {
            // Large range correction
            return -Math.pow(2, 32) * Math.log(1.0 - rawEstimate / Math.pow(2, 32));
        } else {
            return Math.round(rawEstimate);
        }
    }
}