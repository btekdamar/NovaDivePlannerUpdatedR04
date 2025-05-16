package com.burc.novadiveplannerupdated.domain.usecase.gas;

import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.repository.GasRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;

/**
 * Use case for saving a gas (inserting if new, or updating if existing).
 * If the gas name is not provided or is empty, it will be automatically generated.
 */
public class SaveGasUseCase {

    private final GasRepository gasRepository;
    private final GenerateGasNameUseCase generateGasNameUseCase;

    @Inject
    public SaveGasUseCase(GasRepository gasRepository, GenerateGasNameUseCase generateGasNameUseCase) {
        this.gasRepository = gasRepository;
        this.generateGasNameUseCase = generateGasNameUseCase;
    }

    /**
     * Executes the use case to save a gas.
     * If the gas name in the provided Gas object is null, empty, or blank,
     * a standard name will be generated based on its FO2 and FHe content.
     * Otherwise, the provided name will be used.
     *
     * @param gas The Gas domain entity to save.
     * @return A Completable that completes when the save operation is finished.
     */
    public Completable execute(Gas gas) {
        if (gas == null) {
            return Completable.error(new IllegalArgumentException("Gas to save cannot be null."));
        }

        Gas gasToSave;
        String currentName = gas.getGasName();

        // Check if the current name is null, empty, or just whitespace
        if (currentName == null || currentName.trim().isEmpty()) {
            String generatedName = generateGasNameUseCase.execute(gas);
            gasToSave = new Gas.Builder(gas).gasName(generatedName).build();
        } else {
            // Use the existing name if it's already provided and not blank
            gasToSave = gas;
        }

        return gasRepository.saveGas(gasToSave);
    }
} 