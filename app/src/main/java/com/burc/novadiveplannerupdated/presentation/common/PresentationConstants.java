package com.burc.novadiveplannerupdated.presentation.common;

public final class PresentationConstants {

    private PresentationConstants() {
        // Bu sınıfın örneği oluşturulamaz
    }

    // Fragment Result Request Keys
    public static final String REQUEST_KEY_UNITS_PICKER = "UnitsPickerRequest";
    public static final String REQUEST_KEY_ALTITUDE_PICKER = "AltitudePickerRequest";
    public static final String REQUEST_KEY_LAST_STOP_DEPTH_PICKER = "LastStopDepthPickerRequest";
    public static final String REQUEST_KEY_GF_HIGH_PICKER = "GfHighPickerRequest";
    public static final String REQUEST_KEY_GF_LOW_PICKER = "GfLowPickerRequest";
    public static final String REQUEST_KEY_END_THRESHOLD_PICKER = "EndThresholdPickerRequest";
    public static final String REQUEST_KEY_WOB_THRESHOLD_PICKER = "WobThresholdPickerRequest";
    public static final String REQUEST_KEY_SAC_DIVE_PICKER = "SacDivePickerRequest";
    public static final String REQUEST_KEY_SAC_DECO_PICKER = "SacDecoPickerRequest";

    // Dialog Tags
    public static final String TAG_DIALOG_UNITS_PICKER = "UnitsPickerDialog";
    public static final String TAG_DIALOG_ALTITUDE_PICKER = "AltitudePickerDialog";
    public static final String TAG_DIALOG_LAST_STOP_DEPTH_PICKER = "LastStopDepthPickerDialog";
    public static final String TAG_DIALOG_GF_HIGH_PICKER = "GfHighPickerDialog";
    public static final String TAG_DIALOG_GF_LOW_PICKER = "GfLowPickerDialog";
    public static final String TAG_DIALOG_END_THRESHOLD_PICKER = "EndThresholdPickerDialog";
    public static final String TAG_DIALOG_WOB_THRESHOLD_PICKER = "WobThresholdPickerDialog";
    public static final String TAG_DIALOG_SAC_DIVE_PICKER = "SacDivePickerDialog";
    public static final String TAG_DIALOG_SAC_DECO_PICKER = "SacDecoPickerDialog";

    // --- Picker Configuration Constants ---

    // For END/WOB Alarm Thresholds
    public static final int ALARM_THRESHOLD_METRIC_MIN_M = 30;
    public static final int ALARM_THRESHOLD_METRIC_MAX_M = 60;
    public static final int ALARM_THRESHOLD_METRIC_STEP_M = 1; 
    // Imperial Min/Max for alarms are in DomainDefaults (e.g., MIN_END_ALARM_THRESHOLD_FT)
    public static final int ALARM_THRESHOLD_IMPERIAL_STEP_FT = 10;

    // For SAC/RMV Values (ileride eklenecek)
    // public static final float RMV_METRIC_MIN_L_MIN = 8.5f; // Will be calculated from DomainDefaults
    // public static final float RMV_METRIC_MAX_L_MIN = 113.0f; // Will be calculated from DomainDefaults
    public static final float RMV_METRIC_STEP_L_MIN = 0.5f; // Metric step for RMV in L/min
    // Imperial Min/Max for RMV are in DomainDefaults (e.g., MIN_RMV_CUFT_MIN)
    public static final float RMV_IMPERIAL_STEP_CUFT_MIN = 0.05f;

    // Unit Suffixes 
    public static final String UNIT_SUFFIX_METERS = "m";
    public static final String UNIT_SUFFIX_FEET = "ft";
    public static final String UNIT_SUFFIX_LITERS_MIN = "L/min";
    public static final String UNIT_SUFFIX_CUFT_MIN = "cuft/min";
    public static final String UNIT_SUFFIX_PERCENT = "%";

} 