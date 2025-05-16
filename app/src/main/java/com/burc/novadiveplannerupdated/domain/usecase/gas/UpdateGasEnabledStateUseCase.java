package com.burc.novadiveplannerupdated.domain.usecase.gas;

import com.burc.novadiveplannerupdated.domain.repository.GasRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;

/**
 * Use case for updating the enabled state of a specific gas.
 */
public class UpdateGasEnabledStateUseCase {

    private final GasRepository gasRepository;

    @Inject
    public UpdateGasEnabledStateUseCase(GasRepository gasRepository) {
        this.gasRepository = gasRepository;
    }

    /**
     * Executes the use case to update the enabled state of a gas.
     *
     * @param slotNumber The slot number of the gas to update.
     * @param isEnabled  The new enabled state for the gas.
     * @return A Completable that completes when the update operation is finished.
     */
    public Completable execute(int slotNumber, boolean isEnabled) {
        if (slotNumber < 1 || slotNumber > 10) {
            return Completable.error(new IllegalArgumentException("Slot number must be between 1 and 10."));
        }
        return gasRepository.updateGasEnabledState(slotNumber, isEnabled);
    }
} 