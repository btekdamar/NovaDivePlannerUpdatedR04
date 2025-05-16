package com.burc.novadiveplannerupdated.domain.usecase.gas;

import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;

import javax.inject.Inject;

public class CalculateEndUseCase {

    // private static final double DEPTH_CONSTANT_METRIC = 10.0; // msw
    // private static final double DEPTH_CONSTANT_IMPERIAL = 33.0; // fsw

    @Inject
    public CalculateEndUseCase() {
    }

    /**
     * Calculates the Equivalent Narcotic Depth (END).
     *
     * @param gas The breathing gas.
     * @param depth The current depth in units specified by unitSystem.
     * @param unitSystem The unit system (METRIC or IMPERIAL).
     * @param isOxygenNarcotic True if oxygen should be considered narcotic, false otherwise.
     * @return The calculated END, rounded to the nearest integer. Returns null for invalid inputs.
     */
    public Integer execute(Gas gas, int depth, UnitSystem unitSystem, boolean isOxygenNarcotic) {
        if (gas == null || depth < 0) {
            return null; // END cannot be calculated for invalid inputs
        }

        double fo2 = gas.getFo2();
        double fHe = gas.getFhe();
        double fn2 = 1.0 - fo2 - fHe;

        // Ensure fn2 is not negative due to potential floating point inaccuracies with fo2 + fHe slightly > 1
        if (fn2 < 0) {
            fn2 = 0;
        }

        double narcoticFraction;
        if (isOxygenNarcotic) {
            narcoticFraction = fn2 + fo2; // Equivalent to 1.0 - fHe
        } else {
            narcoticFraction = fn2;
        }

        // Ensure narcoticFraction is not negative (e.g. if fHe > 1 due to bad input, 1-fHe could be negative)
        if (narcoticFraction < 0) {
            narcoticFraction = 0;
        }

        double depthConstant = (unitSystem == UnitSystem.METRIC) ? DomainDefaults.DEPTH_CONSTANT_METRIC : DomainDefaults.DEPTH_CONSTANT_IMPERIAL;
        double surfacePressureConstant = DomainDefaults.SURFACE_PRESSURE_ATA; // Although depthConstant already considers this by being 'per ATA'
                                                                            // the formula is (D+X)*F - X, where X is the constant in depth units for 1 ATA.
                                                                            // So using SURFACE_PRESSURE_ATA directly in the calculation (depth + depthConstant / surfacePressureConstant) * ... - depthConstant / surfacePressureConstant
                                                                            // would be redundant if depthConstant is already defined as 33fsw or 10msw (which are ~1 ATA)
                                                                            // The original formula implicitly uses 1 ATA for the constants 33 and 10.

        // Original formula: END = (Depth_absolute_pressure_ATA / Narcotic_Gas_Fraction_in_Air_equivalent) * Atm_Pressure_in_Depth_Units - Atm_Pressure_in_Depth_Units
        // Simplified: END = ( (Depth_gauge + Atm_Pressure_in_Depth_Units) * NarcoticFraction ) - Atm_Pressure_in_Depth_Units
        double end = (depth + depthConstant) * narcoticFraction - depthConstant;

        if (end < 0) {
            return 0; // END cannot be negative
        }

        return (int) Math.round(end);
    }
} 