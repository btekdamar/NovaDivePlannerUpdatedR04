package com.burc.novadiveplannerupdated.domain.entity;

import com.burc.novadiveplannerupdated.domain.common.DiveConstants;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the current state of inert gas pressures (Nitrogen and Helium)
 * in all 17 tissue compartments of a diver.
 * Pressures are stored in feet of sea water (fsw).
 */
public class TissueState {

    private final double[] nitrogenPressuresFsw;
    private final double[] heliumPressuresFsw;

    /**
     * Constructor for the initial tissue state of a "clean" diver at a given ambient pressure.
     *
     * @param initialAmbientPressureFsw The initial ambient pressure at the surface/altitude in fsw.
     *                                  This value is typically obtained from {@link com.burc.novadiveplannerupdated.domain.model.AltitudeLevel}.
     */
    public TissueState(double initialAmbientPressureFsw) {
        this.nitrogenPressuresFsw = new double[DiveConstants.NUMBER_OF_TISSUE_COMPARTMENTS];
        this.heliumPressuresFsw = new double[DiveConstants.NUMBER_OF_TISSUE_COMPARTMENTS];

        for (int i = 0; i < DiveConstants.NUMBER_OF_TISSUE_COMPARTMENTS; i++) {
            // Assuming air breathing (0.79 N2, 0.0 He) and full saturation at initial ambient pressure.
            this.nitrogenPressuresFsw[i] = 0.79 * initialAmbientPressureFsw;
            this.heliumPressuresFsw[i] = 0.0;
        }
    }

    /**
     * Copy constructor. Creates a deep copy of another TissueState.
     *
     * @param other The TissueState to copy.
     */
    public TissueState(TissueState other) {
        Objects.requireNonNull(other, "Other TissueState cannot be null for copy constructor.");
        this.nitrogenPressuresFsw = Arrays.copyOf(other.nitrogenPressuresFsw, other.nitrogenPressuresFsw.length);
        this.heliumPressuresFsw = Arrays.copyOf(other.heliumPressuresFsw, other.heliumPressuresFsw.length);
    }

    /**
     * Constructor to create a TissueState with explicitly provided pressure arrays.
     * Primarily for testing or specific rehydration scenarios.
     *
     * @param nitrogenPressuresFsw Array of 17 Nitrogen pressures in fsw.
     * @param heliumPressuresFsw   Array of 17 Helium pressures in fsw.
     * @throws IllegalArgumentException if arrays are null or not of the correct length.
     */
    public TissueState(double[] nitrogenPressuresFsw, double[] heliumPressuresFsw) {
        Objects.requireNonNull(nitrogenPressuresFsw, "Nitrogen pressures array cannot be null.");
        Objects.requireNonNull(heliumPressuresFsw, "Helium pressures array cannot be null.");

        if (nitrogenPressuresFsw.length != DiveConstants.NUMBER_OF_TISSUE_COMPARTMENTS ||
            heliumPressuresFsw.length != DiveConstants.NUMBER_OF_TISSUE_COMPARTMENTS) {
            throw new IllegalArgumentException("Pressure arrays must have a length of " +
                    DiveConstants.NUMBER_OF_TISSUE_COMPARTMENTS);
        }

        this.nitrogenPressuresFsw = Arrays.copyOf(nitrogenPressuresFsw, nitrogenPressuresFsw.length);
        this.heliumPressuresFsw = Arrays.copyOf(heliumPressuresFsw, heliumPressuresFsw.length);
    }

    // --- Getters ---

    /**
     * Gets the Nitrogen partial pressure for a specific compartment.
     *
     * @param compartmentIndex The index of the compartment (0-16).
     * @return The Nitrogen pressure in fsw.
     * @throws IndexOutOfBoundsException if compartmentIndex is invalid.
     */
    public double getNitrogenPressureFsw(int compartmentIndex) {
        if (compartmentIndex < 0 || compartmentIndex >= DiveConstants.NUMBER_OF_TISSUE_COMPARTMENTS) {
            throw new IndexOutOfBoundsException("Invalid compartment index: " + compartmentIndex);
        }
        return nitrogenPressuresFsw[compartmentIndex];
    }

    /**
     * Gets the Helium partial pressure for a specific compartment.
     *
     * @param compartmentIndex The index of the compartment (0-16).
     * @return The Helium pressure in fsw.
     * @throws IndexOutOfBoundsException if compartmentIndex is invalid.
     */
    public double getHeliumPressureFsw(int compartmentIndex) {
        if (compartmentIndex < 0 || compartmentIndex >= DiveConstants.NUMBER_OF_TISSUE_COMPARTMENTS) {
            throw new IndexOutOfBoundsException("Invalid compartment index: " + compartmentIndex);
        }
        return heliumPressuresFsw[compartmentIndex];
    }

    /**
     * Returns a defensive copy of the Nitrogen pressures array.
     * @return A new array containing Nitrogen pressures in fsw for all compartments.
     */
    public double[] getNitrogenPressuresFsw() {
        return Arrays.copyOf(nitrogenPressuresFsw, nitrogenPressuresFsw.length);
    }

    /**
     * Returns a defensive copy of the Helium pressures array.
     * @return A new array containing Helium pressures in fsw for all compartments.
     */
    public double[] getHeliumPressuresFsw() {
        return Arrays.copyOf(heliumPressuresFsw, heliumPressuresFsw.length);
    }
    
    // It's generally preferred to return new instances of TissueState from calculations
    // rather than mutating existing ones directly with setters.
    // If mutation is needed, it should be done carefully and typically within a calculator service.
    // For now, no public setters are provided to encourage immutability from the outside.

    // --- equals() and hashCode() ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TissueState that = (TissueState) o;
        return Arrays.equals(nitrogenPressuresFsw, that.nitrogenPressuresFsw) &&
               Arrays.equals(heliumPressuresFsw, that.heliumPressuresFsw);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(nitrogenPressuresFsw);
        result = 31 * result + Arrays.hashCode(heliumPressuresFsw);
        return result;
    }

    @Override
    public String toString() {
        return "TissueState{" +
                "N2_Pressures_fsw=" + Arrays.toString(nitrogenPressuresFsw) +
                ", He_Pressures_fsw=" + Arrays.toString(heliumPressuresFsw) +
                '}';
    }
} 