package com.burc.novadiveplannerupdated.domain.usecase.gas;

import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.repository.GasRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Use case for retrieving a stream of all gases.
 */
public class GetGasesUseCase {

    private final GasRepository gasRepository;

    @Inject
    public GetGasesUseCase(GasRepository gasRepository) {
        this.gasRepository = gasRepository;
    }

    /**
     * Executes the use case to get the stream of gases.
     *
     * @return A Flowable emitting a list of Gas domain entities.
     */
    public Flowable<List<Gas>> execute() {
        return gasRepository.getGasesStream();
    }
} 