package com.burc.novadiveplannerupdated.domain.usecase.settings;

import android.util.Log;
import com.burc.novadiveplannerupdated.domain.entity.DiveSettings;
import com.burc.novadiveplannerupdated.domain.repository.SettingsRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Use case for retrieving the current dive settings.
 */
public class GetSettingsUseCase {

    private static final String TAG = "GetSettingsUseCase";
    private final SettingsRepository settingsRepository;

    @Inject
    public GetSettingsUseCase(SettingsRepository settingsRepository) {
        Log.d(TAG, "Constructor called. settingsRepository is " + (settingsRepository == null ? "null" : "not null"));
        this.settingsRepository = settingsRepository;
    }

    /**
     * Executes the use case to get the dive settings.
     *
     * @return A Flowable that emits the current {@link DiveSettings}.
     */
    public Flowable<DiveSettings> execute() {
        Log.d(TAG, "execute() called.");
        if (settingsRepository == null) {
            Log.e(TAG, "settingsRepository is null in execute(), returning Flowable.empty()");
            return Flowable.empty();
        }
        Log.d(TAG, "Calling settingsRepository.getSettings()");
        return settingsRepository.getSettings();
    }
} 