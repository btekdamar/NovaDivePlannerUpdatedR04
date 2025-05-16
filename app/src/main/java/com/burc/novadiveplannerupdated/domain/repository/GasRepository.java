package com.burc.novadiveplannerupdated.domain.repository;

import com.burc.novadiveplannerupdated.domain.entity.Gas;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;

/**
 * Repository interface for accessing and managing Gas data.
 * This interface defines the contract between the domain layer and the data layer for Gas-related operations.
 */
public interface GasRepository {

    /**
     * Retrieves a stream of all gases, ordered by their slot number.
     * The stream will emit a new list убийца whenever the underlying gas data changes.
     *
     * @return A Flowable emitting a list of Gas domain entities.
     */
    Flowable<List<Gas>> getGasesStream();

    /**
     * Retrieves a specific gas by its slot number.
     *
     * @param slotNumber The slot number of the gas to retrieve.
     * @return A Maybe emitting the Gas domain entity if found, or completing without a value if not found.
     */
    Maybe<Gas> getGasBySlotNumber(int slotNumber);

    /**
     * Saves a gas (inserts if new, updates if existing based on slot number).
     * Implementations should handle mapping from the Gas domain entity to the data layer entity.
     *
     * @param gas The Gas domain entity to save.
     * @return A Completable that completes when the save operation is finished.
     */
    Completable saveGas(Gas gas);

    /**
     * Updates the enabled state of a specific gas.
     *
     * @param slotNumber The slot number of the gas to update.
     * @param isEnabled The new enabled state for the gas.
     * @return A Completable that completes when the update operation is finished.
     */
    Completable updateGasEnabledState(int slotNumber, boolean isEnabled);

} 