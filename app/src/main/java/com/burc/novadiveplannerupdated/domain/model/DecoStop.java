package com.burc.novadiveplannerupdated.domain.model;

import com.burc.novadiveplannerupdated.domain.entity.Gas;

import java.util.Objects;

/**
 * Represents a single decompression stop.
 */
public final class DecoStop {

    private final double depthFsw;
    private final int durationMinutes;
    private final Gas gas;

    public DecoStop(double depthFsw, int durationMinutes, Gas gas) {
        if (depthFsw < 0) {
            throw new IllegalArgumentException("Depth cannot be negative.");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        Objects.requireNonNull(gas, "Gas cannot be null for a deco stop.");

        this.depthFsw = depthFsw;
        this.durationMinutes = durationMinutes;
        this.gas = gas;
    }

    public double getDepthFsw() {
        return depthFsw;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public Gas getGas() {
        return gas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecoStop decoStop = (DecoStop) o;
        return Double.compare(decoStop.depthFsw, depthFsw) == 0 &&
                durationMinutes == decoStop.durationMinutes &&
                Objects.equals(gas, decoStop.gas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(depthFsw, durationMinutes, gas);
    }

    @Override
    public String toString() {
        return "DecoStop{" +
                "depthFsw=" + depthFsw +
                ", durationMinutes=" + durationMinutes +
                ", gas=" + gas.getGasName() + // Assuming Gas has a getName() or similar
                '}';
    }
} 