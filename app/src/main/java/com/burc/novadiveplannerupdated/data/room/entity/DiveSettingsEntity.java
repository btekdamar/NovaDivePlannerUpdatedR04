package com.burc.novadiveplannerupdated.data.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.burc.novadiveplannerupdated.domain.model.AlarmSettings;
import com.burc.novadiveplannerupdated.domain.model.AltitudeLevel;
import com.burc.novadiveplannerupdated.domain.model.GradientFactors;
import com.burc.novadiveplannerupdated.domain.model.LastStopDepthOption;
import com.burc.novadiveplannerupdated.domain.model.SurfaceConsumptionRates;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;

@Entity(tableName = "dive_settings")
public class DiveSettingsEntity {

    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "unit_system")
    public UnitSystem unitSystem; // Stored as ordinal by default

    @ColumnInfo(name = "altitude_level")
    public AltitudeLevel altitudeLevel; // Stored as ordinal by default

    @ColumnInfo(name = "last_stop_depth_option")
    public LastStopDepthOption lastStopDepthOption; // Stored as ordinal by default

    @Embedded(prefix = "gf_")
    public GradientFactors gradientFactors;

    @Embedded(prefix = "alarm_")
    public AlarmSettings alarmSettings;

    @Embedded(prefix = "sr_")
    public SurfaceConsumptionRates surfaceConsumptionRates;

    public DiveSettingsEntity() {}

    public DiveSettingsEntity(int id, UnitSystem unitSystem, AltitudeLevel altitudeLevel,
                              LastStopDepthOption lastStopDepthOption, GradientFactors gradientFactors,
                              AlarmSettings alarmSettings, SurfaceConsumptionRates surfaceConsumptionRates) {
        this.id = id;
        this.unitSystem = unitSystem;
        this.altitudeLevel = altitudeLevel;
        this.lastStopDepthOption = lastStopDepthOption;
        this.gradientFactors = gradientFactors;
        this.alarmSettings = alarmSettings;
        this.surfaceConsumptionRates = surfaceConsumptionRates;
    }
} 