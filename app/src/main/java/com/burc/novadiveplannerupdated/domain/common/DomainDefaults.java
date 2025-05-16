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

    // Dive Calculation Constants
    public static final double DEPTH_CONSTANT_METRIC = 10.0; // msw per ATA
    public static final double DEPTH_CONSTANT_IMPERIAL = 33.0; // fsw per ATA
    public static final double MIN_SAFE_PPO2_ATA = 0.21; // Minimum safe PPO2 for hypoxia calculations
    public static final double SURFACE_PRESSURE_ATA = 1.0; // Standard surface pressure in ATA

    // WOB (Work of Breathing) Calculation Constants from 039_END_WOB_HT_Formulas v1.7.md
    public static final double WOB_EMPIRICAL_CONSTANT_A = 0.167; // Co-efficient for FN2 term
    public static final double WOB_EMPIRICAL_CONSTANT_B = 1.167; // Co-efficient for PPO2 term
    public static final double WOB_EMPIRICAL_DENOMINATOR = 1.202; // Denominator in WOB formula
} 