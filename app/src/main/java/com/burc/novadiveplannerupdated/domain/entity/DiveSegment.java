package com.burc.novadiveplannerupdated.domain.entity;

import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;
import com.burc.novadiveplannerupdated.domain.model.GasType;

import java.util.Objects;

/**
 * Represents a single segment of a dive.
 * Instances are immutable and created via the Builder pattern.
 * All calculations and stored values use the Imperial system (feet, ft/min).
 */
public final class DiveSegment {

    // --- Fields ---
    private final int segmentNumber;
    private final double targetDepth; // in feet
    private final long userInputTotalDurationInSeconds; // Includes transit time to targetDepth + time at targetDepth
    private final Gas gas;
    private final double descentRate; // in ft/min
    private final double ascentRate;  // in ft/min
    private final double setPoint;    // PPO2 setpoint for CC, 0.0 for OC

    // Calculated fields (to be populated by UseCases after Buhlmann etc. calculations)
    private final TissueState tissueStateAtEndOfSegment; // Nullable if not yet calculated or for a user-input-only segment
    private final Double calculatedTransitDurationSeconds; // Nullable
    private final Double gasConsumedInSegmentCuft; // Amount of gas consumed IN THIS SEGMENT, in cubic feet. Nullable.
    private final Double cnsAddedInSegmentPercent; // CNS percentage ADDED IN THIS SEGMENT. Nullable.
    private final Double otusAddedInSegment;       // OTUs ADDED IN THIS SEGMENT. Nullable.

    // --- Constructor ---
    private DiveSegment(Builder builder) {
        this.segmentNumber = builder.segmentNumber;
        this.targetDepth = builder.targetDepth;
        this.userInputTotalDurationInSeconds = builder.userInputTotalDurationInSeconds;
        this.gas = builder.gas;
        // Builder.build() içinde son değerler zaten builder.descentRate vb. alanlara atanmıştı.
        this.descentRate = builder.descentRate;
        this.ascentRate = builder.ascentRate;
        this.setPoint = builder.setPoint;

        this.tissueStateAtEndOfSegment = builder.tissueStateAtEndOfSegment;
        this.calculatedTransitDurationSeconds = builder.calculatedTransitDurationSeconds;
        this.gasConsumedInSegmentCuft = builder.gasConsumedInSegmentCuft;
        this.cnsAddedInSegmentPercent = builder.cnsAddedInSegmentPercent;
        this.otusAddedInSegment = builder.otusAddedInSegment;
    }

    // --- Getters ---
    public int getSegmentNumber() {
        return segmentNumber;
    }

    public double getTargetDepth() {
        return targetDepth;
    }

    public long getUserInputTotalDurationInSeconds() {
        return userInputTotalDurationInSeconds;
    }

    public Gas getGas() {
        return gas;
    }

    public double getDescentRate() {
        return descentRate;
    }

    public double getAscentRate() {
        return ascentRate;
    }

    public double getSetPoint() {
        return setPoint;
    }

    // Getters for new calculated fields
    public TissueState getTissueStateAtEndOfSegment() {
        // Return a new copy if TissueState is mutable and immutability is desired here.
        // Assuming TissueState is immutable or its immutability is handled by the caller/setter context.
        return tissueStateAtEndOfSegment;
    }

    public Double getCalculatedTransitDurationSeconds() {
        return calculatedTransitDurationSeconds;
    }

    public Double getGasConsumedInSegmentCuft() {
        return gasConsumedInSegmentCuft;
    }

    public Double getCnsAddedInSegmentPercent() {
        return cnsAddedInSegmentPercent;
    }

    public Double getOtusAddedInSegment() {
        return otusAddedInSegment;
    }

    // --- equals() and hashCode() ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiveSegment that = (DiveSegment) o;
        return segmentNumber == that.segmentNumber &&
                Double.compare(that.targetDepth, targetDepth) == 0 &&
                userInputTotalDurationInSeconds == that.userInputTotalDurationInSeconds &&
                Double.compare(that.descentRate, descentRate) == 0 &&
                Double.compare(that.ascentRate, ascentRate) == 0 &&
                Double.compare(that.setPoint, setPoint) == 0 &&
                Objects.equals(gas, that.gas) &&
                Objects.equals(tissueStateAtEndOfSegment, that.tissueStateAtEndOfSegment) &&
                Objects.equals(calculatedTransitDurationSeconds, that.calculatedTransitDurationSeconds) &&
                Objects.equals(gasConsumedInSegmentCuft, that.gasConsumedInSegmentCuft) &&
                Objects.equals(cnsAddedInSegmentPercent, that.cnsAddedInSegmentPercent) &&
                Objects.equals(otusAddedInSegment, that.otusAddedInSegment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(segmentNumber, targetDepth, userInputTotalDurationInSeconds, gas, descentRate, ascentRate, setPoint,
                tissueStateAtEndOfSegment, calculatedTransitDurationSeconds, gasConsumedInSegmentCuft, cnsAddedInSegmentPercent, otusAddedInSegment);
    }

    @Override
    public String toString() {
        return "DiveSegment{" +
                "segmentNumber=" + segmentNumber +
                ", targetDepth=" + targetDepth + " ft" +
                ", userInputTotalDurationInSeconds=" + userInputTotalDurationInSeconds + "s" +
                ", gas=" + (gas != null ? gas.getGasName() : "null") +
                ", descentRate=" + descentRate + " ft/min" +
                ", ascentRate=" + ascentRate + " ft/min" +
                ", setPoint=" + setPoint +
                // Add new fields to toString if useful for debugging
                (tissueStateAtEndOfSegment != null ? ", tissueStateAtEndHash=" + tissueStateAtEndOfSegment.hashCode() : "") +
                (calculatedTransitDurationSeconds != null ? ", transitSecs=" + calculatedTransitDurationSeconds : "") +
                (gasConsumedInSegmentCuft != null ? ", gasConsumedCuft=" + gasConsumedInSegmentCuft : "") +
                (cnsAddedInSegmentPercent != null ? ", cnsAdded%=" + cnsAddedInSegmentPercent : "") +
                (otusAddedInSegment != null ? ", otusAdded=" + otusAddedInSegment : "") +
                '}';
    }

    // --- Builder Class ---
    public static class Builder {
        private int segmentNumber;
        private double targetDepth; // feet
        private long userInputTotalDurationInSeconds;
        private Gas gas;
        private Double descentRate; // ft/min, if null, default will be used
        private Double ascentRate;  // ft/min, if null, default will be used
        private Double setPoint;    // PPO2 for CC

        // Fields for calculated data
        private TissueState tissueStateAtEndOfSegment = null;
        private Double calculatedTransitDurationSeconds = null;
        private Double gasConsumedInSegmentCuft = null;
        private Double cnsAddedInSegmentPercent = null;
        private Double otusAddedInSegment = null;

        public Builder() {
            // No-arg constructor
        }

        public Builder segmentNumber(int segmentNumber) {
            this.segmentNumber = segmentNumber;
            return this;
        }

        public Builder targetDepth(double targetDepth) {
            this.targetDepth = targetDepth;
            return this;
        }

        public Builder userInputTotalDurationInSeconds(long durationInSeconds) {
            this.userInputTotalDurationInSeconds = durationInSeconds;
            return this;
        }

        public Builder gas(Gas gas) {
            this.gas = gas;
            return this;
        }

        public Builder descentRate(Double descentRate) {
            this.descentRate = descentRate;
            return this;
        }

        public Builder ascentRate(Double ascentRate) {
            this.ascentRate = ascentRate;
            return this;
        }

        public Builder setPoint(Double setPoint) {
            this.setPoint = setPoint;
            return this;
        }

        // Setters for calculated fields
        public Builder tissueStateAtEndOfSegment(TissueState tissueState) {
            this.tissueStateAtEndOfSegment = tissueState;
            return this;
        }

        public Builder calculatedTransitDurationSeconds(Double seconds) {
            this.calculatedTransitDurationSeconds = seconds;
            return this;
        }

        public Builder gasConsumedInSegmentCuft(Double cuft) {
            this.gasConsumedInSegmentCuft = cuft;
            return this;
        }

        public Builder cnsAddedInSegmentPercent(Double cnsPercent) {
            this.cnsAddedInSegmentPercent = cnsPercent;
            return this;
        }

        public Builder otusAddedInSegment(Double otu) {
            this.otusAddedInSegment = otu;
            return this;
        }

        public DiveSegment build() {
            Objects.requireNonNull(gas, "Gas cannot be null for a DiveSegment.");

            // Assign final rates and setPoint to builder fields before passing to constructor
            this.descentRate = (this.descentRate != null) ? this.descentRate : DomainDefaults.DEFAULT_DESCENT_RATE_FT_MIN;
            this.ascentRate = (this.ascentRate != null) ? this.ascentRate : DomainDefaults.DEFAULT_ASCENT_RATE_FT_MIN;

            if (gas.getGasType() == GasType.OPEN_CIRCUIT) {
                this.setPoint = 0.0;
            } else { // CLOSED_CIRCUIT
                Objects.requireNonNull(this.setPoint, "SetPoint cannot be null for a CC gas.");
                if (this.setPoint < DomainDefaults.MIN_SET_POINT || this.setPoint > DomainDefaults.MAX_SET_POINT) {
                    throw new IllegalArgumentException("SetPoint for CC gas (" + this.setPoint + ") is out of valid range (" +
                            DomainDefaults.MIN_SET_POINT + "-" + DomainDefaults.MAX_SET_POINT + ").");
                }
                // this.setPoint is already assigned if valid
            }

            // Basic validations
            if (targetDepth < 0)
                throw new IllegalArgumentException("Target depth cannot be negative. Was: " + targetDepth);
            if (userInputTotalDurationInSeconds < 0)
                throw new IllegalArgumentException("Duration cannot be negative. Was: " + userInputTotalDurationInSeconds);
            if (this.descentRate <= 0)
                throw new IllegalArgumentException("Descent rate must be positive. Was: " + this.descentRate);
            if (this.ascentRate <= 0)
                throw new IllegalArgumentException("Ascent rate must be positive. Was: " + this.ascentRate);
            if (segmentNumber <= 0)
                throw new IllegalArgumentException("Segment number must be positive. Was: " + segmentNumber);


            return new DiveSegment(this);
        }
    }
} 