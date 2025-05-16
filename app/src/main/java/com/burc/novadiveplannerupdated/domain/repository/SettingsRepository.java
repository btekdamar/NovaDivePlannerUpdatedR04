package com.burc.novadiveplannerupdated.domain.repository;

import com.burc.novadiveplannerupdated.domain.entity.DiveSettings; // Domain entity'miz

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

/**
 * Interface for accessing and modifying dive settings.
 * This acts as a contract for the data layer to implement.
 */
public interface SettingsRepository {

    /**
     * Retrieves the current dive settings.
     *
     * @return A Flowable that emits the current {@link DiveSettings}.
     *         It will emit a new item whenever the settings change.
     *         If no settings are stored, it might emit a default settings object
     *         or handle this scenario based on implementation (e.g., emit error or empty).
     *         For simplicity, we can assume the repository implementation will ensure
     *         a default settings object is created if none exists.
     */
    Flowable<DiveSettings> getSettings();

    /**
     * Saves the given dive settings.
     *
     * @param settings The {@link DiveSettings} to save.
     * @return A {@link Completable} that completes when the save operation is finished,
     *         or emits an error if the operation fails.
     */
    Completable saveSettings(DiveSettings settings);
} 