package com.burc.novadiveplannerupdated.domain.usecase.settings;

import com.burc.novadiveplannerupdated.domain.entity.DiveSettings;
import com.burc.novadiveplannerupdated.domain.repository.SettingsRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;

/**
 * Use case for saving the dive settings.
 */
public class SaveSettingsUseCase {

    private final SettingsRepository settingsRepository;

    @Inject
    public SaveSettingsUseCase(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    /**
     * Executes the use case to save the dive settings.
     *
     * @param settings The {@link DiveSettings} to save.
     * @return A Completable that completes when the save operation is finished.
     */
    public Completable execute(DiveSettings settings) {
        // Perform any additional business logic/validation before saving if needed
        if (settings == null) {
            return Completable.error(new IllegalArgumentException("DiveSettings cannot be null."));
        }
        return settingsRepository.saveSettings(settings);
    }
} 