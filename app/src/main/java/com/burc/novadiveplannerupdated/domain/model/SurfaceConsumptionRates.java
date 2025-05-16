package com.burc.novadiveplannerupdated.domain.model;

import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;

import java.util.Objects;

/**
 * Represents the Surface Air Consumption (SAC) or Respiratory Minute Volume (RMV) rates
 * for different phases of a dive.
 * All rates are in Imperial units (cubic feet per minute - cuft/min).
 */
public class SurfaceConsumptionRates {
    private final double rmvDiveCuFtMin; // Respiratory Minute Volume for dive phase in cubic feet per minute
    private final double rmvDecoCuFtMin; // Respiratory Minute Volume for deco phase in cubic feet per minute

    /**
     * Constructs a new SurfaceConsumptionRates instance.
     *
     * @param rmvDiveCuFtMin The RMV for the dive phase, in cuft/min. Must be positive.
     * @param rmvDecoCuFtMin The RMV for the deco phase, in cuft/min. Must be positive.
     * @throws IllegalArgumentException if either RMV value is not positive.
     */
    public SurfaceConsumptionRates(double rmvDiveCuFtMin, double rmvDecoCuFtMin) {
        if (rmvDiveCuFtMin < DomainDefaults.MIN_RMV_CUFT_MIN || rmvDiveCuFtMin > DomainDefaults.MAX_RMV_CUFT_MIN) {
            throw new IllegalArgumentException("Dive RMV (rmvDiveCuFtMin) must be between " +
                    DomainDefaults.MIN_RMV_CUFT_MIN + " and " + DomainDefaults.MAX_RMV_CUFT_MIN +
                    " cuft/min. Was: " + rmvDiveCuFtMin);
        }
        if (rmvDecoCuFtMin < DomainDefaults.MIN_RMV_CUFT_MIN || rmvDecoCuFtMin > DomainDefaults.MAX_RMV_CUFT_MIN) {
            throw new IllegalArgumentException("Deco RMV (rmvDecoCuFtMin) must be between " +
                    DomainDefaults.MIN_RMV_CUFT_MIN + " and " + DomainDefaults.MAX_RMV_CUFT_MIN +
                    " cuft/min. Was: " + rmvDecoCuFtMin);
        }
        this.rmvDiveCuFtMin = rmvDiveCuFtMin;
        this.rmvDecoCuFtMin = rmvDecoCuFtMin;
    }

    public double getRmvDiveCuFtMin() {
        return rmvDiveCuFtMin;
    }

    public double getRmvDecoCuFtMin() {
        return rmvDecoCuFtMin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SurfaceConsumptionRates that = (SurfaceConsumptionRates) o;
        return Double.compare(that.rmvDiveCuFtMin, rmvDiveCuFtMin) == 0 &&
               Double.compare(that.rmvDecoCuFtMin, rmvDecoCuFtMin) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rmvDiveCuFtMin, rmvDecoCuFtMin);
    }

    @Override
    public String toString() {
        return "SurfaceConsumptionRates{" +
                "rmvDiveCuFtMin=" + rmvDiveCuFtMin +
                ", rmvDecoCuFtMin=" + rmvDecoCuFtMin +
                '}';
    }
} 