package com.burc.novadiveplannerupdated.domain.model;

public enum AltitudeLevel {
    SEA_LEVEL(0, 3000, "Sea Level (0-3000 ft)", 33.0), // Standard Sea Level P_init
    LEVEL_2(3001, 5000, "Altitude (3001-5000 ft)", 28.8), // Approx for 4000 ft
    LEVEL_3(5001, 7000, "Altitude (5001-7000 ft)", 26.2), // Approx for 6000 ft
    LEVEL_4(7001, 9000, "Altitude (7001-9000 ft)", 23.8), // Approx for 8000 ft
    LEVEL_5(9001, 11000, "Altitude (9001-11000 ft)", 21.6), // Approx for 10000 ft
    LEVEL_6(11001, 13000, "Altitude (11001-13000 ft)", 19.6), // Approx for 12000 ft
    LEVEL_7(13001, Integer.MAX_VALUE, "Altitude (>13000 ft)", 17.8); // Approx for 14000 ft

    private final int minAltitudeFt;
    private final int maxAltitudeFt;
    private final String imperialDisplayName;
    private final double initialAmbientPressureFsw; // P_init in fsw

    AltitudeLevel(int minAltitudeFt, int maxAltitudeFt, String imperialDisplayName, double initialAmbientPressureFsw) {
        this.minAltitudeFt = minAltitudeFt;
        this.maxAltitudeFt = maxAltitudeFt;
        this.imperialDisplayName = imperialDisplayName;
        this.initialAmbientPressureFsw = initialAmbientPressureFsw;
    }

    public int getMinAltitudeFt() {
        return minAltitudeFt;
    }

    public int getMaxAltitudeFt() {
        return maxAltitudeFt;
    }

    public String getImperialDisplayName() {
        return imperialDisplayName;
    }

    public double getInitialAmbientPressureFsw() {
        return initialAmbientPressureFsw;
    }

    /**
     * Returns the display name for the altitude level based on the selected unit system.
     * For IMPERIAL, it returns the predefined Imperial string.
     * For METRIC, it returns predefined, user-friendly metric range strings.
     *
     * @param system The unit system to use for the display string.
     * @return The formatted display string for the UI.
     */
    public String getDisplayString(UnitSystem system) {
        if (system == UnitSystem.METRIC) {
            switch (this) {
                case SEA_LEVEL: return "Sea Level (0-915 m)";
                case LEVEL_2:   return "Altitude (916-1525 m)";
                case LEVEL_3:   return "Altitude (1526-2135 m)";
                case LEVEL_4:   return "Altitude (2136-2745 m)";
                case LEVEL_5:   return "Altitude (2746-3355 m)";
                case LEVEL_6:   return "Altitude (3356-3965 m)";
                case LEVEL_7:   return "Altitude (>3965 m)";
                default:
                    // Should not happen for a known enum, fallback to imperial.
                    return imperialDisplayName;
            }
        }
        return imperialDisplayName; // Default or Imperial
    }

    /**
     * Returns the display name for use in UI components.
     */
    @Override
    public String toString() {
        return imperialDisplayName;
    }

    /**
     * Finds the AltitudeLevel that corresponds to a given altitude in feet.
     * Defaults to SEA_LEVEL if no specific range matches (e.g., for negative altitudes, though unlikely).
     *
     * @param currentAltitudeFt The current altitude in feet.
     * @return The corresponding AltitudeLevel.
     */
    public static AltitudeLevel fromAltitudeFt(int currentAltitudeFt) {
        for (AltitudeLevel level : values()) {
            if (currentAltitudeFt >= level.getMinAltitudeFt() && currentAltitudeFt <= level.getMaxAltitudeFt()) {
                return level;
            }
        }
        // Default to SEA_LEVEL if outside defined positive ranges or for safety.
        // Or throw an IllegalArgumentException if strict matching is required.
        return SEA_LEVEL;
    }
} 