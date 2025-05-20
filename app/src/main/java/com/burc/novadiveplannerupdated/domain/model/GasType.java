package com.burc.novadiveplannerupdated.domain.model;

/**
 * Represents the type of gas circuit.
 */
public enum GasType {
    /**
     * Open Circuit: Gas is inhaled from a cylinder and exhaled directly into the water.
     */
    OPEN_CIRCUIT(0, "Open Circuit", "OC"),

    /**
     * Closed Circuit: Exhaled gas is re-processed (CO2 scrubbed, O2 added) and re-breathed.
     * In the context of dive planning, this usually refers to the diluent gas for a CCR.
     */
    CLOSED_CIRCUIT(1, "Closed Circuit", "CC");

    private final int value;
    private final String fullName;
    private final String shortName;

    GasType(int value, String fullName, String shortName) {
        this.value = value;
        this.fullName = fullName;
        this.shortName = shortName;
    }

    public int getValue() {
        return value;
    }

    public String getFullName() {
        return fullName;
    }

    public String getShortName() {
        return shortName;
    }
} 