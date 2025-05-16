package com.burc.novadiveplannerupdated.domain.entity;

import com.burc.novadiveplannerupdated.domain.model.AlarmSettings;
import com.burc.novadiveplannerupdated.domain.model.AltitudeLevel;
import com.burc.novadiveplannerupdated.domain.model.GradientFactors;
import com.burc.novadiveplannerupdated.domain.model.LastStopDepthOption;
import com.burc.novadiveplannerupdated.domain.model.SurfaceConsumptionRates;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;
import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;

import java.util.Objects;

/**
 * Represents the collection of all dive planning settings for the application.
 * This class is an entity and should be instantiated using its Builder.
 */
public class DiveSettings {

    private final UnitSystem unitSystem;
    private final AltitudeLevel altitudeLevel;
    private final LastStopDepthOption lastStopDepthOption;
    private final GradientFactors gradientFactors;
    private final AlarmSettings alarmSettings;
    private final SurfaceConsumptionRates surfaceConsumptionRates;
    // Future CCR settings can be added here

    private DiveSettings(Builder builder) {
        this.unitSystem = builder.unitSystem;
        this.altitudeLevel = builder.altitudeLevel;
        this.lastStopDepthOption = builder.lastStopDepthOption;
        this.gradientFactors = builder.gradientFactors;
        this.alarmSettings = builder.alarmSettings;
        this.surfaceConsumptionRates = builder.surfaceConsumptionRates;
    }

    // Getters for all fields
    public UnitSystem getUnitSystem() {
        return unitSystem;
    }

    public AltitudeLevel getAltitudeLevel() {
        return altitudeLevel;
    }

    public LastStopDepthOption getLastStopDepthOption() {
        return lastStopDepthOption;
    }

    public GradientFactors getGradientFactors() {
        return gradientFactors;
    }

    public AlarmSettings getAlarmSettings() {
        return alarmSettings;
    }

    public SurfaceConsumptionRates getSurfaceConsumptionRates() {
        return surfaceConsumptionRates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiveSettings that = (DiveSettings) o;
        return unitSystem == that.unitSystem &&
                altitudeLevel == that.altitudeLevel &&
                lastStopDepthOption == that.lastStopDepthOption &&
                Objects.equals(gradientFactors, that.gradientFactors) &&
                Objects.equals(alarmSettings, that.alarmSettings) &&
                Objects.equals(surfaceConsumptionRates, that.surfaceConsumptionRates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unitSystem, altitudeLevel, lastStopDepthOption, gradientFactors, alarmSettings, surfaceConsumptionRates);
    }

    @Override
    public String toString() {
        return "DiveSettings{" +
                "unitSystem=" + unitSystem +
                ", altitudeLevel=" + altitudeLevel +
                ", lastStopDepthOption=" + lastStopDepthOption +
                ", gradientFactors=" + gradientFactors +
                ", alarmSettings=" + alarmSettings +
                ", surfaceConsumptionRates=" + surfaceConsumptionRates +
                '}';
    }

    /**
     * Builder for {@link DiveSettings}.
     */
    public static class Builder {
        private UnitSystem unitSystem;
        private AltitudeLevel altitudeLevel;
        private LastStopDepthOption lastStopDepthOption;
        private GradientFactors gradientFactors;
        private AlarmSettings alarmSettings;
        private SurfaceConsumptionRates surfaceConsumptionRates;

        public Builder() {
            // Initialize with sensible defaults or ensure all are set before build()
            // Referencing refcodes/Settings.vb for typical defaults
            this.unitSystem = DomainDefaults.DEFAULT_UNIT_SYSTEM;
            this.altitudeLevel = DomainDefaults.DEFAULT_ALTITUDE_LEVEL;
            this.lastStopDepthOption = DomainDefaults.DEFAULT_LAST_STOP_DEPTH_OPTION;
            this.gradientFactors = new GradientFactors(
                    DomainDefaults.DEFAULT_GF_LOW,
                    DomainDefaults.DEFAULT_GF_HIGH
            );
            this.alarmSettings = new AlarmSettings(
                    DomainDefaults.DEFAULT_END_ALARM_ENABLED,
                    DomainDefaults.DEFAULT_END_ALARM_THRESHOLD_FT,
                    DomainDefaults.DEFAULT_WOB_ALARM_ENABLED,
                    DomainDefaults.DEFAULT_WOB_ALARM_THRESHOLD_FT,
                    DomainDefaults.DEFAULT_OXYGEN_NARCOTIC_ENABLED
            );
            this.surfaceConsumptionRates = new SurfaceConsumptionRates(
                    DomainDefaults.DEFAULT_RMV_DIVE_CUFT_MIN,
                    DomainDefaults.DEFAULT_RMV_DECO_CUFT_MIN
            );
        }

        /**
         * Copy constructor for the Builder.
         * Initializes a new Builder instance with values from an existing DiveSettings object.
         * @param settings The DiveSettings object to copy values from.
         */
        public Builder(DiveSettings settings) {
            Objects.requireNonNull(settings, "settings cannot be null for copy constructor");
            this.unitSystem = settings.getUnitSystem();
            this.altitudeLevel = settings.getAltitudeLevel();
            this.lastStopDepthOption = settings.getLastStopDepthOption();
            this.gradientFactors = settings.getGradientFactors(); // Assumes GradientFactors is immutable or deep copy if needed
            this.alarmSettings = settings.getAlarmSettings();       // Assumes AlarmSettings is immutable or deep copy if needed
            this.surfaceConsumptionRates = settings.getSurfaceConsumptionRates(); // Assumes SurfaceConsumptionRates is immutable or deep copy if needed
        }

        public Builder unitSystem(UnitSystem unitSystem) {
            this.unitSystem = Objects.requireNonNull(unitSystem, "unitSystem cannot be null");
            return this;
        }

        public Builder altitudeLevel(AltitudeLevel altitudeLevel) {
            this.altitudeLevel = Objects.requireNonNull(altitudeLevel, "altitudeLevel cannot be null");
            return this;
        }

        public Builder lastStopDepthOption(LastStopDepthOption lastStopDepthOption) {
            this.lastStopDepthOption = Objects.requireNonNull(lastStopDepthOption, "lastStopDepthOption cannot be null");
            return this;
        }

        public Builder gradientFactors(GradientFactors gradientFactors) {
            this.gradientFactors = Objects.requireNonNull(gradientFactors, "gradientFactors cannot be null");
            return this;
        }

        public Builder alarmSettings(AlarmSettings alarmSettings) {
            this.alarmSettings = Objects.requireNonNull(alarmSettings, "alarmSettings cannot be null");
            return this;
        }

        public Builder surfaceConsumptionRates(SurfaceConsumptionRates surfaceConsumptionRates) {
            this.surfaceConsumptionRates = Objects.requireNonNull(surfaceConsumptionRates, "surfaceConsumptionRates cannot be null");
            return this;
        }

        public DiveSettings build() {
            // Perform null checks here for any fields that don't have defaults and are mandatory
            // For now, all fields have defaults or are wrapped with Objects.requireNonNull in setters.
            return new DiveSettings(this);
        }
    }
} 