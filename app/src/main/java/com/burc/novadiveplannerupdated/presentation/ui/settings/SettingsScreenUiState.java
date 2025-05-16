package com.burc.novadiveplannerupdated.presentation.ui.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.burc.novadiveplannerupdated.domain.entity.DiveSettings;
import java.util.Objects;
import java.util.Locale;

public class SettingsScreenUiState {

    private final boolean isLoading;
    @Nullable
    private final DiveSettings diveSettings;
    @Nullable
    private final String errorMessage;

    // UI'da doğrudan gösterilecek değerler ve picker konfigürasyonları
    @Nullable
    private final String unitSystemDisplay;
    @Nullable
    private final String altitudeLevelDisplay;
    @Nullable
    private final String lastStopDepthDisplay;

    @Nullable
    private final NumericSettingConfig gfLowConfig;
    @Nullable
    private final NumericSettingConfig gfHighConfig;

    private final boolean isEndAlarmEnabled;
    @Nullable
    private final NumericSettingConfig endAlarmThresholdConfig; // Enable değilse null olabilir

    private final boolean isWobAlarmEnabled;
    @Nullable
    private final NumericSettingConfig wobAlarmThresholdConfig; // Enable değilse null olabilir

    private final boolean isOxygenNarcoticEnabled;

    @Nullable
    private final NumericSettingConfig sacDiveConfig;
    @Nullable
    private final NumericSettingConfig sacDecoConfig;


    /**
     * Represents the UI configuration for a numeric setting that can be edited
     * using a NumberPickerDialogFragment.
     */
    public static class NumericSettingConfig {
        private final String displayedValueString; // e.g., "30 m", "100 ft", "0.75 cuft/min"
        private final Number currentPickerValue;   // Integer or Float
        private final Number minPickerValue;
        private final Number maxPickerValue;
        private final Number stepPickerValue;
        private final String unitSuffix;           // e.g., "m", "ft", "%", "cuft/min"
        private final boolean isFloatType;
        @Nullable
        private final String decimalFormatPattern; // e.g., "%.2f", null for integers
        private final int decimalPlacesToRound; // e.g., 2, 0 for integers

        // Constructor for Integer values
        public NumericSettingConfig(String displayedValueString,
                                    int currentPickerValue, int minPickerValue, int maxPickerValue, int stepPickerValue,
                                    String unitSuffix) {
            this.displayedValueString = displayedValueString;
            this.currentPickerValue = currentPickerValue;
            this.minPickerValue = minPickerValue;
            this.maxPickerValue = maxPickerValue;
            this.stepPickerValue = stepPickerValue;
            this.unitSuffix = unitSuffix;
            this.isFloatType = false;
            this.decimalFormatPattern = null;
            this.decimalPlacesToRound = 0;
        }

        // Constructor for Float values
        public NumericSettingConfig(String displayedValueString,
                                    float currentPickerValue, float minPickerValue, float maxPickerValue, float stepPickerValue,
                                    String unitSuffix, @NonNull String decimalFormatPattern, int decimalPlacesToRound) {
            this.displayedValueString = displayedValueString;
            this.currentPickerValue = currentPickerValue;
            this.minPickerValue = minPickerValue;
            this.maxPickerValue = maxPickerValue;
            this.stepPickerValue = stepPickerValue;
            this.unitSuffix = unitSuffix;
            this.isFloatType = true;
            this.decimalFormatPattern = decimalFormatPattern;
            this.decimalPlacesToRound = decimalPlacesToRound;
        }

        // Getters
        public String getDisplayedValueString() { return displayedValueString; }
        public Number getCurrentPickerValue() { return currentPickerValue; }
        public Number getMinPickerValue() { return minPickerValue; }
        public Number getMaxPickerValue() { return maxPickerValue; }
        public Number getStepPickerValue() { return stepPickerValue; }
        public String getUnitSuffix() { return unitSuffix; }
        public boolean isFloatType() { return isFloatType; }
        @Nullable public String getDecimalFormatPattern() { return decimalFormatPattern; }
        public int getDecimalPlacesToRound() { return decimalPlacesToRound; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NumericSettingConfig that = (NumericSettingConfig) o;
            return isFloatType == that.isFloatType &&
                   decimalPlacesToRound == that.decimalPlacesToRound &&
                   Objects.equals(displayedValueString, that.displayedValueString) &&
                   Objects.equals(currentPickerValue, that.currentPickerValue) &&
                   Objects.equals(minPickerValue, that.minPickerValue) &&
                   Objects.equals(maxPickerValue, that.maxPickerValue) &&
                   Objects.equals(stepPickerValue, that.stepPickerValue) &&
                   Objects.equals(unitSuffix, that.unitSuffix) &&
                   Objects.equals(decimalFormatPattern, that.decimalFormatPattern);
        }

        @Override
        public int hashCode() {
            return Objects.hash(displayedValueString, currentPickerValue, minPickerValue, maxPickerValue, stepPickerValue, unitSuffix, isFloatType, decimalFormatPattern, decimalPlacesToRound);
        }

        @Override
        public String toString() {
            return String.format(Locale.US,
                "NumericSettingConfig{displayedValue='%s', current=%s, min=%s, max=%s, step=%s, unit='%s', isFloat=%b, format='%s', places=%d}",
                displayedValueString, currentPickerValue, minPickerValue, maxPickerValue, stepPickerValue, unitSuffix, isFloatType, decimalFormatPattern, decimalPlacesToRound);
        }
    }


    private SettingsScreenUiState(boolean isLoading, @Nullable DiveSettings diveSettings, @Nullable String errorMessage,
                                 @Nullable String unitSystemDisplay, @Nullable String altitudeLevelDisplay, @Nullable String lastStopDepthDisplay,
                                 @Nullable NumericSettingConfig gfLowConfig, @Nullable NumericSettingConfig gfHighConfig,
                                 boolean isEndAlarmEnabled, @Nullable NumericSettingConfig endAlarmThresholdConfig,
                                 boolean isWobAlarmEnabled, @Nullable NumericSettingConfig wobAlarmThresholdConfig,
                                 boolean isOxygenNarcoticEnabled,
                                 @Nullable NumericSettingConfig sacDiveConfig, @Nullable NumericSettingConfig sacDecoConfig) {
        this.isLoading = isLoading;
        this.diveSettings = diveSettings;
        this.errorMessage = errorMessage;
        this.unitSystemDisplay = unitSystemDisplay;
        this.altitudeLevelDisplay = altitudeLevelDisplay;
        this.lastStopDepthDisplay = lastStopDepthDisplay;
        this.gfLowConfig = gfLowConfig;
        this.gfHighConfig = gfHighConfig;
        this.isEndAlarmEnabled = isEndAlarmEnabled;
        this.endAlarmThresholdConfig = endAlarmThresholdConfig;
        this.isWobAlarmEnabled = isWobAlarmEnabled;
        this.wobAlarmThresholdConfig = wobAlarmThresholdConfig;
        this.isOxygenNarcoticEnabled = isOxygenNarcoticEnabled;
        this.sacDiveConfig = sacDiveConfig;
        this.sacDecoConfig = sacDecoConfig;
    }

    public boolean isLoading() { return isLoading; }
    @Nullable public DiveSettings getDiveSettings() { return diveSettings; }
    @Nullable public String getErrorMessage() { return errorMessage; }
    @Nullable public String getUnitSystemDisplay() { return unitSystemDisplay; }
    @Nullable public String getAltitudeLevelDisplay() { return altitudeLevelDisplay; }
    @Nullable public String getLastStopDepthDisplay() { return lastStopDepthDisplay; }
    @Nullable public NumericSettingConfig getGfLowConfig() { return gfLowConfig; }
    @Nullable public NumericSettingConfig getGfHighConfig() { return gfHighConfig; }
    public boolean isEndAlarmEnabled() { return isEndAlarmEnabled; }
    @Nullable public NumericSettingConfig getEndAlarmThresholdConfig() { return endAlarmThresholdConfig; }
    public boolean isWobAlarmEnabled() { return isWobAlarmEnabled; }
    @Nullable public NumericSettingConfig getWobAlarmThresholdConfig() { return wobAlarmThresholdConfig; }
    public boolean isOxygenNarcoticEnabled() { return isOxygenNarcoticEnabled; }
    @Nullable public NumericSettingConfig getSacDiveConfig() { return sacDiveConfig; }
    @Nullable public NumericSettingConfig getSacDecoConfig() { return sacDecoConfig; }


    public static SettingsScreenUiState loading() {
        return new SettingsScreenUiState(true, null, null, null, null, null, null, null, false, null, false, null, false, null, null);
    }

    public static SettingsScreenUiState success(@NonNull DiveSettings diveSettings,
                                                @NonNull String unitSystemDisplay,
                                                @NonNull String altitudeLevelDisplay,
                                                @NonNull String lastStopDepthDisplay,
                                                @NonNull NumericSettingConfig gfLowConfig,
                                                @NonNull NumericSettingConfig gfHighConfig,
                                                boolean isEndAlarmEnabled,
                                                @Nullable NumericSettingConfig endAlarmThresholdConfig,
                                                boolean isWobAlarmEnabled,
                                                @Nullable NumericSettingConfig wobAlarmThresholdConfig,
                                                boolean isOxygenNarcoticEnabled,
                                                @NonNull NumericSettingConfig sacDiveConfig,
                                                @NonNull NumericSettingConfig sacDecoConfig) {
        return new SettingsScreenUiState(false, diveSettings, null,
                unitSystemDisplay, altitudeLevelDisplay, lastStopDepthDisplay,
                gfLowConfig, gfHighConfig,
                isEndAlarmEnabled, endAlarmThresholdConfig,
                isWobAlarmEnabled, wobAlarmThresholdConfig,
                isOxygenNarcoticEnabled,
                sacDiveConfig, sacDecoConfig);
    }

    public static SettingsScreenUiState error(String message) {
        return new SettingsScreenUiState(false, null, message, null, null, null, null, null, false, null, false, null, false, null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettingsScreenUiState that = (SettingsScreenUiState) o;
        return isLoading == that.isLoading &&
                isEndAlarmEnabled == that.isEndAlarmEnabled &&
                isWobAlarmEnabled == that.isWobAlarmEnabled &&
                isOxygenNarcoticEnabled == that.isOxygenNarcoticEnabled &&
                Objects.equals(diveSettings, that.diveSettings) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(unitSystemDisplay, that.unitSystemDisplay) &&
                Objects.equals(altitudeLevelDisplay, that.altitudeLevelDisplay) &&
                Objects.equals(lastStopDepthDisplay, that.lastStopDepthDisplay) &&
                Objects.equals(gfLowConfig, that.gfLowConfig) &&
                Objects.equals(gfHighConfig, that.gfHighConfig) &&
                Objects.equals(endAlarmThresholdConfig, that.endAlarmThresholdConfig) &&
                Objects.equals(wobAlarmThresholdConfig, that.wobAlarmThresholdConfig) &&
                Objects.equals(sacDiveConfig, that.sacDiveConfig) &&
                Objects.equals(sacDecoConfig, that.sacDecoConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isLoading, diveSettings, errorMessage,
                unitSystemDisplay, altitudeLevelDisplay, lastStopDepthDisplay,
                gfLowConfig, gfHighConfig,
                isEndAlarmEnabled, endAlarmThresholdConfig,
                isWobAlarmEnabled, wobAlarmThresholdConfig,
                isOxygenNarcoticEnabled,
                sacDiveConfig, sacDecoConfig);
    }

    @Override
    public String toString() {
        return "SettingsScreenUiState{" +
                "isLoading=" + isLoading +
                ", diveSettings=" + (diveSettings != null ? diveSettings.toString() : "null") +
                ", errorMessage='" + errorMessage + '\'' +
                ", unitSystemDisplay='" + unitSystemDisplay + '\'' +
                ", altitudeLevelDisplay='" + altitudeLevelDisplay + '\'' +
                ", lastStopDepthDisplay='" + lastStopDepthDisplay + '\'' +
                ", gfLowConfig=" + (gfLowConfig != null ? gfLowConfig.toString() : "null") +
                ", gfHighConfig=" + (gfHighConfig != null ? gfHighConfig.toString() : "null") +
                ", isEndAlarmEnabled=" + isEndAlarmEnabled +
                ", endAlarmThresholdConfig=" + (endAlarmThresholdConfig != null ? endAlarmThresholdConfig.toString() : "null") +
                ", isWobAlarmEnabled=" + isWobAlarmEnabled +
                ", wobAlarmThresholdConfig=" + (wobAlarmThresholdConfig != null ? wobAlarmThresholdConfig.toString() : "null") +
                ", isOxygenNarcoticEnabled=" + isOxygenNarcoticEnabled +
                ", sacDiveConfig=" + (sacDiveConfig != null ? sacDiveConfig.toString() : "null") +
                ", sacDecoConfig=" + (sacDecoConfig != null ? sacDecoConfig.toString() : "null") +
                '}';
    }
} 