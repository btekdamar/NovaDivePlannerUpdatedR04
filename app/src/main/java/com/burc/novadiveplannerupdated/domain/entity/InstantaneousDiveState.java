package com.burc.novadiveplannerupdated.domain.entity;

import java.util.Objects;

/**
 * Represents the state of the dive at a specific instant in time.
 * Used for populating the dive profile graph and showing instantaneous values.
 * Instances are immutable and created via the Builder pattern.
 */
public final class InstantaneousDiveState {

    private final long timeSeconds;         // Time from the start of the dive in seconds
    private final double depthFsw;          // Depth in feet at this instant
    private final Gas gas;                  // Gas being breathed at this instant
    private final TissueState tissueState;    // Tissue saturation state at this instant
    private final double ppo2Ata;           // Partial pressure of O2 in ATA at this instant
    private final double currentGF;         // Interpolated Gradient Factor at this instant (e.g., 0.85 for 85%)
    private final int cumulativeCNSPercent; // Cumulative CNS % from start of dive to this instant
    private final int cumulativeOTU;        // Cumulative OTUs from start of dive to this instant
    private final double cumulativeGasConsumedLiters; // Cumulative gas consumed in liters from start of dive
    // Optional: NDL and TTS can be calculated on-the-fly by the ViewModel using the tissueState, depth, and gas.
    // Including them here would mean pre-calculating for every point, which might be heavy.
    // private final Integer ndlMinutes;    // NDL from this point forward
    // private final Integer ttsSeconds;    // Time To Surface from this point forward
    // private final Double currentCeilingFsw; // Current ascent ceiling at this instant

    private InstantaneousDiveState(Builder builder) {
        this.timeSeconds = builder.timeSeconds;
        this.depthFsw = builder.depthFsw;
        this.gas = Objects.requireNonNull(builder.gas, "Gas cannot be null for InstantaneousDiveState");
        this.tissueState = Objects.requireNonNull(builder.tissueState, "TissueState cannot be null for InstantaneousDiveState");
        this.ppo2Ata = builder.ppo2Ata;
        this.currentGF = builder.currentGF;
        this.cumulativeCNSPercent = builder.cumulativeCNSPercent;
        this.cumulativeOTU = builder.cumulativeOTU;
        this.cumulativeGasConsumedLiters = builder.cumulativeGasConsumedLiters;
    }

    // --- Getters ---
    public long getTimeSeconds() { return timeSeconds; }
    public double getDepthFsw() { return depthFsw; }
    public Gas getGas() { return gas; } // Assuming Gas is immutable or a safe copy is managed elsewhere
    public TissueState getTissueState() { return new TissueState(tissueState); } // Return a copy for safety
    public double getPpo2Ata() { return ppo2Ata; }
    public double getCurrentGF() { return currentGF; }
    public int getCumulativeCNSPercent() { return cumulativeCNSPercent; }
    public int getCumulativeOTU() { return cumulativeOTU; }
    public double getCumulativeGasConsumedLiters() { return cumulativeGasConsumedLiters; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstantaneousDiveState that = (InstantaneousDiveState) o;
        return timeSeconds == that.timeSeconds &&
                Double.compare(that.depthFsw, depthFsw) == 0 &&
                Double.compare(that.ppo2Ata, ppo2Ata) == 0 &&
                Double.compare(that.currentGF, currentGF) == 0 &&
                cumulativeCNSPercent == that.cumulativeCNSPercent &&
                cumulativeOTU == that.cumulativeOTU &&
                Double.compare(that.cumulativeGasConsumedLiters, cumulativeGasConsumedLiters) == 0 &&
                gas.equals(that.gas) &&
                tissueState.equals(that.tissueState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeSeconds, depthFsw, gas, tissueState, ppo2Ata, currentGF, cumulativeCNSPercent, cumulativeOTU, cumulativeGasConsumedLiters);
    }

    @Override
    public String toString() {
        return "InstantaneousDiveState{" +
                "timeS=" + timeSeconds +
                ", depthFt=" + depthFsw +
                ", gas=" + (gas != null ? gas.getGasName() : "null") +
                ", tissueStateHash=" + (tissueState != null ? tissueState.hashCode() : "null") +
                ", ppo2Ata=" + ppo2Ata +
                ", GF=" + currentGF +
                ", CNS%=" + cumulativeCNSPercent +
                ", OTU=" + cumulativeOTU +
                ", gasUsedL=" + cumulativeGasConsumedLiters +
                '}';
    }

    // --- Builder Class ---
    public static class Builder {
        private long timeSeconds;
        private double depthFsw;
        private Gas gas;
        private TissueState tissueState;
        private double ppo2Ata;
        private double currentGF;
        private int cumulativeCNSPercent;
        private int cumulativeOTU;
        private double cumulativeGasConsumedLiters;

        public Builder(long timeSeconds, double depthFsw, Gas gas, TissueState tissueState) {
            this.timeSeconds = timeSeconds;
            this.depthFsw = depthFsw;
            this.gas = gas;
            this.tissueState = tissueState;
        }

        public Builder ppo2Ata(double ppo2Ata) {
            this.ppo2Ata = ppo2Ata;
            return this;
        }

        public Builder currentGF(double currentGF) {
            this.currentGF = currentGF;
            return this;
        }

        public Builder cumulativeCNSPercent(int cumulativeCNSPercent) {
            this.cumulativeCNSPercent = cumulativeCNSPercent;
            return this;
        }

        public Builder cumulativeOTU(int cumulativeOTU) {
            this.cumulativeOTU = cumulativeOTU;
            return this;
        }

        public Builder cumulativeGasConsumedLiters(double cumulativeGasConsumedLiters) {
            this.cumulativeGasConsumedLiters = cumulativeGasConsumedLiters;
            return this;
        }
        
        // Optional: setters for gas and tissueState if they need to be updated post-construction of builder
        // public Builder gas(Gas gas) { this.gas = gas; return this; }
        // public Builder tissueState(TissueState tissueState) { this.tissueState = tissueState; return this; }


        public InstantaneousDiveState build() {
            if (timeSeconds < 0) throw new IllegalArgumentException("Time cannot be negative.");
            if (depthFsw < 0) throw new IllegalArgumentException("Depth cannot be negative.");
            // Further validations for other fields can be added here if necessary
            return new InstantaneousDiveState(this);
        }
    }
} 