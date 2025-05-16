package com.burc.novadiveplannerupdated.data.mapper;

import com.burc.novadiveplannerupdated.data.common.DataConstants;
import com.burc.novadiveplannerupdated.data.room.entity.DiveSettingsEntity;
import com.burc.novadiveplannerupdated.domain.entity.DiveSettings;
// Import domain.model classes if they are not automatically resolved by the IDE
// based on their usage in DiveSettings and DiveSettingsEntity.
// For example:
// import com.burc.novadiveplannerupdated.domain.model.UnitSystem;
// import com.burc.novadiveplannerupdated.domain.model.AltitudeLevel;
// etc.

/**
 * Mapper class to convert between domain {@link DiveSettings} and data {@link DiveSettingsEntity}.
 */
public class SettingsMapper {

    // Private constructor to prevent instantiation
    private SettingsMapper() {}

    /**
     * Maps a {@link DiveSettingsEntity} from the data layer to a {@link DiveSettings} domain entity.
     *
     * @param entity The DiveSettingsEntity to map.
     * @return The mapped DiveSettings domain entity.
     *         Returns null if the input entity is null.
     */
    public static DiveSettings toDomain(DiveSettingsEntity entity) {
        if (entity == null) {
            // If entity is null, we might return default domain settings
            // This ensures the domain layer always gets a valid DiveSettings object.
            return new DiveSettings.Builder().build(); 
        }
        // Create builder from entity fields
        DiveSettings.Builder builder = new DiveSettings.Builder()
                .unitSystem(entity.unitSystem)
                .altitudeLevel(entity.altitudeLevel)
                .lastStopDepthOption(entity.lastStopDepthOption);

        if (entity.gradientFactors != null) {
            builder.gradientFactors(entity.gradientFactors);
        }
        if (entity.alarmSettings != null) {
            builder.alarmSettings(entity.alarmSettings);
        }
        if (entity.surfaceConsumptionRates != null) {
            builder.surfaceConsumptionRates(entity.surfaceConsumptionRates);
        }
        return builder.build();
    }

    /**
     * Maps a {@link DiveSettings} domain entity to a {@link DiveSettingsEntity} for the data layer.
     *
     * @param domainModel The DiveSettings domain entity to map.
     * @return The mapped DiveSettingsEntity.
     *         Returns null if the input domainModel is null.
     */
    public static DiveSettingsEntity toEntity(DiveSettings domainModel) {
        if (domainModel == null) {
            return null;
        }
        // Ensure the entity always uses the single primary key
        return new DiveSettingsEntity(
                DataConstants.DIVE_SETTINGS_PRIMARY_KEY, // Correct ID from DataConstants
                domainModel.getUnitSystem(),
                domainModel.getAltitudeLevel(),
                domainModel.getLastStopDepthOption(),
                domainModel.getGradientFactors(),
                domainModel.getAlarmSettings(),
                domainModel.getSurfaceConsumptionRates()
        );
    }
} 