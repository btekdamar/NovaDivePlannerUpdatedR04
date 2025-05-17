package com.burc.novadiveplannerupdated.domain.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a complete dive plan, which includes overall settings and a list of one or more dives.
 * Instances are immutable and created via the Builder pattern.
 */
public final class DivePlan {

    private final String id; // Unique identifier for the dive plan
    private final String planTitle;
    private final DiveSettings settings; // Snapshot of settings used for this plan
    private final List<Dive> dives;    // Must contain at least one dive
    // Optional: Timestamps for creation/modification
    // private final long creationTimestamp;
    // private final long lastModifiedTimestamp;

    private DivePlan(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Plan ID cannot be null.");
        this.planTitle = builder.planTitle; // Allowed to be null or empty
        this.settings = Objects.requireNonNull(builder.settings, "DiveSettings cannot be null for a DivePlan.");

        Objects.requireNonNull(builder.dives, "Dives list cannot be null for a DivePlan.");
        if (builder.dives.isEmpty()) {
            throw new IllegalArgumentException("A DivePlan must contain at least one Dive.");
        }
        // Ensure dives are ordered by diveNumber if not already guaranteed by how they are added
        // For now, assume they are added in order or order is maintained by the list structure.
        this.dives = Collections.unmodifiableList(new ArrayList<>(builder.dives)); // Defensive copy
        
        // Example for timestamps if added:
        // this.creationTimestamp = builder.creationTimestamp;
        // this.lastModifiedTimestamp = builder.lastModifiedTimestamp;
    }

    // --- Getters ---

    public String getId() {
        return id;
    }

    public String getPlanTitle() {
        return planTitle;
    }

    public DiveSettings getSettings() {
        // Return a new copy to ensure immutability of the plan's settings snapshot
        // if DiveSettings itself were mutable or if we want to be extra safe.
        // Assumes DiveSettings has a copy constructor or a builder that can take an instance.
        return new DiveSettings.Builder(settings).build(); 
    }

    public List<Dive> getDives() {
        return dives; // Already unmodifiable
    }

    // Optional: Getters for timestamps

    // --- equals(), hashCode(), toString() ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DivePlan divePlan = (DivePlan) o;
        return id.equals(divePlan.id) &&
                Objects.equals(planTitle, divePlan.planTitle) &&
                settings.equals(divePlan.settings) &&
                dives.equals(divePlan.dives);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, planTitle, settings, dives);
    }

    @Override
    public String toString() {
        return "DivePlan{" +
                "id='" + id + '\'' +
                ", planTitle='" + planTitle + '\'' +
                ", settings=" + settings + // Relies on DiveSettings.toString()
                ", numberOfDives=" + dives.size() +
                '}';
    }

    // --- Builder Class ---
    public static class Builder {
        private String id;
        private String planTitle = ""; // Default to empty string
        private DiveSettings settings;
        private final List<Dive> dives = new ArrayList<>();
        // private long creationTimestamp;
        // private long lastModifiedTimestamp;

        /**
         * Builder constructor that automatically generates a UUID for the plan ID.
         * Requires initial DiveSettings for the plan.
         * @param initialSettings The dive settings to be associated with this plan (a snapshot will be taken).
         */
        public Builder(DiveSettings initialSettings) {
            this.id = UUID.randomUUID().toString();
            Objects.requireNonNull(initialSettings, "Initial DiveSettings cannot be null.");
            // Create a snapshot of the settings
            this.settings = new DiveSettings.Builder(initialSettings).build();
            // this.creationTimestamp = System.currentTimeMillis();
            // this.lastModifiedTimestamp = this.creationTimestamp;
        }
        
        /**
         * Builder constructor allowing a specific ID to be provided.
         * Useful for reconstructing a DivePlan from storage.
         * @param id The unique ID for this dive plan.
         * @param initialSettings The dive settings for this plan (should be the stored snapshot).
         */
        public Builder(String id, DiveSettings initialSettings) {
            Objects.requireNonNull(id, "DivePlan ID cannot be null.");
            Objects.requireNonNull(initialSettings, "Initial DiveSettings cannot be null.");
            this.id = id;
            this.settings = new DiveSettings.Builder(initialSettings).build(); // Take snapshot
            // Timestamps would typically be set from stored values here
        }

        public Builder planTitle(String planTitle) {
            this.planTitle = planTitle;
            // this.lastModifiedTimestamp = System.currentTimeMillis();
            return this;
        }

        /**
         * Updates the settings for the DivePlan. A new snapshot of the provided settings is taken.
         */
        public Builder settings(DiveSettings newSettings) {
            Objects.requireNonNull(newSettings, "DiveSettings cannot be null.");
            this.settings = new DiveSettings.Builder(newSettings).build(); // Create a new snapshot
            // this.lastModifiedTimestamp = System.currentTimeMillis();
            return this;
        }

        public Builder addDive(Dive dive) {
            Objects.requireNonNull(dive, "Cannot add a null Dive to DivePlan.");
            this.dives.add(dive);
            // this.lastModifiedTimestamp = System.currentTimeMillis();
            return this;
        }

        public Builder dives(List<Dive> dives) {
            Objects.requireNonNull(dives, "Dives list cannot be null for DivePlan.");
            this.dives.clear();
            this.dives.addAll(dives);
            // this.lastModifiedTimestamp = System.currentTimeMillis();
            return this;
        }
        
        public DivePlan build() {
            // ID and settings are guaranteed by constructors.
            // Dives list emptiness is checked in DivePlan constructor.
            return new DivePlan(this);
        }
    }
} 