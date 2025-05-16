package com.burc.novadiveplannerupdated.data.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.burc.novadiveplannerupdated.data.room.entity.GasEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;

@Dao
public interface GasDao {

    /**
     * Inserts a gas into the table or updates it if it already exists based on its primary key.
     *
     * @param gas The gas entity to be inserted or updated.
     * @return A Completable that completes when the operation is finished.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertOrUpdateGas(GasEntity gas);

    /**
     * Retrieves all gases from the table, ordered by their slot number.
     *
     * @return A Flowable emitting a list of all gas entities, automatically updated on changes.
     */
    @Query("SELECT * FROM gases ORDER BY slot_number ASC")
    Flowable<List<GasEntity>> getAllGasesSorted();

    /**
     * Retrieves a specific gas from the table by its slot number.
     *
     * @param slotNumber The slot number of the gas to retrieve.
     * @return A Maybe emitting the gas entity if found, or completing without a value if not found.
     */
    @Query("SELECT * FROM gases WHERE slot_number = :slotNumber")
    Maybe<GasEntity> getGasBySlotNumber(int slotNumber);

    /**
     * Updates the enabled state of a specific gas in the table.
     *
     * @param slotNumber The slot number of the gas to update.
     * @param isEnabled The new enabled state for the gas.
     * @return A Completable that completes when the operation is finished.
     */
    @Query("UPDATE gases SET is_enabled = :isEnabled WHERE slot_number = :slotNumber")
    Completable updateGasEnabledState(int slotNumber, boolean isEnabled);

} 