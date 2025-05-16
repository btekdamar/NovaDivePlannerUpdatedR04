package com.burc.novadiveplannerupdated.domain.model;

import com.burc.novadiveplannerupdated.domain.util.UnitConverter; 
import java.util.Locale; 

/**
 * Represents the configurable options for the last decompression stop depth.
 * All depths are in Imperial units (feet).
 */
public enum LastStopDepthOption {
    TEN_FEET("10 ft", 10),
    TWENTY_FEET("20 ft", 20);

    private final String imperialDisplayName;
    private final int depthFt;

    LastStopDepthOption(String imperialDisplayName, int depthFt) {
        this.imperialDisplayName = imperialDisplayName;
        this.depthFt = depthFt;
    }

    public int getDepthFt() {
        return depthFt;
    }

    /**
     * Returns the display name for the last stop depth option based on the selected unit system.
     * For IMPERIAL, it returns the predefined string (e.g., "10 ft").
     * For METRIC, it converts the depth to meters, rounds it, and formats as "X m" (e.g., "3 m").
     *
     * @param system The unit system to use for the display string.
     * @return The formatted display string for the UI.
     */
    public String getDisplayString(UnitSystem system) {
        if (system == UnitSystem.METRIC) {
            int depthMeters = UnitConverter.convertFeetToMetersForDisplay(this.depthFt);
            return String.format(Locale.getDefault(), "%d m", depthMeters);
        }
        return imperialDisplayName; // Varsayılan olarak veya Imperial ise
    }

    /**
     * Returns the Imperial display name. Kept for convenience or specific Imperial-only contexts.
     * Consider using getDisplayString(UnitSystem.IMPERIAL) for consistency.
     */
    public String getImperialDisplayName() {
        return imperialDisplayName;
    }

    /**
     * Returns the display string for the current enum constant, defaulting to Imperial.
     * For UI components that directly call toString() on the enum, this will show the Imperial value.
     * It's generally better to use getDisplayString(UnitSystem) in UI logic.
     */
    @Override
    public String toString() {
        return imperialDisplayName; 
    }
} 