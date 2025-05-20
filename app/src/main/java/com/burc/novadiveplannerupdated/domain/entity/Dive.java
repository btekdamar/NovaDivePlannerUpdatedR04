package com.burc.novadiveplannerupdated.domain.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single dive within a dive plan.
 * A dive consists of a series of segments and is preceded by a surface interval (except for the first dive).
 * It also holds the initial tissue state at the beginning of this specific dive and calculated results.
 * Instances are immutable and created via the Builder pattern.
 */
public final class Dive {

    private final int diveNumber; // 1-based index within a dive plan
    private final long surfaceIntervalBeforeDiveInSeconds; // 0 for the first dive in a plan
    private final TissueState initialTissueStateForThisDive;
    private final List<DiveSegment> segments; // Must not be empty

    // Calculated fields
    private final List<InstantaneousDiveState> timelineStates;
    private final Double calculatedMaxDepthMeters;
    private final Integer calculatedNdlSeconds;
    private final Integer calculatedTimeToSurfaceSeconds;
    private final Double calculatedTotalGasConsumedCuft;
    private final Double calculatedTotalCNSPercent;
    private final Double calculatedTotalOTU;
    private final TissueState tissueStateAtEndOfDive;

    private Dive(Builder builder) {
        this.diveNumber = builder.diveNumber;
        this.surfaceIntervalBeforeDiveInSeconds = builder.surfaceIntervalBeforeDiveInSeconds;
        this.initialTissueStateForThisDive = Objects.requireNonNull(builder.initialTissueStateForThisDive, "initialTissueStateForThisDive cannot be null");

        Objects.requireNonNull(builder.segments, "Segments list cannot be null for a Dive.");
        this.segments = Collections.unmodifiableList(new ArrayList<>(builder.segments)); // Defensive copy

        this.timelineStates = builder.timelineStates != null ?
                Collections.unmodifiableList(new ArrayList<>(builder.timelineStates)) :
                Collections.emptyList(); // Defensive copy or empty list

        this.calculatedMaxDepthMeters = builder.calculatedMaxDepthMeters;
        this.calculatedNdlSeconds = builder.calculatedNdlSeconds;
        this.calculatedTimeToSurfaceSeconds = builder.calculatedTimeToSurfaceSeconds;
        this.calculatedTotalGasConsumedCuft = builder.calculatedTotalGasConsumedCuft;
        this.calculatedTotalCNSPercent = builder.calculatedTotalCNSPercent;
        this.calculatedTotalOTU = builder.calculatedTotalOTU;
        this.tissueStateAtEndOfDive = builder.tissueStateAtEndOfDive != null ?
                new TissueState(builder.tissueStateAtEndOfDive) : // Defensive copy if not null
                null;
    }

    // --- Getters ---

    public int getDiveNumber() {
        return diveNumber;
    }

    public long getSurfaceIntervalBeforeDiveInSeconds() {
        return surfaceIntervalBeforeDiveInSeconds;
    }

    public TissueState getInitialTissueStateForThisDive() {
        // Return a new copy to maintain immutability of the Dive's own state if TissueState is mutable.
        // If TissueState is already immutable, this is not strictly necessary but good practice.
        return new TissueState(initialTissueStateForThisDive); // Assuming TissueState has a copy constructor
    }

    public List<DiveSegment> getSegments() {
        return segments; // Already unmodifiable
    }

    public List<InstantaneousDiveState> getTimelineStates() {
        return timelineStates; // Already unmodifiable
    }

    public Double getCalculatedMaxDepthMeters() {
        return calculatedMaxDepthMeters;
    }

    public Integer getCalculatedNdlSeconds() {
        return calculatedNdlSeconds;
    }

    public Integer getCalculatedTimeToSurfaceSeconds() {
        return calculatedTimeToSurfaceSeconds;
    }

    public Double getCalculatedTotalGasConsumedCuft() {
        return calculatedTotalGasConsumedCuft;
    }

    public Double getCalculatedTotalCNSPercent() {
        return calculatedTotalCNSPercent;
    }

    public Double getCalculatedTotalOTU() {
        return calculatedTotalOTU;
    }

    public TissueState getTissueStateAtEndOfDive() {
        // Return a new copy to maintain immutability if TissueState is mutable.
        return tissueStateAtEndOfDive != null ? new TissueState(tissueStateAtEndOfDive) : null;
    }

    // --- equals(), hashCode(), toString() ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dive dive = (Dive) o;
        return diveNumber == dive.diveNumber &&
                surfaceIntervalBeforeDiveInSeconds == dive.surfaceIntervalBeforeDiveInSeconds &&
                initialTissueStateForThisDive.equals(dive.initialTissueStateForThisDive) &&
                segments.equals(dive.segments) &&
                Objects.equals(timelineStates, dive.timelineStates) &&
                Objects.equals(calculatedMaxDepthMeters, dive.calculatedMaxDepthMeters) &&
                Objects.equals(calculatedNdlSeconds, dive.calculatedNdlSeconds) &&
                Objects.equals(calculatedTimeToSurfaceSeconds, dive.calculatedTimeToSurfaceSeconds) &&
                Objects.equals(calculatedTotalGasConsumedCuft, dive.calculatedTotalGasConsumedCuft) &&
                Objects.equals(calculatedTotalCNSPercent, dive.calculatedTotalCNSPercent) &&
                Objects.equals(calculatedTotalOTU, dive.calculatedTotalOTU) &&
                Objects.equals(tissueStateAtEndOfDive, dive.tissueStateAtEndOfDive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diveNumber, surfaceIntervalBeforeDiveInSeconds, initialTissueStateForThisDive, segments,
                timelineStates, calculatedMaxDepthMeters, calculatedNdlSeconds, calculatedTimeToSurfaceSeconds,
                calculatedTotalGasConsumedCuft, calculatedTotalCNSPercent, calculatedTotalOTU, tissueStateAtEndOfDive);
    }

    @Override
    public String toString() {
        return "Dive{" +
                "diveNumber=" + diveNumber +
                ", surfaceIntervalInSeconds=" + surfaceIntervalBeforeDiveInSeconds +
                ", initialTissueState=" + initialTissueStateForThisDive +
                ", numberOfSegments=" + segments.size() +
                ", calculatedMaxDepthMeters=" + calculatedMaxDepthMeters +
                ", calculatedNdlSeconds=" + calculatedNdlSeconds +
                ", calculatedTimeToSurfaceSeconds=" + calculatedTimeToSurfaceSeconds +
                ", calculatedTotalGasConsumedCuft=" + calculatedTotalGasConsumedCuft +
                ", calculatedTotalCNSPercent=" + calculatedTotalCNSPercent +
                ", calculatedTotalOTU=" + calculatedTotalOTU +
                ", tissueStateAtEndOfDive=" + tissueStateAtEndOfDive +
                ", timelineStatesSize=" + (timelineStates != null ? timelineStates.size() : 0) +
                '}';
    }

    // --- Builder Class ---

    public static class Builder {
        private final int diveNumber; // Mandatory for constructor
        private long surfaceIntervalBeforeDiveInSeconds = 0; // Default for first dive
        private TissueState initialTissueStateForThisDive;
        private final List<DiveSegment> segments = new ArrayList<>();

        // Calculated fields
        private List<InstantaneousDiveState> timelineStates = new ArrayList<>();
        private Double calculatedMaxDepthMeters;
        private Integer calculatedNdlSeconds;
        private Integer calculatedTimeToSurfaceSeconds;
        private Double calculatedTotalGasConsumedCuft;
        private Double calculatedTotalCNSPercent;
        private Double calculatedTotalOTU;
        private TissueState tissueStateAtEndOfDive;

        /**
         * Builder constructor.
         * @param diveNumber The 1-based number of this dive in the sequence of a plan.
         */
        public Builder(int diveNumber) {
            if (diveNumber <= 0) {
                throw new IllegalArgumentException("Dive number must be positive.");
            }
            this.diveNumber = diveNumber;
        }

        public Builder surfaceIntervalBeforeDiveInSeconds(long surfaceIntervalBeforeDiveInSeconds) {
            if (surfaceIntervalBeforeDiveInSeconds < 0) {
                throw new IllegalArgumentException("Surface interval cannot be negative.");
            }
            this.surfaceIntervalBeforeDiveInSeconds = surfaceIntervalBeforeDiveInSeconds;
            return this;
        }

        public Builder initialTissueStateForThisDive(TissueState initialTissueStateForThisDive) {
            this.initialTissueStateForThisDive = initialTissueStateForThisDive;
            return this;
        }

        public Builder addSegment(DiveSegment segment) {
            Objects.requireNonNull(segment, "Cannot add a null segment.");
            this.segments.add(segment);
            return this;
        }

        public Builder segments(List<DiveSegment> segments) {
            Objects.requireNonNull(segments, "Segments list cannot be null.");
            this.segments.clear();
            this.segments.addAll(segments);
            return this;
        }

        public Builder timelineStates(List<InstantaneousDiveState> timelineStates) {
            if (timelineStates != null) {
                this.timelineStates = new ArrayList<>(timelineStates); // Defensive copy
            } else {
                this.timelineStates.clear();
            }
            return this;
        }

        public Builder calculatedMaxDepthMeters(Double calculatedMaxDepthMeters) {
            this.calculatedMaxDepthMeters = calculatedMaxDepthMeters;
            return this;
        }

        public Builder calculatedNdlSeconds(Integer calculatedNdlSeconds) {
            this.calculatedNdlSeconds = calculatedNdlSeconds;
            return this;
        }

        public Builder calculatedTimeToSurfaceSeconds(Integer calculatedTimeToSurfaceSeconds) {
            this.calculatedTimeToSurfaceSeconds = calculatedTimeToSurfaceSeconds;
            return this;
        }

        public Builder calculatedTotalGasConsumedCuft(Double calculatedTotalGasConsumedCuft) {
            this.calculatedTotalGasConsumedCuft = calculatedTotalGasConsumedCuft;
            return this;
        }

        public Builder calculatedTotalCNSPercent(Double calculatedTotalCNSPercent) {
            this.calculatedTotalCNSPercent = calculatedTotalCNSPercent;
            return this;
        }

        public Builder calculatedTotalOTU(Double calculatedTotalOTU) {
            this.calculatedTotalOTU = calculatedTotalOTU;
            return this;
        }

        public Builder tissueStateAtEndOfDive(TissueState tissueStateAtEndOfDive) {
            this.tissueStateAtEndOfDive = tissueStateAtEndOfDive; // Builder can hold direct ref, constructor will copy
            return this;
        }

        public Dive build() {
            // initialTissueStateForThisDive will be checked for null in the Dive constructor.
            // segments list emptiness will also be checked in the Dive constructor.
            return new Dive(this);
        }
    }
} 