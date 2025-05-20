package com.burc.novadiveplannerupdated.domain.repository;

import com.burc.novadiveplannerupdated.domain.entity.DivePlan;
import io.reactivex.rxjava3.core.Observable;

/**
 * Repository responsible for managing the currently active DivePlan in memory.
 */
public interface ActiveDivePlanRepository {

    /**
     * Retrieves an Observable that emits the currently active DivePlan
     * and any subsequent updates to it.
     *
     * @return An Observable of the active DivePlan.
     */
    Observable<DivePlan> getActiveDivePlan();

    /**
     * Sets the given DivePlan as the currently active one.
     * This will also trigger an emission to observers of getActiveDivePlan().
     *
     * @param divePlan The DivePlan to set as active.
     */
    void setActiveDivePlan(DivePlan divePlan);

    /**
     * Retrieves the current active DivePlan synchronously.
     * This might return null if no plan has been set yet.
     * Use with caution, prefer the reactive getActiveDivePlan() where possible.
     *
     * @return The current active DivePlan, or null.
     */
    // Optional: Consider if a synchronous getter is truly needed.
    // DivePlan getCurrentActiveDivePlan();
} 