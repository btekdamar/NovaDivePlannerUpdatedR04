package com.burc.novadiveplannerupdated.domain.service;

import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;
import com.burc.novadiveplannerupdated.domain.entity.Gas;

import java.util.Objects;

/**
 * Service class for calculating gas consumption related parameters.
 */
public class GasConsumptionService {

    public GasConsumptionService() {
        // Constructor
    }

    /**
     * Calculates the volumetric gas consumption rate at a specific depth.
     *
     * @param surfaceConsumptionRateCuftPerMin The diver's surface air consumption (SAC) or respiratory minute volume (RMV) in cubic feet per minute.
     * @param depthFsw The current depth in feet of seawater.
     * @param initialAmbientPressureFsw The initial ambient pressure at the surface/altitude in feet of seawater (e.g., from DiveSettings or DiveConstants).
     * @return The gas consumption rate at the given depth in cubic feet per minute.
     *         Returns 0 if initialAmbientPressureFsw is zero or negative, or if surfaceConsumptionRateCuftPerMin is negative.
     */
    public double calculateGasConsumptionRateAtDepthCuftPerMinute(
            double surfaceConsumptionRateCuftPerMin,
            double depthFsw,
            double initialAmbientPressureFsw) {

        if (initialAmbientPressureFsw <= 0) {
            // Cannot calculate if initial ambient pressure is not positive.
            // This would also lead to division by zero.
            return 0.0;
        }

        if (surfaceConsumptionRateCuftPerMin < 0) {
            // Consumption rate cannot be negative.
            surfaceConsumptionRateCuftPerMin = 0;
        }

        if (depthFsw < 0) {
            // Depth should not be negative for this calculation.
            depthFsw = 0;
        }

        // Absolute pressure at depth in atmospheres equivalent based on FSW
        // P_abs = (D_fsw + P_init_fsw)
        // We want the ratio of absolute pressure at depth to absolute pressure at surface.
        // Ratio = (depth_fsw + P_init_fsw) / P_init_fsw
        double pressureRatio = (depthFsw + initialAmbientPressureFsw) / initialAmbientPressureFsw;

        return surfaceConsumptionRateCuftPerMin * pressureRatio;
    }

    /**
     * Calculates the total volume of gas consumed over a duration at a specific average depth.
     *
     * @param surfaceConsumptionRateCuftPerMin The diver's surface air consumption (SAC) or respiratory minute volume (RMV) in cubic feet per minute.
     * @param averageDepthFsw The average depth during the specified duration in feet of seawater.
     * @param durationMinutes The duration of time spent at the average depth in minutes.
     * @param initialAmbientPressureFsw The initial ambient pressure at the surface/altitude in feet of seawater.
     * @return The total gas consumed in cubic feet. Returns 0 if durationMinutes is negative.
     */
    public double calculateTotalGasConsumedCuft(
            double surfaceConsumptionRateCuftPerMin,
            double averageDepthFsw,
            double durationMinutes,
            double initialAmbientPressureFsw) {

        if (durationMinutes < 0) {
            // Duration cannot be negative.
            durationMinutes = 0;
        }

        if (durationMinutes == 0) {
            return 0.0;
        }

        double consumptionRateAtDepthCuftPerMin = calculateGasConsumptionRateAtDepthCuftPerMinute(
                surfaceConsumptionRateCuftPerMin,
                averageDepthFsw,
                initialAmbientPressureFsw
        );

        return consumptionRateAtDepthCuftPerMin * durationMinutes;
    }

    /**
     * Calculates the Gas Time Remaining (GTR) for a given gas, tank pressure, consumption rate, and depth.
     *
     * @param gas                               The Gas object, containing tank capacity and reserve percentage.
     * @param currentTankPressurePsi            The current pressure in the tank in PSI.
     * @param surfaceConsumptionRateCuftPerMin  The diver's surface consumption rate in cubic feet per minute.
     * @param currentDepthFsw                   The current depth in feet of seawater.
     * @param initialAmbientPressureFsw         The initial ambient pressure at the surface/altitude in feet of seawater.
     * @return The estimated gas time remaining in minutes. Returns 0 if no usable gas or {@link Double#POSITIVE_INFINITY} if consumption is zero.
     * @throws NullPointerException if gas is null.
     */
    public double calculateGasTimeRemainingMinutes(
            Gas gas,
            double currentTankPressurePsi,
            double surfaceConsumptionRateCuftPerMin,
            double currentDepthFsw,
            double initialAmbientPressureFsw) {

        Objects.requireNonNull(gas, "Gas object cannot be null for GTR calculation.");

        if (currentTankPressurePsi < 0) {
            currentTankPressurePsi = 0; // Pressure cannot be negative
        }

        double tankServicePressurePsi = DomainDefaults.DEFAULT_TANK_SERVICE_PRESSURE_PSI;
        if (tankServicePressurePsi <= 0) {
            return 0.0; // Cannot calculate GTR without a valid service pressure
        }

        if (gas.getTankCapacity() <= 0) {
            return 0.0; // No gas to consume if tank capacity is zero or negative
        }

        double reservePressurePsi = (gas.getReservePressurePercentage() / 100.0) * tankServicePressurePsi;
        double usablePressurePsi = currentTankPressurePsi - reservePressurePsi;

        if (usablePressurePsi <= 0) {
            return 0.0; // No usable gas above reserve pressure
        }

        // Calculate volume per PSI based on nominal capacity and service pressure
        double volumePerPsi = gas.getTankCapacity() / tankServicePressurePsi;
        double usableGasVolumeCuft = usablePressurePsi * volumePerPsi;

        double consumptionRateAtDepthCuftPerMin = calculateGasConsumptionRateAtDepthCuftPerMinute(
                surfaceConsumptionRateCuftPerMin,
                currentDepthFsw,
                initialAmbientPressureFsw
        );

        if (consumptionRateAtDepthCuftPerMin <= 0) {
            // If consumption rate is zero or negative (e.g., SCR is 0, or at surface with no depth component if P_init = 0 which is guarded),
            // then GTR is effectively infinite, provided there's usable gas.
            return Double.POSITIVE_INFINITY;
        }

        return usableGasVolumeCuft / consumptionRateAtDepthCuftPerMin;
    }

    // Methods will be added here one by one.

} 