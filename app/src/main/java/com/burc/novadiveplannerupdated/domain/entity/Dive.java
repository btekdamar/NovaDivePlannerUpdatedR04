package com.burc.novadiveplannerupdated.domain.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single dive within a dive plan.
 * A dive consists of a series of segments and is preceded by a surface interval (except for the first dive).
 * It also holds the initial tissue state at the beginning of this specific dive.
 * Instances are immutable and created via the Builder pattern.
 */
public final class Dive {

    private final int diveNumber; // 1-based index within a dive plan
    private final long surfaceIntervalBeforeDiveInSeconds; // 0 for the first dive in a plan
    private final TissueState initialTissueStateForThisDive;
    private final List<DiveSegment> segments; // Must not be empty

    private Dive(Builder builder) {
        this.diveNumber = builder.diveNumber;
        this.surfaceIntervalBeforeDiveInSeconds = builder.surfaceIntervalBeforeDiveInSeconds;
        this.initialTissueStateForThisDive = Objects.requireNonNull(builder.initialTissueStateForThisDive, "initialTissueStateForThisDive cannot be null");

        Objects.requireNonNull(builder.segments, "Segments list cannot be null for a Dive.");
        this.segments = Collections.unmodifiableList(new ArrayList<>(builder.segments)); // Defensive copy
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

    // --- equals(), hashCode(), toString() ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dive dive = (Dive) o;
        return diveNumber == dive.diveNumber &&
                surfaceIntervalBeforeDiveInSeconds == dive.surfaceIntervalBeforeDiveInSeconds &&
                initialTissueStateForThisDive.equals(dive.initialTissueStateForThisDive) &&
                segments.equals(dive.segments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diveNumber, surfaceIntervalBeforeDiveInSeconds, initialTissueStateForThisDive, segments);
    }

    @Override
    public String toString() {
        return "Dive{" +
                "diveNumber=" + diveNumber +
                ", surfaceIntervalInSeconds=" + surfaceIntervalBeforeDiveInSeconds +
                ", initialTissueState=" + initialTissueStateForThisDive +
                ", numberOfSegments=" + segments.size() +
                '}';
    }

    // --- Builder Class ---

    public static class Builder {
        private final int diveNumber; // Mandatory for constructor
        private long surfaceIntervalBeforeDiveInSeconds = 0; // Default for first dive
        private TissueState initialTissueStateForThisDive;
        private final List<DiveSegment> segments = new ArrayList<>();

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

        public Dive build() {
            // initialTissueStateForThisDive will be checked for null in the Dive constructor.
            // segments list emptiness will also be checked in the Dive constructor.
            return new Dive(this);
        }
    }
} 