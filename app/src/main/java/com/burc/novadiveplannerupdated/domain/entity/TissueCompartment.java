package com.burc.novadiveplannerupdated.domain.entity;

import com.burc.novadiveplannerupdated.domain.common.DiveConstants;

import java.util.Objects;

/**
 * Represents a single tissue compartment with its specific Buhlmann ZHL-16c parameters.
 * Instances are immutable.
 * All parameter values are based on the Imperial system as defined in DiveConstants.
 */
public final class TissueCompartment {

    private final int compartmentIndex; // 0 to 16

    // Nitrogen parameters
    private final double n2HalfTimeMin;
    private final double n2SurfaceHalfTimeMin;
    private final double n2_A_fsw;
    private final double n2_B;

    // Helium parameters
    private final double heHalfTimeMin;
    private final double heSurfaceHalfTimeMin;
    private final double he_A_fsw;
    private final double he_B;

    /**
     * Constructs a TissueCompartment.
     *
     * @param compartmentIndex The index of the compartment (0-16). This is used to fetch
     *                         the correct parameters from {@link DiveConstants}.
     * @throws IndexOutOfBoundsException if compartmentIndex is not between 0 and 16.
     */
    public TissueCompartment(int compartmentIndex) {
        if (compartmentIndex < 0 || compartmentIndex >= DiveConstants.NUMBER_OF_TISSUE_COMPARTMENTS) {
            throw new IndexOutOfBoundsException("Compartment index must be between 0 and " +
                    (DiveConstants.NUMBER_OF_TISSUE_COMPARTMENTS - 1) + ". Was: " + compartmentIndex);
        }
        this.compartmentIndex = compartmentIndex;

        // Populate Nitrogen parameters from DiveConstants
        this.n2HalfTimeMin = DiveConstants.N2_HALF_TIMES_MIN[compartmentIndex];
        this.n2SurfaceHalfTimeMin = DiveConstants.N2_SURFACE_HALF_TIMES_MIN[compartmentIndex];
        this.n2_A_fsw = DiveConstants.N2_A_COEFFICIENTS_FSW[compartmentIndex];
        this.n2_B = DiveConstants.N2_B_COEFFICIENTS[compartmentIndex];

        // Populate Helium parameters from DiveConstants
        this.heHalfTimeMin = DiveConstants.HE_HALF_TIMES_MIN[compartmentIndex];
        this.heSurfaceHalfTimeMin = DiveConstants.HE_SURFACE_HALF_TIMES_MIN[compartmentIndex];
        this.he_A_fsw = DiveConstants.HE_A_COEFFICIENTS_FSW[compartmentIndex];
        this.he_B = DiveConstants.HE_B_COEFFICIENTS[compartmentIndex];
    }

    // --- Getters ---
    public int getCompartmentIndex() {
        return compartmentIndex;
    }

    public double getN2HalfTimeMin() {
        return n2HalfTimeMin;
    }

    public double getN2SurfaceHalfTimeMin() {
        return n2SurfaceHalfTimeMin;
    }

    public double getN2_A_fsw() {
        return n2_A_fsw;
    }

    public double getN2_B() {
        return n2_B;
    }

    public double getHeHalfTimeMin() {
        return heHalfTimeMin;
    }

    public double getHeSurfaceHalfTimeMin() {
        return heSurfaceHalfTimeMin;
    }

    public double getHe_A_fsw() {
        return he_A_fsw;
    }

    public double getHe_B() {
        return he_B;
    }

    // --- equals() and hashCode() ---
    // Based on compartmentIndex only, as all other parameters are derived from it
    // and DiveConstants, making instances with the same index identical.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TissueCompartment that = (TissueCompartment) o;
        return compartmentIndex == that.compartmentIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(compartmentIndex);
    }

    @Override
    public String toString() {
        return "TissueCompartment{" +
                "index=" + compartmentIndex +
                ", n2HalfTime=" + n2HalfTimeMin +
                ", n2_A=" + n2_A_fsw +
                ", n2_B=" + n2_B +
                ", heHalfTime=" + heHalfTimeMin +
                ", he_A=" + he_A_fsw +
                ", he_B=" + he_B +
                '}';
    }
} 