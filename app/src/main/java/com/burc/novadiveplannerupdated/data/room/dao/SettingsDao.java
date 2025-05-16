package com.burc.novadiveplannerupdated.data.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.burc.novadiveplannerupdated.data.common.DataConstants;
import com.burc.novadiveplannerupdated.data.room.entity.DiveSettingsEntity;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

/**
 * Data Access Object for the DiveSettingsEntity.
 */
@Dao
public interface SettingsDao {

    /**
     * Retrieves the dive settings from the database.
     * Since there's only one settings entry, it fetches the one with SINGLE_INSTANCE_ID.
     * Returns a Flowable to observe changes to the settings.
     *
     * @return A Flowable emitting the DiveSettingsEntity.
     */
    @Query("SELECT * FROM dive_settings WHERE id = :id")
    Flowable<DiveSettingsEntity> getSettings(int id);

    /**
     * Inserts or updates the dive settings in the database.
     * If a settings entry with the same ID already exists, it will be replaced.
     *
     * @param settings The DiveSettingsEntity to insert or update.
     * @return A Completable that completes when the operation is finished.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable saveSettings(DiveSettingsEntity settings);

    // We can add a helper method to specifically get the single instance of settings
    // This is more of a convenience if we always use DiveSettingsEntity.SINGLE_INSTANCE_ID
    /**
     * Convenience method to get the single instance of dive settings.
     * @return A Flowable emitting the DiveSettingsEntity.
     */
    default Flowable<DiveSettingsEntity> getAppSettings() {
        return getSettings(DataConstants.DIVE_SETTINGS_PRIMARY_KEY);
    }
} 