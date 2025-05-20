package com.burc.novadiveplannerupdated.domain.service;

import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.model.GasType;

import java.util.Objects;

/**
 * Service class for calculating oxygen toxicity (OTU, CNS%) and related parameters.
 */
public class OxygenToxicityService {

    public OxygenToxicityService() {
        // Constructor
    }

    /**
     * Calculates the Partial Pressure of Oxygen (PPO2) for a given gas, depth, and settings.
     *
     * @param gas                          The breathing gas.
     * @param depthFsw                     Current depth in feet of sea water.
     * @param initialAmbientPressureFsw    Initial ambient pressure at surface/altitude in fsw (P_init).
     * @param setPointAta                  The PPO2 setpoint in ATA for Closed Circuit Rebreathers (CC).
     *                                     Should be null for Open Circuit (OC) dives.
     * @return The calculated PPO2 in atmospheres absolute (ata).
     * @throws IllegalArgumentException if gas is null.
     */
    public double calculatePpo2(Gas gas, double depthFsw, double initialAmbientPressureFsw, Double setPointAta) {
        Objects.requireNonNull(gas, "Gas cannot be null for PPO2 calculation.");
        if (depthFsw < 0) {
            // Or handle as an error, though P_init might be > 0 even if depth relative to surface is 0
            // For practical purposes, relative depth shouldn't be negative.
             // For calculation P_amb_abs = depthFsw + initialAmbientPressureFsw, if depthFsw is negative, it would mean above surface.
             // P_init already accounts for altitude. So depthFsw should be >= 0 from the water surface.
            // throw new IllegalArgumentException("Depth cannot be negative.");
        }
        if (initialAmbientPressureFsw <= 0) {
            throw new IllegalArgumentException("Initial ambient pressure must be positive.");
        }

        double pAmbientAbsoluteFsw = depthFsw + initialAmbientPressureFsw;
        double pAmbientAbsoluteAta = pAmbientAbsoluteFsw / DomainDefaults.DEPTH_CONSTANT_IMPERIAL;

        if (pAmbientAbsoluteAta < 0) { // Should not happen if inputs are sane
            pAmbientAbsoluteAta = 0;
        }

        if (gas.getGasType() == GasType.OPEN_CIRCUIT) {
            return gas.getFo2() * pAmbientAbsoluteAta;
        } else if (gas.getGasType() == GasType.CLOSED_CIRCUIT) {
            if (setPointAta == null) {
                // This is a problematic state for CC.
                // Fallback to diluent's FO2, but log a warning or throw an error based on strictness.
                // For now, let's assume setPointAta will be provided for CC segments as per UI logic.
                // If we must calculate, use diluent FO2.
                // System.err.println("Warning: Setpoint is null for a Closed Circuit gas during PPO2 calculation. Using diluent FO2.");
                // return gas.getFo2() * pAmbientAbsoluteAta;
                // Requirements state setPoint is mandatory for CC segment.
                throw new IllegalArgumentException("SetPoint (setPointAta) cannot be null for a Closed Circuit gas during PPO2 calculation.");
            }
            if (setPointAta < DomainDefaults.MIN_SET_POINT || setPointAta > DomainDefaults.MAX_SET_POINT) {
                 throw new IllegalArgumentException("SetPoint for CC gas (" + setPointAta + ") is out of valid range (" +
                            DomainDefaults.MIN_SET_POINT + "-" + DomainDefaults.MAX_SET_POINT + ").");
            }
            // For a CCR, the PPO2 is the setpoint, but it cannot exceed the ambient pressure
            // (i.e., you can't have a PPO2 of 1.3 if ambient pressure is only 1.0 ata).
            // If ambient pressure is very low, PPO2 will be limited by breathing 100% O2 at that ambient pressure.
            return Math.min(setPointAta, pAmbientAbsoluteAta);
        } else {
            // Should not happen if GasType is a well-defined enum
            throw new IllegalArgumentException("Unknown gas type: " + gas.getGasType());
        }
    }

    /**
     * Calculates the Rate of Daily Oxygen Toxicity Unit (OTUD) accumulation (ROTD).
     * Based on the formula from Pelagic Trimix Dive Computer Algorithms document (pg. 19)
     * and reference implementation in BuhlmannModel.vb.
     *
     * @param ppo2Ata The current Partial Pressure of Oxygen in atmospheres absolute (ata).
     * @return The rate of OTUD accumulation in OTUs per minute.
     */
    public double calculateRotd(double ppo2Ata) {
        if (ppo2Ata < 0) {
            // PPO2 should not be negative. Handling this defensively.
            // Depending on strictness, could throw IllegalArgumentException or log warning.
            // For now, treat as if PPO2 is 0 for ROTD calculation if negative.
            ppo2Ata = 0;
        }

        if (ppo2Ata < 0.5) {
            return 0.0;
        } else {
            // ROTD = -0.17 + 0.82 * PPO2 + 0.35 * PPO2^2
            return -0.17 + (0.82 * ppo2Ata) + (0.35 * ppo2Ata * ppo2Ata);
        }
    }

    /**
     * Calculates the Rate of Single Dive Oxygen Toxicity Unit (OTUS) accumulation (ROTS).
     * Based on the formula from Pelagic Trimix Dive Computer Algorithms document (pg. 19)
     * and reference implementation in BuhlmannModel.vb.
     *
     * @param ppo2Ata The current Partial Pressure of Oxygen in atmospheres absolute (ata).
     * @param rotd    The pre-calculated Rate of Daily OTU accumulation for the same ppo2Ata.
     * @return The rate of OTUS accumulation in OTUs per minute.
     */
    public double calculateRots(double ppo2Ata, double rotd) {
        if (ppo2Ata < 0) {
            // PPO2 should not be negative. Handling this defensively.
            // Treat as if PPO2 is 0 for ROTS calculation if negative.
            // This scenario should ideally be caught by calculateRotd as well.
            ppo2Ata = 0;
        }

        if (ppo2Ata <= 1.0) {
            return rotd;
        } else if (ppo2Ata < 1.13) { // Corresponds to Pelagic: 1.00 < PPO2 <= 1.13 (using < for exclusive upper bound)
            return (2.5 * ppo2Ata) - 1.5;
        } else if (ppo2Ata < 1.5) {  // Corresponds to Pelagic: 1.13 < PPO2 <= 1.50 (using < for exclusive upper bound)
                                     // VB code also uses po2 < 1.5 for this condition.
            return 4.56 - (7.2 * ppo2Ata) + (3.84 * ppo2Ata * ppo2Ata);
        } else { // PPO2 >= 1.5
            return (41.7 * ppo2Ata) - 60.0;
        }
    }

    /**
     * Calculates the rate of CNS (Central Nervous System) oxygen toxicity accumulation.
     * The calculation is based on NOAA limits and uses linear interpolation between PPO2 points
     * as defined in the Pelagic Trimix Dive Computer Algorithms document (pg. 20)
     * and reference implementation in BuhlmannModel.vb (CalculateCNSPercentPerMin).
     *
     * @param ppo2Ata The current Partial Pressure of Oxygen in atmospheres absolute (ata).
     * @return The rate of CNS toxicity accumulation in % per minute.
     */
    public double calculateCnsToxicityRate(double ppo2Ata) {
        if (ppo2Ata < 0) {
            // PPO2 should not be negative.
            ppo2Ata = 0;
        }

        // Based on NOAA CNS % per minute table (100 / limit_in_minutes)
        // PPO2 (ata) | Limit (min) | %/min
        //------------------------------------
        // 0.5        | --          | 0.0
        // 0.6        | 720         | 0.1389
        // 0.7        | 570         | 0.1754
        // 0.8        | 450         | 0.2222
        // 0.9        | 360         | 0.2778
        // 1.0        | 300         | 0.3333
        // 1.1        | 240         | 0.4167
        // 1.2        | 210         | 0.4762
        // 1.3        | 180         | 0.5556
        // 1.4        | 150         | 0.6667
        // 1.5        | 120         | 0.8333
        // 1.6        | 45          | 2.2222

        if (ppo2Ata <= 0.5) return 0.0;
        if (ppo2Ata <= 0.6) return interpolate(ppo2Ata, 0.5, 0.6, 0.0, 0.1388888888888889); // 100.0/720.0
        if (ppo2Ata <= 0.7) return interpolate(ppo2Ata, 0.6, 0.7, 0.1388888888888889, 0.17543859649122806); // 100.0/570.0
        if (ppo2Ata <= 0.8) return interpolate(ppo2Ata, 0.7, 0.8, 0.17543859649122806, 0.2222222222222222);  // 100.0/450.0
        if (ppo2Ata <= 0.9) return interpolate(ppo2Ata, 0.8, 0.9, 0.2222222222222222, 0.2777777777777778);  // 100.0/360.0
        if (ppo2Ata <= 1.0) return interpolate(ppo2Ata, 0.9, 1.0, 0.2777777777777778, 0.3333333333333333);  // 100.0/300.0
        if (ppo2Ata <= 1.1) return interpolate(ppo2Ata, 1.0, 1.1, 0.3333333333333333, 0.4166666666666667);  // 100.0/240.0
        if (ppo2Ata <= 1.2) return interpolate(ppo2Ata, 1.1, 1.2, 0.4166666666666667, 0.47619047619047616); // 100.0/210.0
        if (ppo2Ata <= 1.3) return interpolate(ppo2Ata, 1.2, 1.3, 0.47619047619047616, 0.5555555555555556); // 100.0/180.0
        if (ppo2Ata <= 1.4) return interpolate(ppo2Ata, 1.3, 1.4, 0.5555555555555556, 0.6666666666666666); // 100.0/150.0
        if (ppo2Ata <= 1.5) return interpolate(ppo2Ata, 1.4, 1.5, 0.6666666666666666, 0.8333333333333334); // 100.0/120.0
        if (ppo2Ata <= 1.6) return interpolate(ppo2Ata, 1.5, 1.6, 0.8333333333333334, 2.2222222222222223); // 100.0/45.0
        // For PPO2 > 1.6, the rate is the same as at 1.6 (or could be considered off-chart/dangerous)
        // The reference VB code continues to use the 1.5-1.6 slope.
        // Pelagic document pg 20 states "Above 1.6 PO2, the limit is 45 minutes", implying the rate for 1.6 applies.
        return 2.2222222222222223; // Rate at 1.6 ata
    }

    /**
     * Helper method for linear interpolation.
     * Calculates y for a given x, based on two points (x0, y0) and (x1, y1).
     * y = y0 + (x - x0) * (y1 - y0) / (x1 - x0)
     */
    private double interpolate(double x, double x0, double x1, double y0, double y1) {
        if (x1 == x0) { // Avoid division by zero, should not happen with distinct PPO2 points
            return y0; // Or y1, they should be the same if x0 == x1 implies y0 == y1
        }
        return y0 + (x - x0) * (y1 - y0) / (x1 - x0);
    }

    /**
     * Constant for the half-life of Single Dive Oxygen Toxicity Units (OTUS) during a surface interval.
     * Value is 90 minutes as per Pelagic Trimix Dive Computer Algorithms document (pg. 21).
     */
    public static final double OTUS_HALF_LIFE_MINUTES = 90.0;

    /**
     * Calculates the decay of Single Dive Oxygen Toxicity Units (OTUS) during a surface interval.
     * OTUS decays with a half-life of 90 minutes. OTUD (Daily OTU) does not decay.
     * Based on Pelagic Trimix Dive Computer Algorithms document (pg. 21)
     * and reference implementation in BuhlmannModel.vb (CalculateOTUDecay).
     *
     * @param currentOtus The OTUS value at the beginning of the surface interval. Must be non-negative.
     * @param surfaceIntervalMinutes The duration of the surface interval in minutes. Must be non-negative.
     * @return The remaining OTUS value after the surface interval.
     */
    public double calculateOtusDecay(double currentOtus, double surfaceIntervalMinutes) {
        if (currentOtus < 0) {
            // OTUS should not be negative. Treat as 0 if it is.
            currentOtus = 0;
        }
        if (surfaceIntervalMinutes < 0) {
            // Surface interval cannot be negative. No decay if it's zero or negative.
            surfaceIntervalMinutes = 0;
        }

        if (currentOtus == 0 || surfaceIntervalMinutes == 0) {
            return currentOtus;
        }

        // Decay formula: NewOTUS = OldOTUS * (0.5 ^ (IntervalTime / HalfLife))
        double decayFactor = Math.pow(0.5, surfaceIntervalMinutes / OTUS_HALF_LIFE_MINUTES);
        return currentOtus * decayFactor;
    }

    /**
     * Calculates the Oxygen Time Remaining (OTR) based on the current PPO2 and CNS% accumulation.
     * This indicates how much longer one can stay at the current PPO2 before reaching the CNS% limit.
     * The CNS% limit is taken from {@link DomainDefaults#DEFAULT_CNS_LIMIT_PERCENT}.
     *
     * @param ppo2Ata             The current Partial Pressure of Oxygen in atmospheres absolute (ata).
     * @param currentCnsPercent   The current accumulated CNS toxicity percentage.
     * @return The estimated time remaining in minutes before CNS% limit is reached.
     *         Returns {@link Double#POSITIVE_INFINITY} if the CNS accumulation rate is zero or negative.
     *         Returns 0.0 if the CNS% limit is already reached or exceeded.
     */
    public double calculateOxygenTimeRemaining(double ppo2Ata, double currentCnsPercent) {
        if (currentCnsPercent < 0) {
            // Defensive: CNS percent should not be negative.
            currentCnsPercent = 0;
        }

        double cnsLimitPercent = DomainDefaults.DEFAULT_CNS_LIMIT_PERCENT;

        if (currentCnsPercent >= cnsLimitPercent) {
            return 0.0; // Limit already reached or exceeded
        }

        double cnsRatePerMinute = calculateCnsToxicityRate(ppo2Ata);

        if (cnsRatePerMinute <= 0) {
            // If rate is zero (e.g., PPO2 <= 0.5) or negative (shouldn't happen with valid PPO2),
            // time remaining is effectively infinite as CNS% isn't increasing.
            return Double.POSITIVE_INFINITY;
        }

        double remainingCnsPercent = cnsLimitPercent - currentCnsPercent;
        return remainingCnsPercent / cnsRatePerMinute;
    }

    // --- Other methods will be added below ---

} 