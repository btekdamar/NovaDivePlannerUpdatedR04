package com.burc.novadiveplannerupdated.data.repository;

import android.util.Log;
import com.burc.novadiveplannerupdated.data.mapper.SettingsMapper;
import com.burc.novadiveplannerupdated.data.room.dao.SettingsDao;
import com.burc.novadiveplannerupdated.data.room.entity.DiveSettingsEntity;
import com.burc.novadiveplannerupdated.domain.entity.DiveSettings;
import com.burc.novadiveplannerupdated.domain.repository.SettingsRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Implementation of {@link SettingsRepository} for managing dive settings.
 * This class interacts with the {@link SettingsDao} and uses {@link SettingsMapper}
 * for object transformations.
 */
@Singleton
public class SettingsRepositoryImpl implements SettingsRepository {

    private static final String TAG = "SettingsRepositoryImpl";
    private final SettingsDao settingsDao;

    @Inject
    public SettingsRepositoryImpl(SettingsDao settingsDao) {
        this.settingsDao = settingsDao;
    }

    @Override
    public Flowable<DiveSettings> getSettings() {
        Log.d(TAG, "getSettings called");
        return settingsDao.getAppSettings()
                .doOnSubscribe(disposable -> Log.d(TAG, "DAO onSubscribe - Subscribed to DAO Flowable"))
                .doOnEach(notification -> {
                    if (notification.isOnNext()) {
                        Log.d(TAG, "doOnEach - DAO onNext: " + (notification.getValue() != null ? notification.getValue().toString() : "null"));
                    } else if (notification.isOnError()) {
                        Log.e(TAG, "doOnEach - DAO onError", notification.getError());
                    } else if (notification.isOnComplete()) {
                        Log.d(TAG, "doOnEach - DAO onComplete");
                    }
                })
                .doOnNext(entity -> Log.d(TAG, "(After doOnEach) DAO returned entity: " + (entity != null ? entity.toString() : "null")))
                .map(diveSettingsEntity -> {
                    Log.d(TAG, "Mapping entity: " + (diveSettingsEntity != null ? diveSettingsEntity.toString() : "null"));
                    DiveSettings domainSettings = SettingsMapper.toDomain(diveSettingsEntity);
                    if (domainSettings == null) {
                        Log.w(TAG, "Mapped to null domainSettings (entity was likely null), returning new Builder().build()");
                        return new DiveSettings.Builder().build();
                    }
                    Log.d(TAG, "Mapped to domainSettings: " + domainSettings.toString());
                    return domainSettings;
                })
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "Error in getSettings stream (after map), returning default settings", throwable);
                    return new DiveSettings.Builder().build();
                })
                .subscribeOn(Schedulers.io())
                .doFinally(() -> Log.d(TAG, "getSettings Flowable terminated (doFinally)"));
    }

    @Override
    public Completable saveSettings(DiveSettings settings) {
        Log.d(TAG, "saveSettings called for: " + (settings != null ? settings.toString() : "null"));
        if (settings == null) {
            Log.e(TAG, "DiveSettings to save cannot be null");
            return Completable.error(new NullPointerException("DiveSettings to save cannot be null"));
        }
        DiveSettingsEntity entityToSave = SettingsMapper.toEntity(settings);
        Log.d(TAG, "Saving entity: " + (entityToSave != null ? entityToSave.toString() : "null"));
        return settingsDao.saveSettings(entityToSave)
                .subscribeOn(Schedulers.io());
    }
} 