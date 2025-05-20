package com.burc.novadiveplannerupdated.domain.service;

import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;

import javax.inject.Inject;

public class GasCalculationService {

    private static final double EPSILON = 0.00001; // For floating point comparisons to avoid division by zero

    @Inject
    public GasCalculationService() {
    }

    /**
     * Calculates the Maximum Operating Depth (MOD) for a given gas.
     *
     * @param gas        The gas for which to calculate MOD.
     * @param unitSystem The unit system (METRIC or IMPERIAL) for the output depth.
     * @return The MOD in feet or meters, rounded down to the nearest integer.
     * Returns null if MOD cannot be calculated (e.g., FO2 is 0 or PO2Max is not set).
     */
    public Integer calculateMod(Gas gas, UnitSystem unitSystem) {
        if (gas == null || gas.getFo2() <= 0 || gas.getPo2Max() == null || gas.getPo2Max() <= 0) {
            return null; // MOD cannot be calculated
        }

        double fo2 = gas.getFo2();
        double po2Max = gas.getPo2Max();

        // P_amb_at_MOD (ATA) = PPO2Max / FO2
        // Depth_in_atm_gauge = P_amb_at_MOD - 1 (assuming surface pressure is 1 ATA)
        // MOD = Depth_in_atm_gauge * ConversionFactorToDepthUnits
        double modCalculation = ((po2Max / fo2) - DomainDefaults.SURFACE_PRESSURE_ATA);

        if (modCalculation < 0) {
            // This can happen if po2Max < fo2 (e.g. PPO2Max = 0.21, FO2 = 1.0 for pure O2)
            // In such cases, the gas is breathable at the surface, and the MOD relative to diving is effectively 0.
            return 0;
        }

        double result;
        if (unitSystem == UnitSystem.METRIC) {
            result = modCalculation * DomainDefaults.DEPTH_CONSTANT_METRIC;
        } else {
            result = modCalculation * DomainDefaults.DEPTH_CONSTANT_IMPERIAL;
        }
        return (int) Math.floor(result);
    }

    /**
     * Calculates the Equivalent Narcotic Depth (END).
     *
     * @param gas              The breathing gas.
     * @param depth            The current depth in units specified by unitSystem.
     * @param unitSystem       The unit system (METRIC or IMPERIAL).
     * @param isOxygenNarcotic True if oxygen should be considered narcotic, false otherwise.
     * @return The calculated END, rounded to the nearest integer. Returns null for invalid inputs.
     */
    public Integer calculateEnd(Gas gas, int depth, UnitSystem unitSystem, boolean isOxygenNarcotic) {
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

        // Simplified: END = ( (Depth_gauge + Atm_Pressure_in_Depth_Units) * NarcoticFraction ) - Atm_Pressure_in_Depth_Units
        double end = (depth + depthConstant) * narcoticFraction - depthConstant;

        if (end < 0) {
            return 0; // END cannot be negative
        }

        return (int) Math.round(end);
    }

    /**
     * Calculates the depth at which a target Equivalent Narcotic Depth (END) is reached.
     *
     * @param gas              The breathing gas.
     * @param unitSystem       The unit system (METRIC or IMPERIAL).
     * @param isOxygenNarcotic True if oxygen should be considered narcotic.
     * @param targetEndDepth   The target END value (in depth units corresponding to unitSystem).
     * @return The calculated depth at which the target END is reached, rounded to the nearest integer.
     * Returns null if the target END is unachievable with the given gas.
     * Returns 0 if the calculated depth is negative.
     */
    public Integer calculateEndAlarmDepth(Gas gas, UnitSystem unitSystem, boolean isOxygenNarcotic, int targetEndDepth) {
        if (gas == null) {
            return null; // Or handle as an error
        }
        if (targetEndDepth < 0) targetEndDepth = 0; // Target END cannot be negative

        double fo2 = gas.getFo2();
        double fHe = gas.getFhe();
        double fn2 = 1.0 - fo2 - fHe;

        if (fn2 < -EPSILON) { // Allow for slight positive if fo2+fhe is a bit less than 1
            fn2 = 0;
        }
        if (fn2 < 0 && fn2 > -EPSILON) fn2 = 0; // Clamp small negatives from precision issues to 0

        double narcoticFraction;
        if (isOxygenNarcotic) {
            narcoticFraction = fn2 + fo2; // Equivalent to 1.0 - fHe
        } else {
            narcoticFraction = fn2;
        }

        if (Math.abs(narcoticFraction) < EPSILON) {
            return null;
        }

        double depthConstant = (unitSystem == UnitSystem.METRIC) ?
                DomainDefaults.DEPTH_CONSTANT_METRIC :
                DomainDefaults.DEPTH_CONSTANT_IMPERIAL;

        // Depth_alarm = ((TargetEND_depth + DepthConstant) / NarcoticFraction) - DepthConstant
        double alarmDepth = (((double) targetEndDepth + depthConstant) / narcoticFraction) - depthConstant;

        if (alarmDepth < 0) {
            return 0;
        }

        if (alarmDepth > 2000) {
            return null;
        }

        return (int) Math.round(alarmDepth);
    }

    /**
     * Calculates the Hypoxic Threshold (HT) for a given gas.
     * The HT is the shallowest depth (in specified units) at which the gas provides a safe minimum PPO2.
     * If the gas is safe at the surface, HT is 0.
     * If the gas never reaches the minimum safe PPO2 (e.g., FO2 is 0), returns null.
     *
     * @param gas        The gas for which to calculate the HT.
     * @param unitSystem The unit system (METRIC or IMPERIAL) for the output depth.
     * @return The hypoxic threshold depth, rounded up to the nearest integer. Returns 0 if safe at surface,
     * or null if always hypoxic or FO2 is zero.
     */
    public Integer calculateHypoxicThreshold(Gas gas, UnitSystem unitSystem) {
        if (gas == null || gas.getFo2() <= 0) {
            return null; // HT cannot be calculated or gas is always hypoxic
        }

        double fo2 = gas.getFo2();

        // Check if the gas is already safe (not hypoxic) at surface pressure (1 ATA)
        if ((fo2 * DomainDefaults.SURFACE_PRESSURE_ATA) >= DomainDefaults.MIN_SAFE_PPO2_ATA) {
            return 0; // Not hypoxic at surface or any depth below it
        }

        // Gas is hypoxic at the surface, calculate the depth at which PPO2 reaches MIN_SAFE_PPO2_ATA
        // TargetAmbientPressureATA = MIN_SAFE_PPO2_ATA / fo2
        // DepthInAtmospheresGauge = TargetAmbientPressureATA - SURFACE_PRESSURE_ATA
        double targetAmbientPressureATA = DomainDefaults.MIN_SAFE_PPO2_ATA / fo2;
        double depthInAtmospheresGauge = targetAmbientPressureATA - DomainDefaults.SURFACE_PRESSURE_ATA;

        double hypoxicThresholdDepth;
        if (unitSystem == UnitSystem.METRIC) {
            hypoxicThresholdDepth = depthInAtmospheresGauge * DomainDefaults.DEPTH_CONSTANT_METRIC;
        } else {
            hypoxicThresholdDepth = depthInAtmospheresGauge * DomainDefaults.DEPTH_CONSTANT_IMPERIAL;
        }

        if (hypoxicThresholdDepth < 0) return 0;

        return (int) Math.ceil(hypoxicThresholdDepth);
    }

    /**
     * Calculates the Work of Breathing (WOB) expressed as an equivalent depth in air.
     * Formula based on 039_END_WOB_HT_Formulas v1.7.md, page 6.
     *
     * @param gas        The breathing gas.
     * @param depth      The current depth in units specified by unitSystem.
     * @param unitSystem The unit system (METRIC or IMPERIAL).
     * @return The calculated WOB as an equivalent depth, rounded to the nearest integer. Returns null for invalid inputs.
     */
    public Integer calculateWob(Gas gas, int depth, UnitSystem unitSystem) {
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

    /**
     * Calculates the depth at which a target Work of Breathing (WOB) value is reached.
     * The WOB value itself is an equivalent depth in air.
     *
     * @param gas            The breathing gas.
     * @param unitSystem     The unit system for calculations (METRIC or IMPERIAL).
     * @param targetWobDepth The target WOB value (expressed as an equivalent depth in air, in units of unitSystem).
     * @return The calculated actual depth at which the target WOB is reached, rounded to the nearest integer.
     * Returns null if the target WOB is unachievable or calculation is ill-defined.
     * Returns 0 if the calculated depth is negative.
     */
    public Integer calculateWobAlarmDepth(Gas gas, UnitSystem unitSystem, int targetWobDepth) {
        if (gas == null) {
            return null;
        }
        if (targetWobDepth < 0) targetWobDepth = 0;

        double fo2 = gas.getFo2();
        double fHe = gas.getFhe();
        double fn2 = 1.0 - fo2 - fHe;

        // fn2 < -EPSILON durumu fn2'nin gerçekten negatif olduğunu gösterir, bu durumda 0'a çekilir.
        // fn2 < 0 && fn2 > -EPSILON durumu ise fn2'nin çok küçük bir negatif sayı olduğunu (muhtemelen float hassasiyet sorunu)
        // gösterir, bu da 0'a çekilir.
        if (fn2 < -EPSILON) {
            fn2 = 0;
        } else if (fn2 < 0) {
            fn2 = 0;
        } // Basitleştirilmiş: fn2 < 0 ise fn2 = 0 yap

        double depthUnitsPerAtm = (unitSystem == UnitSystem.METRIC) ?
                DomainDefaults.DEPTH_CONSTANT_METRIC :
                DomainDefaults.DEPTH_CONSTANT_IMPERIAL;

        double targetWobFactor = ((double) targetWobDepth / depthUnitsPerAtm) + DomainDefaults.SURFACE_PRESSURE_ATA;

        double factorZ = (fn2 + DomainDefaults.WOB_EMPIRICAL_CONSTANT_A) + (DomainDefaults.WOB_EMPIRICAL_CONSTANT_B * fo2);

        if (Math.abs(factorZ) < EPSILON) {
            return null;
        }

        double pAmbUnknown = (targetWobFactor * DomainDefaults.WOB_EMPIRICAL_DENOMINATOR) / factorZ;

        if (pAmbUnknown < DomainDefaults.SURFACE_PRESSURE_ATA) {
            return 0;
        }

        double alarmDepth = (pAmbUnknown - DomainDefaults.SURFACE_PRESSURE_ATA) * depthUnitsPerAtm;

        if (alarmDepth < 0) {
            return 0;
        }

        if (alarmDepth > 2000) {
            return null;
        }

        return (int) Math.round(alarmDepth);
    }

    // Diğer gaz hesaplama metotları buraya eklenecek
} 