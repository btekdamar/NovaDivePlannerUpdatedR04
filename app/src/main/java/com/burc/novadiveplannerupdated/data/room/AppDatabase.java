package com.burc.novadiveplannerupdated.data.room;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.burc.novadiveplannerupdated.data.room.dao.GasDao;
import com.burc.novadiveplannerupdated.data.room.dao.SettingsDao;
import com.burc.novadiveplannerupdated.data.room.entity.DiveSettingsEntity;
import com.burc.novadiveplannerupdated.data.room.entity.GasEntity;

/**
 * The Room database for the application.
 * It contains the DiveSettingsEntity and GasEntity, and provides access to their DAOs.
 */
@Database(entities = {DiveSettingsEntity.class, GasEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    /**
     * Abstract method to get the Data Access Object for Settings.
     *
     * @return The SettingsDao instance.
     */
    public abstract SettingsDao settingsDao();

    /**
     * Abstract method to get the Data Access Object for Gases.
     *
     * @return The GasDao instance.
     */
    public abstract GasDao gasDao();

    // Singleton yönetimi Hilt tarafından yapıldığı için aşağıdaki kodlar Hilt ile gereksizdir.
    /*
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "nova_dive_planner_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    */
} 