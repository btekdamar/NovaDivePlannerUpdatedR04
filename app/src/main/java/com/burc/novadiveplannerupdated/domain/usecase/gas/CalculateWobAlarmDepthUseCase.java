package com.burc.novadiveplannerupdated.domain.usecase.gas;

import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;

import javax.inject.Inject;

public class CalculateWobAlarmDepthUseCase {

    private static final double EPSILON = 0.00001; // For floating point comparisons

    @Inject
    public CalculateWobAlarmDepthUseCase() {
    }

    /**
     * Calculates the depth at which a target Work of Breathing (WOB) value is reached.
     * The WOB value itself is an equivalent depth in air.
     *
     * @param gas The breathing gas.
     * @param unitSystem The unit system for calculations (METRIC or IMPERIAL).
     * @param targetWobDepth The target WOB value (expressed as an equivalent depth in air, in units of unitSystem).
     * @return The calculated actual depth at which the target WOB is reached, rounded to the nearest integer.
     *         Returns null if the target WOB is unachievable or calculation is ill-defined.
     *         Returns 0 if the calculated depth is negative.
     */
    public Integer execute(Gas gas, UnitSystem unitSystem, int targetWobDepth) {
        if (gas == null) {
            return null;
        }
        if (targetWobDepth < 0) targetWobDepth = 0;

        double fo2 = gas.getFo2();
        double fHe = gas.getFhe();
        double fn2 = 1.0 - fo2 - fHe;

        if (fn2 < -EPSILON) { fn2 = 0; }
        if (fn2 < 0 && fn2 > -EPSILON) fn2 = 0;

        double depthUnitsPerAtm = (unitSystem == UnitSystem.METRIC) ?
                DomainDefaults.DEPTH_CONSTANT_METRIC :
                DomainDefaults.DEPTH_CONSTANT_IMPERIAL;

        // Target WOB (expressed as equivalent depth) converted to an ATA-like factor for the formula inversion
        // TargetWobFactor = (TargetWOB_Depth / DepthUnitsPerAtm) + SurfacePressureInATA
        double targetWobFactor = ((double) targetWobDepth / depthUnitsPerAtm) + DomainDefaults.SURFACE_PRESSURE_ATA;

        // Denominator part of the P_amb_unknown formula:
        // FactorZ = (FN2 + WOB_EMPIRICAL_CONSTANT_A) + (WOB_EMPIRICAL_CONSTANT_B * FO2)
        double factorZ = (fn2 + DomainDefaults.WOB_EMPIRICAL_CONSTANT_A) + (DomainDefaults.WOB_EMPIRICAL_CONSTANT_B * fo2);

        if (Math.abs(factorZ) < EPSILON) {
            // If FactorZ is zero, P_amb_unknown is undefined or infinite, meaning target WOB might be unachievable.
            // This could happen with unusual gas mixes (e.g. very high He, low N2/O2 making the WOB response abnormal)
            // or if targetWobFactor implies an extreme condition.
            return null; 
        }

        // P_amb_unknown = (TargetWobFactor * WOB_EMPIRICAL_DENOMINATOR) / FactorZ
        double pAmbUnknown = (targetWobFactor * DomainDefaults.WOB_EMPIRICAL_DENOMINATOR) / factorZ;

        // If calculated P_amb_unknown is less than surface pressure, it implies the WOB target is met at or above surface.
        if (pAmbUnknown < DomainDefaults.SURFACE_PRESSURE_ATA) {
             // This could mean the gas is very light and meets the WOB target even at surface, or target WOB is very low.
             // For a "limit depth", 0 is appropriate.
            return 0;
        }
        
        // AlarmDepth_gauge = (P_amb_unknown - SurfacePressureInATA) * DepthUnitsPerAtm
        double alarmDepth = (pAmbUnknown - DomainDefaults.SURFACE_PRESSURE_ATA) * depthUnitsPerAtm;

        if (alarmDepth < 0) { // Should be caught by pAmbUnknown check, but for safety.
            return 0;
        }

        if (alarmDepth > 2000) { // Practical upper limit
            return null; // Unachievable beyond practical limits
        }

        return (int) Math.round(alarmDepth);
    }
} 