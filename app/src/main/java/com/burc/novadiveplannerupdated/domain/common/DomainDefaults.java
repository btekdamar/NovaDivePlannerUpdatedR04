package com.burc.novadiveplannerupdated.domain.common;

import com.burc.novadiveplannerupdated.domain.model.AltitudeLevel;
import com.burc.novadiveplannerupdated.domain.model.LastStopDepthOption;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;

public final class DomainDefaults {

    private DomainDefaults() {
        // Bu sınıfın örneği oluşturulamaz
    }

    // Unit System
    public static final UnitSystem DEFAULT_UNIT_SYSTEM = UnitSystem.IMPERIAL;

    // Altitude Level
    public static final AltitudeLevel DEFAULT_ALTITUDE_LEVEL = AltitudeLevel.SEA_LEVEL;
    // İrtifa için min/max değerler AltitudeLevel enum'ının kendisinde yönetiliyor.

    // Last Stop Depth Option
    public static final LastStopDepthOption DEFAULT_LAST_STOP_DEPTH_OPTION = LastStopDepthOption.TWENTY_FEET;
    // Son durak derinliği için min/max seçenekleri LastStopDepthOption enum'ında.

    // Gradient Factors
    public static final int DEFAULT_GF_LOW = 30;
    public static final int DEFAULT_GF_HIGH = 85;
    public static final int MIN_GF_VALUE = 15; 
    public static final int MAX_GF_VALUE = 95;

    // Alarm Settings
    public static final boolean DEFAULT_END_ALARM_ENABLED = false;
    public static final double DEFAULT_END_ALARM_THRESHOLD_FT = 100.0;
    public static final int MIN_END_ALARM_THRESHOLD_FT = 100;
    public static final int MAX_END_ALARM_THRESHOLD_FT = 200;

    public static final boolean DEFAULT_WOB_ALARM_ENABLED = false;
    public static final double DEFAULT_WOB_ALARM_THRESHOLD_FT = 100.0;
    public static final int MIN_WOB_ALARM_THRESHOLD_FT = 100;
    public static final int MAX_WOB_ALARM_THRESHOLD_FT = 200;

    public static final boolean DEFAULT_OXYGEN_NARCOTIC_ENABLED = false;

    // Surface Consumption Rates (RMV)
    public static final double DEFAULT_RMV_DIVE_CUFT_MIN = 0.9;
    public static final double DEFAULT_RMV_DECO_CUFT_MIN = 0.7;
    public static final double MIN_RMV_CUFT_MIN = 0.3;   
    public static final double MAX_RMV_CUFT_MIN = 4.0;   

    // Dive Segment Defaults (for AddEditSegmentDialog and DiveSegment Builder)
    public static final long DEFAULT_SEGMENT_DURATION_SECONDS = 600; // 10 minutes
    public static final double DEFAULT_SEGMENT_DEPTH_FT = 10.0;    // 10 feet
    
    // Imperial Ascent/Descent Rates (ft/min)
    public static final double DEFAULT_ASCENT_RATE_FT_MIN = 30.0;
    public static final double MIN_ASCENT_RATE_FT_MIN = 3.0;
    public static final double MAX_ASCENT_RATE_FT_MIN = 30.0;
    public static final double DEFAULT_DESCENT_RATE_FT_MIN = 60.0;
    public static final double MIN_DESCENT_RATE_FT_MIN = 3.0;
    public static final double MAX_DESCENT_RATE_FT_MIN = 75.0;

    // Metric Ascent/Descent Rates (m/min)
    public static final double DEFAULT_ASCENT_RATE_M_MIN = 9.0;
    public static final double MIN_ASCENT_RATE_M_MIN = 3.0;
    public static final double MAX_ASCENT_RATE_M_MIN = 10.0;
    public static final double DEFAULT_DESCENT_RATE_M_MIN = 18.0;
    public static final double MIN_DESCENT_RATE_M_MIN = 3.0;
    public static final double MAX_DESCENT_RATE_M_MIN = 25.0;
    
    public static final double DEFAULT_CC_SET_POINT = 0.5; // Default PPO2 for CC when adding new segment
    public static final double MIN_SET_POINT = 0.4; // PPO2 for CC
    public static final double MAX_SET_POINT = 1.6; // PPO2 for CC

    // Default ascent rate for NDL calculations, as suggested by Pelagic doc page 21 (A'R = 30 ft/min)
    public static final double DEFAULT_ASCENT_RATE_FOR_NDL_FPM = 30.0; // ft/min

    // Decompression Ascent Rates (TODO: Add to DiveSettings)
    public static final double DEFAULT_DECO_ASCENT_RATE_FPM = 30.0; // ft/min, for ascents between deco stops
    public static final double DEFAULT_FINAL_ASCENT_RATE_FPM = 10.0;  // ft/min, for ascent from last stop to surface

    // Dive Calculation Constants
    public static final double DEPTH_CONSTANT_METRIC = 10.0; // msw per ATA
    public static final double DEPTH_CONSTANT_IMPERIAL = 33.0; // fsw per ATA
    public static final double MIN_SAFE_PPO2_ATA = 0.21; // Minimum safe PPO2 for hypoxia calculations
    public static final double SURFACE_PRESSURE_ATA = 1.0; // Standard surface pressure in ATA

    // Maximum depth for planning purposes
    public static final double MAX_DEPTH_FT_PLANNING = 499.0; // Corresponds to ~150m, used in pickers

    // Oxygen Toxicity Defaults
    public static final double DEFAULT_CNS_LIMIT_PERCENT = 100.0; // Default CNS% limit for OTR calculations

    // WOB (Work of Breathing) Calculation Constants from 039_END_WOB_HT_Formulas v1.7.md
    public static final double WOB_EMPIRICAL_CONSTANT_A = 0.167; // Co-efficient for FN2 term
    public static final double WOB_EMPIRICAL_CONSTANT_B = 1.167; // Co-efficient for PPO2 term
    public static final double WOB_EMPIRICAL_DENOMINATOR = 1.202; // Denominator in WOB formula

    // Gas Consumption Defaults
    public static final double DEFAULT_TANK_SERVICE_PRESSURE_PSI = 3000.0; // Default tank service pressure in PSI
} 