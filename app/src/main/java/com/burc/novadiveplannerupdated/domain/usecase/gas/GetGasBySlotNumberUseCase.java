package com.burc.novadiveplannerupdated.domain.usecase.gas;

import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.repository.GasRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Maybe;

/**
 * Use case for retrieving a specific gas by its slot number.
 */
public class GetGasBySlotNumberUseCase {

    private final GasRepository gasRepository;

    @Inject
    public GetGasBySlotNumberUseCase(GasRepository gasRepository) {
        this.gasRepository = gasRepository;
    }

    /**
     * Executes the use case to get a specific gas by its slot number.
     *
     * @param slotNumber The slot number of the gas to retrieve.
     * @return A Maybe emitting the Gas domain entity if found, or completing without a value if not found.
     */
    public Maybe<Gas> execute(int slotNumber) {
        if (slotNumber < 1 || slotNumber > 10) {
            return Maybe.error(new IllegalArgumentException("Slot number must be between 1 and 10."));
        }
        return gasRepository.getGasBySlotNumber(slotNumber);
    }
} 