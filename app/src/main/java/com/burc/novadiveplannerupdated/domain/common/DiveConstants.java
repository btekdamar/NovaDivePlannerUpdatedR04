package com.burc.novadiveplannerupdated.domain.common;

/**
 * Contains constants for the Buhlmann ZHL-16c algorithm and other dive calculations.
 * All values are based on or converted to the Imperial system (feet, ft/min, fsw).
 */
public final class DiveConstants {

    private DiveConstants() {
        // Bu sınıfın örneği oluşturulamaz
    }

    // --- Buhlmann ZHL-16c Parameters (17 Compartments) ---

    // Nitrogen Half-Times (minutes) - for depth calculations
    public static final double[] N2_HALF_TIMES_MIN = {
            5.77, 7.22, 11.5, 18.0, 26.7, 39.0, 55.3, 78.4, 111.0, 157.0, 211.0,
            270.0, 345.0, 440.0, 563.0, 719.0, 916.0
    };

    // Nitrogen Half-Times on Surface (minutes) - for surface interval calculations
    // As per NewAlgorithmModel.java, may differ for faster tissues in some models.
    public static final double[] N2_SURFACE_HALF_TIMES_MIN = {
            86.6, 86.6, 86.6, 86.6, 86.6, 86.6, 86.6, 86.6, 111.0, 157.0, 211.0,
            270.0, 345.0, 440.0, 563.0, 719.0, 916.0
    };

    // Helium Half-Times (minutes) - for depth calculations
    public static final double[] HE_HALF_TIMES_MIN = {
            2.18, 2.71, 4.36, 6.81, 10.1, 14.7, 20.9, 29.6, 42.0, 59.5, 79.6,
            102.0, 130.0, 166.0, 213.0, 272.0, 346.0
    };

    // Helium Half-Times on Surface (minutes) - for surface interval calculations
    // As per NewAlgorithmModel.java, may differ for faster tissues in some models.
    public static final double[] HE_SURFACE_HALF_TIMES_MIN = {
            32.7, 32.7, 32.7, 32.7, 32.7, 32.7, 32.7, 32.7, 42.0, 59.5, 79.6,
            102.0, 130.0, 166.0, 213.0, 272.0, 346.0
    };

    // Nitrogen 'a' Coefficients (fsw - feet of sea water)
    // Values from NewAlgorithmModel.java, confirmed as fsw by Pelagic[...] doc.
    public static final double[] N2_A_COEFFICIENTS_FSW = {
            41.0, 38.1, 32.6, 28.1, 24.6, 20.2, 16.4, 14.4, 13.0, 11.0, 10.0,
            9.1, 8.2, 7.5, 6.8, 6.1, 5.6
    };

    // Helium 'a' Coefficients (fsw - feet of sea water)
    // Values from NewAlgorithmModel.java, confirmed as fsw by Pelagic[...] doc.
    public static final double[] HE_A_COEFFICIENTS_FSW = {
            56.7, 52.7, 45.0, 38.8, 34.1, 30.0, 26.7, 23.8, 21.2, 19.4, 18.1,
            17.4, 16.9, 16.9, 16.9, 16.8, 16.7
    };

    // Nitrogen 'b' Coefficients (dimensionless)
    // Values from NewAlgorithmModel.java.
    public static final double[] N2_B_COEFFICIENTS = {
            0.505, 0.558, 0.651, 0.722, 0.783, 0.813, 0.843, 0.869, 0.891,
            0.909, 0.922, 0.932, 0.94, 0.948, 0.954, 0.96, 0.965
    };

    // Helium 'b' Coefficients (dimensionless)
    // Values from NewAlgorithmModel.java.
    public static final double[] HE_B_COEFFICIENTS = {
            0.425, 0.477, 0.575, 0.653, 0.722, 0.758, 0.796, 0.828, 0.855,
            0.876, 0.89, 0.9, 0.907, 0.912, 0.917, 0.922, 0.927
    };

    public static final int NUMBER_OF_TISSUE_COMPARTMENTS = 17;

    // Schreiner equation constant: ln(2)
    // k = LN_2 / half_time_min
    public static final double LN_2 = Math.log(2); // Natural logarithm of 2

    // Standard atmospheric gas fractions and pressures
    public static final double FN2_IN_AIR = 0.7902; // Standard fraction of Nitrogen in Air
    public static final double FHE_IN_AIR = 0.0000; // Standard fraction of Helium in Air (negligible)
    public static final double FO2_IN_AIR = 0.2095; // Standard fraction of Oxygen in Air
    // Water Vapor Pressure at body temperature (approx 37°C / 98.6°F) is ~47 mmHg.
    // 47 mmHg * (33 fsw / 760 mmHg) = 2.03 fsw. Rounded to common usage for simplicity.
    // public static final double WATER_VAPOR_PRESSURE_FSW = 0.8; // Approximate water vapor pressure in fsw // Removed as per user request to align with Pelagic doc

} 