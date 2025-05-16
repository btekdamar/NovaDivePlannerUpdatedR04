package com.burc.novadiveplannerupdated.domain.model;

/**
 * Represents the type of gas circuit.
 */
public enum GasType {
    /**
     * Open Circuit: Gas is inhaled from a cylinder and exhaled directly into the water.
     */
    OPEN_CIRCUIT,

    /**
     * Closed Circuit: Exhaled gas is re-processed (CO2 scrubbed, O2 added) and re-breathed.
     * In the context of dive planning, this usually refers to the diluent gas for a CCR.
     */
    CLOSED_CIRCUIT
} 