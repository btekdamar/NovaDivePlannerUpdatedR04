package com.burc.novadiveplannerupdated.di;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.burc.novadiveplannerupdated.data.mapper.SettingsMapper;
import com.burc.novadiveplannerupdated.data.repository.GasRepositoryImpl;
import com.burc.novadiveplannerupdated.data.repository.SettingsRepositoryImpl;
import com.burc.novadiveplannerupdated.data.room.AppDatabase;
import com.burc.novadiveplannerupdated.data.room.dao.GasDao;
import com.burc.novadiveplannerupdated.data.room.dao.SettingsDao;
import com.burc.novadiveplannerupdated.data.room.entity.DiveSettingsEntity;
import com.burc.novadiveplannerupdated.data.room.entity.GasEntity;
import com.burc.novadiveplannerupdated.domain.entity.DiveSettings;
import com.burc.novadiveplannerupdated.domain.model.GasType;
import com.burc.novadiveplannerupdated.domain.repository.GasRepository;
import com.burc.novadiveplannerupdated.domain.repository.SettingsRepository;
import com.burc.novadiveplannerupdated.data.repository.ActiveDivePlanRepositoryImpl;
import com.burc.novadiveplannerupdated.domain.repository.ActiveDivePlanRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class DataModule {

    private static final String DATABASE_NAME = "nova_dive_planner.db";
    private static final String TAG_DATA_MODULE = "DataModule";

    @Provides
    @Singleton
    public static AppDatabase provideAppDatabase(@ApplicationContext Context context,
                                                 Provider<SettingsDao> settingsDaoProvider,
                                                 Provider<GasDao> gasDaoProvider) {
        return Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                DATABASE_NAME
        )
        .addCallback(new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
                Log.d(TAG_DATA_MODULE, "AppDatabase onCreate - Prepopulating default data.");
                Executors.newSingleThreadScheduledExecutor().execute(() -> {
                    // Prepopulate Settings
                    try {
                        SettingsDao settingsDao = settingsDaoProvider.get();
                        DiveSettings defaultDomainSettings = new DiveSettings.Builder().build();
                        DiveSettingsEntity defaultEntity = SettingsMapper.toEntity(defaultDomainSettings);
                        if (defaultEntity != null) {
                            settingsDao.saveSettings(defaultEntity)
                                    .blockingAwait();
                            Log.d(TAG_DATA_MODULE, "Default settings prepopulated into database.");
                        } else {
                            Log.e(TAG_DATA_MODULE, "Failed to create default DiveSettingsEntity for prepopulation.");
                        }
                    } catch (Exception e) {
                        Log.e(TAG_DATA_MODULE, "Error prepopulating default settings", e);
                    }

                    // Prepopulate Gases
                    try {
                        GasDao gasDao = gasDaoProvider.get();
                        List<GasEntity> defaultGases = createDefaultGases();
                        for (GasEntity gas : defaultGases) {
                            gasDao.insertOrUpdateGas(gas).blockingAwait();
                        }
                        Log.d(TAG_DATA_MODULE, "Default gases prepopulated into database.");
                    } catch (Exception e) {
                        Log.e(TAG_DATA_MODULE, "Error prepopulating default gases", e);
                    }
                });
            }
        })
        .fallbackToDestructiveMigration()
        .build();
    }

    private static List<GasEntity> createDefaultGases() {
        List<GasEntity> gases = new ArrayList<>();
        // Slot 1: AIR (Enabled by default)
        gases.add(new GasEntity(1, true, "AIR", 0.21, 0.0, 1.4, GasType.OPEN_CIRCUIT, 0.0, 0.0));
        
        // Slot 2-10: Default Trimix (Disabled by default)
        // Örnek bir varsayılan Trimix (TX 18/45), diğerleri benzer veya farklı olabilir
        // Gerçek değerler ve çeşitlilik projeye göre ayarlanabilir.
        gases.add(new GasEntity(2, false, "TX 18/45", 0.18, 0.45, 1.4, GasType.OPEN_CIRCUIT, 0.0, 0.0));
        gases.add(new GasEntity(3, false, "NX 32", 0.32, 0.0, 1.6, GasType.OPEN_CIRCUIT, 0.0, 0.0));
        gases.add(new GasEntity(4, false, "TX 10/70", 0.10, 0.70, 1.3, GasType.OPEN_CIRCUIT, 0.0, 0.0));
        gases.add(new GasEntity(5, false, "OXYGEN", 1.0, 0.0, 1.6, GasType.OPEN_CIRCUIT, 0.0, 0.0));
        // Diğer 5 slot için de benzer varsayılanlar eklenebilir veya null bırakılabilir
        // Şimdilik ilk 5'i tanımlayalım, kalanlar için kullanıcı düzenlemesi beklenebilir.
        // Ya da hepsini basit bir "OFF" gazı olarak ekleyebiliriz.
        for (int i = 6; i <= 10; i++) {
            gases.add(new GasEntity(i, false, "AIR", 0.21, 0.0, 1.4, GasType.OPEN_CIRCUIT, 0.0, 0.0));
        }
        return gases;
    }

    @Provides
    @Singleton
    public static SettingsDao provideSettingsDao(AppDatabase appDatabase) {
        return appDatabase.settingsDao();
    }

    @Provides
    @Singleton
    public static GasDao provideGasDao(AppDatabase appDatabase) {
        return appDatabase.gasDao();
    }

    @Binds
    @Singleton
    public abstract SettingsRepository bindSettingsRepository(SettingsRepositoryImpl settingsRepositoryImpl);

    @Binds
    @Singleton
    public abstract GasRepository bindGasRepository(GasRepositoryImpl gasRepositoryImpl);

    @Binds
    @Singleton
    public abstract ActiveDivePlanRepository bindActiveDivePlanRepository(
            ActiveDivePlanRepositoryImpl activeDivePlanRepositoryImpl
    );

} 