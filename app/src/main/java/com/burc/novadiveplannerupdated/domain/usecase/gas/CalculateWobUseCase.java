package com.burc.novadiveplannerupdated.domain.usecase.gas;

import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;

import javax.inject.Inject;

public class CalculateWobUseCase {

    @Inject
    public CalculateWobUseCase() {
    }

    /**
     * Calculates the Work of Breathing (WOB) expressed as an equivalent depth in air.
     * Formula based on 039_END_WOB_HT_Formulas v1.7.md, page 6.
     *
     * @param gas The breathing gas.
     * @param depth The current depth in units specified by unitSystem.
     * @param unitSystem The unit system (METRIC or IMPERIAL).
     * @return The calculated WOB as an equivalent depth, rounded to the nearest integer. Returns null for invalid inputs.
     */
    public Integer execute(Gas gas, int depth, UnitSystem unitSystem) {
        if (gas == null || depth < 0) {
            return null; // WOB cannot be calculated for invalid inputs
        }

        double fo2 = gas.getFo2();
        double fHe = gas.getFhe();
        double fn2 = 1.0 - fo2 - fHe;

        if (fn2 < 0) {
            fn2 = 0; // Ensure fn2 is not negative
        }

        double depthUnitsPerAtm = (unitSystem == UnitSystem.METRIC) ?
                DomainDefaults.DEPTH_CONSTANT_METRIC :
                DomainDefaults.DEPTH_CONSTANT_IMPERIAL;

        // Calculate absolute pressure in ATA
        // Pabs = (Depth_gauge / DepthUnitsPerATA) + SurfacePressureInATA
        double pAmb = ((double) depth / depthUnitsPerAtm) + DomainDefaults.SURFACE_PRESSURE_ATA;

        // Calculate PPO2 in ATA
        double ppo2 = pAmb * fo2;

        // Apply WOB Formula:
        // WOB_depth = DepthUnitsPerAtm * { [ ( ( (FN2 + C_A) * Pamb) + (C_B * PPO2) ) / C_D ] – SurfacePressureInATA }
        double termFn2Component = (fn2 + DomainDefaults.WOB_EMPIRICAL_CONSTANT_A) * pAmb;
        double termPpo2Component = DomainDefaults.WOB_EMPIRICAL_CONSTANT_B * ppo2;

        double factorNumerator = termFn2Component + termPpo2Component;
        double calculatedFactor = factorNumerator / DomainDefaults.WOB_EMPIRICAL_DENOMINATOR;

        double wobEquivalentDepth = depthUnitsPerAtm * (calculatedFactor - DomainDefaults.SURFACE_PRESSURE_ATA);

        if (wobEquivalentDepth < 0) {
            return 0; // WOB equivalent depth cannot be negative
        }

        return (int) Math.round(wobEquivalentDepth);
    }
} 