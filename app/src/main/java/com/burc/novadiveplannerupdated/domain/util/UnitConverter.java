package com.burc.novadiveplannerupdated.domain.util;

public final class UnitConverter {

    private UnitConverter() {
        // Utility class, no instantiation.
    }

    // --- Length Conversion Constants ---
    public static final double METERS_PER_FOOT = 0.3048;
    public static final double FEET_PER_METER = 1.0 / METERS_PER_FOOT;

    // --- Volume Flow Rate Conversion Constants (for RMV/SAC) ---
    public static final double LITERS_PER_CUFT = 28.316846592;
    public static final double CUFT_PER_LITER = 1.0 / LITERS_PER_CUFT;

    /**
     * Converts a depth from feet to meters and rounds to the nearest integer for display.
     * For example, 10ft -> 3m, 20ft -> 6m.
     * This is used for values like LastStopDepth or general depth display in Metric.
     *
     * @param feet The depth in feet.
     * @return The depth in meters, rounded to the nearest integer.
     */
    public static int convertFeetToMetersForDisplay(double feet) {
        return (int) Math.round(feet * METERS_PER_FOOT);
    }

    /**
     * Converts a value from feet to meters, rounds it to the nearest integer,
     * and then clamps it within the specified Metric display limits.
     * Used for settings like alarm thresholds when switching UI to Metric.
     *
     * @param feetValue The value in feet (double).
     * @param metricDisplayMin The minimum displayable value in meters for this setting.
     * @param metricDisplayMax The maximum displayable value in meters for this setting.
     * @return The processed value in meters, ready for display in Metric units.
     */
    public static int convertAndClampFeetToMetersForDisplay(double feetValue, int metricDisplayMin, int metricDisplayMax) {
        int metersRounded = (int) Math.round(feetValue * METERS_PER_FOOT);
        if (metersRounded < metricDisplayMin) {
            return metricDisplayMin;
        }
        if (metersRounded > metricDisplayMax) {
            return metricDisplayMax;
        }
        return metersRounded;
    }

    /**
     * Helper method to round a double value to the nearest multiple of a given integer.
     * @param value The double value to round.
     * @param multiple The integer multiple to round to. Must be positive.
     * @return The rounded integer value.
     * @throws IllegalArgumentException if multiple is not positive.
     */
    public static int roundToNearestMultiple(double value, int multiple) {
        if (multiple <= 0) {
            throw new IllegalArgumentException("Multiple must be positive. Was: " + multiple);
        }
        return (int) (Math.round(value / multiple) * multiple);
    }

    /**
     * Helper method to round a float value to the nearest multiple of a given integer.
     * @param value The float value to round.
     * @param multiple The integer multiple to round to. Must be positive.
     * @return The rounded integer value.
     * @throws IllegalArgumentException if multiple is not positive.
     */
    public static int roundToNearestMultiple(float value, int multiple) {
        if (multiple <= 0) {
            throw new IllegalArgumentException("Multiple must be positive. Was: " + multiple);
        }
        return (int) (Math.round(value / multiple) * multiple);
    }

    /**
     * Helper method to round a float value to the nearest multiple of a given float.
     * @param value The float value to round.
     * @param multiple The float multiple to round to. Must be positive.
     * @return The rounded float value.
     * @throws IllegalArgumentException if multiple is not positive.
     */
    public static float roundToNearestMultiple(float value, float multiple) {
        if (multiple <= 0f) {
            throw new IllegalArgumentException("Multiple must be positive. Was: " + multiple);
        }
        return Math.round(value / multiple) * multiple;
    }

    /**
     * Converts a value in meters (typically from user input in Metric system) to feet for storage,
     * rounding to the nearest specified Imperial step (e.g., 10ft).
     * This method does NOT clamp the value, as clamping should be handled by domain model constraints or UI display logic.
     *
     * @param metersValue The value in meters.
     * @param imperialStep The Imperial step to round the converted feet value to (e.g., 10 for 10ft increments). Must be positive.
     * @return The converted value in feet, rounded to the nearest imperialStep.
     * @throws IllegalArgumentException if imperialStep is not positive.
     */
    public static int convertMetersToFeetForStorage(int metersValue, int imperialStep) {
        if (imperialStep <= 0) {
            throw new IllegalArgumentException("Imperial step must be positive. Was: " + imperialStep);
        }
        double feetExact = metersValue * FEET_PER_METER;
        return roundToNearestMultiple(feetExact, imperialStep);
    }

    /**
     * Converts a value from meters to feet, rounds it to the nearest multiple (step),
     * and then clamps it within the specified Imperial display limits.
     * Used when switching the UI display from Metric to Imperial for settings like alarm thresholds.
     *
     * @param metersValue The value in meters (integer).
     * @param imperialDisplayMin The minimum displayable value in feet for this setting.
     * @param imperialDisplayMax The maximum displayable value in feet for this setting.
     * @param imperialStep The step or multiple to round to (e.g., 10 for rounding to nearest 10ft).
     * @return The processed value in feet, ready for display in Imperial units.
     */
    public static int convertMetersToFeetForImperialDisplay(int metersValue, int imperialDisplayMin, int imperialDisplayMax, int imperialStep) {
        if (imperialStep <= 0) {
            // As per previous discussion, step should be positive. 
            // Defaulting to simple rounding if step is invalid, or throw exception.
            // For now, let's assume valid step or handle it more gracefully if needed.
            throw new IllegalArgumentException("Imperial step must be positive.");
        }
        double feetExact = metersValue * FEET_PER_METER;
        int roundedToStepFt = roundToNearestMultiple(feetExact, imperialStep);

        // Clamp to imperial display limits
        if (roundedToStepFt < imperialDisplayMin) {
            return imperialDisplayMin;
        }
        if (roundedToStepFt > imperialDisplayMax) {
            return imperialDisplayMax;
        }
        return roundedToStepFt;
    }

    // --- Volume Conversions ---
    public static double convertLitersToCuFt(double liters) {
        return liters * CUFT_PER_LITER;
    }

    public static double convertCuFtToLiters(double cuft) {
        return cuft * LITERS_PER_CUFT;
    }

    // --- RMV/SAC Specific Conversions (float based for precision with typical RMV values) ---

    /**
     * Converts a Respiratory Minute Volume (RMV) or Surface Air Consumption (SAC)
     * from Liters per Minute (L/min) to Cubic Feet per Minute (cuft/min) for storage,
     * rounding to the nearest specified Imperial step (e.g., 0.05 cuft/min).
     * This method does NOT clamp the value.
     *
     * @param litersPerMinuteValue The value in L/min.
     * @param imperialStepCuFtMin The Imperial step to round the converted cuft/min value to (e.g., 0.05f). Must be positive.
     * @return The converted value in cuft/min, rounded to the nearest imperialStepCuFtMin.
     * @throws IllegalArgumentException if imperialStepCuFtMin is not positive.
     */
    public static float convertLitersPerMinuteToCuFtPerMinuteForStorage(float litersPerMinuteValue, float imperialStepCuFtMin) {
        if (imperialStepCuFtMin <= 0f) {
            throw new IllegalArgumentException("Imperial step for RMV must be positive. Was: " + imperialStepCuFtMin);
        }
        float cuftExact = (float) (litersPerMinuteValue * CUFT_PER_LITER);
        return roundToNearestMultiple(cuftExact, imperialStepCuFtMin);
    }

    /**
     * Converts RMV/SAC from cuft/min to L/min for display, rounds to nearest metric step, and clamps.
     */
    public static float convertAndClampCuFtToLitersForDisplay(double cuftValue, float metricDisplayMinLMin, float metricDisplayMaxLMin, float metricStepLMin) {
        if (metricStepLMin <= 0f) {
            throw new IllegalArgumentException("Metric step for RMV display must be positive. Was: " + metricStepLMin);
        }
        float litersExact = (float) (cuftValue * LITERS_PER_CUFT);
        float roundedLiters = roundToNearestMultiple(litersExact, metricStepLMin);
        return Math.max(metricDisplayMinLMin, Math.min(metricDisplayMaxLMin, roundedLiters));
    }

    /**
     * Converts RMV/SAC from L/min to cuft/min for display, rounds to nearest imperial step, and clamps.
     */
    public static float convertAndClampLitersToCuFtForDisplay(double litersValue, float imperialDisplayMinCuFtMin, float imperialDisplayMaxCuFtMin, float imperialStepCuFtMin) {
        if (imperialStepCuFtMin <= 0f) {
            throw new IllegalArgumentException("Imperial step for RMV display must be positive. Was: " + imperialStepCuFtMin);
        }
        float cuftExact = (float) (litersValue * CUFT_PER_LITER);
        float roundedCuFt = roundToNearestMultiple(cuftExact, imperialStepCuFtMin);
        return Math.max(imperialDisplayMinCuFtMin, Math.min(imperialDisplayMaxCuFtMin, roundedCuFt));
    }

    /**
     * Converts a value in meters (typically from user input in Metric system) to a precise
     * double value in feet for storage, without rounding to an imperial step.
     *
     * @param metersValue The value in meters.
     * @return The converted precise value in feet (double).
     */
    public static double convertMetersToPreciseFeet(int metersValue) {
        return (double) metersValue * FEET_PER_METER;
    }

    /**
     * Converts a Respiratory Minute Volume (RMV) or Surface Air Consumption (SAC)
     * from Liters per Minute (L/min) to a precise float value in Cubic Feet per Minute (cuft/min)
     * for storage, without rounding to an imperial step.
     *
     * @param litersPerMinuteValue The value in L/min.
     * @return The converted precise value in cuft/min (float).
     */
    public static float convertLitersPerMinuteToPreciseCuFt(float litersPerMinuteValue) {
        return (float) (litersPerMinuteValue * CUFT_PER_LITER);
    }
} 