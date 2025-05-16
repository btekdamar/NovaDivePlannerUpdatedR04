package com.burc.novadiveplannerupdated.domain.model;

import java.util.Objects;

/**
 * Represents calculated properties for a given gas mixture and dive settings.
 * This is a Value Object, meaning its instances are immutable once created.
 */
public class GasProperties {

    private final String calculatedGasName; // e.g., "AIR", "NX 32", "TX 18/45"
    private final Double mod; // Maximum Operating Depth in feet (null if not applicable, e.g., for CC diluent in some contexts)
    private final Double ht;  // Hypoxic Threshold in feet (null if not applicable)
    private final Double endLimit; // Depth limit for END alarm in feet (null if alarm is off or not applicable)
    private final Double wobLimit; // Depth limit for WOB alarm in feet (null if alarm is off or not applicable)

    public GasProperties(String calculatedGasName, Double mod, Double ht, Double endLimit, Double wobLimit) {
        this.calculatedGasName = calculatedGasName;
        this.mod = mod;
        this.ht = ht;
        this.endLimit = endLimit;
        this.wobLimit = wobLimit;
    }

    public String getCalculatedGasName() {
        return calculatedGasName;
    }

    public Double getMod() {
        return mod;
    }

    public Double getHt() {
        return ht;
    }

    public Double getEndLimit() {
        return endLimit;
    }

    public Double getWobLimit() {
        return wobLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GasProperties that = (GasProperties) o;
        return Objects.equals(calculatedGasName, that.calculatedGasName) &&
                Objects.equals(mod, that.mod) &&
                Objects.equals(ht, that.ht) &&
                Objects.equals(endLimit, that.endLimit) &&
                Objects.equals(wobLimit, that.wobLimit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(calculatedGasName, mod, ht, endLimit, wobLimit);
    }

    @Override
    public String toString() {
        return "GasProperties{" +
                "calculatedGasName='" + calculatedGasName + '\'' +
                ", mod=" + mod +
                ", ht=" + ht +
                ", endLimit=" + endLimit +
                ", wobLimit=" + wobLimit +
                '}';
    }
} 