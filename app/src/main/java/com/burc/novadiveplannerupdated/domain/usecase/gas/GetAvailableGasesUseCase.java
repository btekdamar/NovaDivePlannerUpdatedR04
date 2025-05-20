package com.burc.novadiveplannerupdated.domain.usecase.gas;

import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.repository.GasRepository; // Bu repository'nin zaten tanımlı olduğunu varsayıyorum (DataModule'da gördüm)

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

/**
 * Use case responsible for providing a list of available (enabled) gases.
 */
public class GetAvailableGasesUseCase {

    private final GasRepository gasRepository;

    @Inject
    public GetAvailableGasesUseCase(GasRepository gasRepository) {
        this.gasRepository = gasRepository;
    }

    /**
     * Executes the use case.
     *
     * @return A Single that emits a list of {@link Gas} objects that are currently enabled.
     *         The list might be empty if no gases are enabled.
     */
    public Single<List<Gas>> execute() {
        return gasRepository.getGasesStream() // GasRepository'den tüm gazları al (Flowable<List<Gas>> veya Observable<List<Gas>> dönebilir)
                .firstOrError() // İlk emisyonu al veya hata ver (Single'a dönüştür)
                .map(gasList -> gasList.stream()
                                      .filter(Gas::isEnabled) // Sadece 'enabled' olanları filtrele
                                      .collect(Collectors.toList()));
    }
} 