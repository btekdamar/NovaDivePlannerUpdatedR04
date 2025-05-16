package com.burc.novadiveplannerupdated.domain.model;

/**
 * Represents the unit system used in the application.
 * All core calculations are performed in IMPERIAL units.
 * METRIC is used for display purposes and requires conversion.
 */
public enum UnitSystem {
    /**
     * Imperial unit system (e.g., feet, pounds).
     * This is the default unit system for calculations.
     */
    IMPERIAL,

    /**
     * Metric unit system (e.g., meters, kilograms).
     * Values in this system are typically converted from IMPERIAL for display.
     */
    METRIC
} 