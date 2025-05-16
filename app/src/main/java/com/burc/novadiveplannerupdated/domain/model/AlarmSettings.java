package com.burc.novadiveplannerupdated.domain.model;

import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;

import java.util.Objects;

public class AlarmSettings {
    private final boolean isEndAlarmEnabled;
    private final double endAlarmThresholdFt; // Equivalent Narcotic Depth threshold in feet
    private final boolean isWobAlarmEnabled;
    private final double wobAlarmThresholdFt; // Work of Breathing alarm threshold as an equivalent depth in feet
    private final boolean isOxygenNarcoticEnabled;

    public AlarmSettings(boolean isEndAlarmEnabled, double endAlarmThresholdFt,
                         boolean isWobAlarmEnabled, double wobAlarmThresholdFt,
                         boolean isOxygenNarcoticEnabled) {

        if (isEndAlarmEnabled && (endAlarmThresholdFt < DomainDefaults.MIN_END_ALARM_THRESHOLD_FT || endAlarmThresholdFt > DomainDefaults.MAX_END_ALARM_THRESHOLD_FT)) {
            throw new IllegalArgumentException("END alarm threshold must be between " +
                    DomainDefaults.MIN_END_ALARM_THRESHOLD_FT + " and " + DomainDefaults.MAX_END_ALARM_THRESHOLD_FT +
                    " ft if enabled. Was: " + endAlarmThresholdFt);
        }
        if (isWobAlarmEnabled && (wobAlarmThresholdFt < DomainDefaults.MIN_WOB_ALARM_THRESHOLD_FT || wobAlarmThresholdFt > DomainDefaults.MAX_WOB_ALARM_THRESHOLD_FT)) {
            throw new IllegalArgumentException("WOB alarm threshold must be between " +
                    DomainDefaults.MIN_WOB_ALARM_THRESHOLD_FT + " and " + DomainDefaults.MAX_WOB_ALARM_THRESHOLD_FT +
                    " ft if enabled. Was: " + wobAlarmThresholdFt);
        }

        this.isEndAlarmEnabled = isEndAlarmEnabled;
        this.endAlarmThresholdFt = endAlarmThresholdFt;
        this.isWobAlarmEnabled = isWobAlarmEnabled;
        this.wobAlarmThresholdFt = wobAlarmThresholdFt;
        this.isOxygenNarcoticEnabled = isOxygenNarcoticEnabled;
    }

    // Getters
    public boolean isEndAlarmEnabled() { return isEndAlarmEnabled; }
    public double getEndAlarmThresholdFt() { return endAlarmThresholdFt; }
    public boolean isWobAlarmEnabled() { return isWobAlarmEnabled; }
    public double getWobAlarmThresholdFt() { return wobAlarmThresholdFt; }
    public boolean isOxygenNarcoticEnabled() { return isOxygenNarcoticEnabled; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlarmSettings that = (AlarmSettings) o;
        return isEndAlarmEnabled == that.isEndAlarmEnabled &&
                Double.compare(that.endAlarmThresholdFt, endAlarmThresholdFt) == 0 &&
                isWobAlarmEnabled == that.isWobAlarmEnabled &&
                Double.compare(that.wobAlarmThresholdFt, wobAlarmThresholdFt) == 0 &&
                isOxygenNarcoticEnabled == that.isOxygenNarcoticEnabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEndAlarmEnabled, endAlarmThresholdFt, isWobAlarmEnabled, wobAlarmThresholdFt, isOxygenNarcoticEnabled);
    }

    @Override
    public String toString() {
        return "AlarmSettings{" +
                "isEndAlarmEnabled=" + isEndAlarmEnabled +
                ", endAlarmThresholdFt=" + endAlarmThresholdFt +
                ", isWobAlarmEnabled=" + isWobAlarmEnabled +
                ", wobAlarmThresholdFt=" + wobAlarmThresholdFt +
                ", isOxygenNarcoticEnabled=" + isOxygenNarcoticEnabled +
                '}';
    }
} 