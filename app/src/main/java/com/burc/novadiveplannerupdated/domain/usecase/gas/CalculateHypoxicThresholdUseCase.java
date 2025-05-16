package com.burc.novadiveplannerupdated.domain.usecase.gas;

import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;

import javax.inject.Inject;

public class CalculateHypoxicThresholdUseCase {

    // private static final double MIN_SAFE_PPO2_ATA = 0.18;
    // private static final double DEPTH_FACTOR_METRIC = 10.0; // meters per ATA relative to surface pressure
    // private static final double DEPTH_FACTOR_IMPERIAL = 33.0; // feet per ATA relative to surface pressure
    // private static final double SURFACE_PRESSURE_ATA = 1.0;

    @Inject
    public CalculateHypoxicThresholdUseCase() {
    }

    /**
     * Calculates the Hypoxic Threshold (HT) for a given gas.
     * The HT is the shallowest depth (in specified units) at which the gas provides a safe minimum PPO2.
     * If the gas is safe at the surface, HT is 0.
     * If the gas never reaches the minimum safe PPO2 (e.g., FO2 is 0), returns Integer.MAX_VALUE.
     *
     * @param gas The gas for which to calculate the HT.
     * @param unitSystem The unit system (METRIC or IMPERIAL) for the output depth.
     * @return The hypoxic threshold depth, rounded up to the nearest integer. Returns 0 if safe at surface,
     *         or Integer.MAX_VALUE if always hypoxic.
     */
    public Integer execute(Gas gas, UnitSystem unitSystem) {
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
        
        // If for some reason calculated depth is negative, it implies safe at surface (already handled)
        // but as a safeguard, ensure non-negative result if we didn't return 0 earlier.
        if (hypoxicThresholdDepth < 0) return 0;

        return (int) Math.ceil(hypoxicThresholdDepth);
    }
} 