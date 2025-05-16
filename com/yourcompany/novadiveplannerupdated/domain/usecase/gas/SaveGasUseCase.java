package com.yourcompany.novadiveplannerupdated.domain.usecase.gas;

import com.yourcompany.novadiveplannerupdated.domain.entity.Gas;
import com.yourcompany.novadiveplannerupdated.domain.repository.GasRepository;
import javax.inject.Inject;
import io.reactivex.rxjava3.core.Completable;

public class SaveGasUseCase {

    private final GasRepository gasRepository;
    private final GenerateGasNameUseCase generateGasNameUseCase;

    @Inject
    public SaveGasUseCase(
            GasRepository gasRepository,
            GenerateGasNameUseCase generateGasNameUseCase
    ) {
        this.gasRepository = gasRepository;
        this.generateGasNameUseCase = generateGasNameUseCase;
    }

    public Completable execute(Gas gasToSave) {
        Gas gasForPersistence;
        String currentName = gasToSave.getGasName();

        // Check if the gas name is null or empty.
        // EditGasViewModel is expected to send gasToSave with gasName = null
        // if a new name should be generated based on FO2/FHe.
        if (currentName == null || currentName.trim().isEmpty()) {
            String generatedName = generateGasNameUseCase.execute(
                    gasToSave.getFo2(),
                    gasToSave.getFhe()
            );

            // Create a new Gas instance with the generated name.
            // This assumes Gas.Builder has a copy constructor like: new Gas.Builder(Gas existingGas)
            // or that Gas has a toBuilder() method.
            // If Gas.Builder(gasToSave) is not available, this part needs adjustment
            // based on how Gas objects are copied/modified.
            Gas.Builder builder;
            try {
                // Attempt to use a copy constructor for the builder
                builder = new Gas.Builder(gasToSave);
            } catch (NoSuchMethodError e) {
                // Fallback if copy constructor Gas.Builder(Gas) doesn't exist.
                // This requires Gas.Builder() to be public and then manually setting all fields.
                // This is less ideal and error-prone, a copy constructor or toBuilder() is preferred.
                builder = new Gas.Builder()
                    .slotNumber(gasToSave.getSlotNumber())
                    .isEnabled(gasToSave.isEnabled())
                    // gasName will be set with generatedName
                    .fo2(gasToSave.getFo2())
                    .fhe(gasToSave.getFhe())
                    .po2Max(gasToSave.getPo2Max())
                    .gasType(gasToSave.getGasType())
                    .tankCapacity(gasToSave.getTankCapacity())
                    .reservePressurePercentage(gasToSave.getReservePressurePercentage());
            }
            
            builder.gasName(generatedName);
            gasForPersistence = builder.build();

        } else {
            // If a name is already provided (and not empty), use the gas object as is.
            // This path would be taken if, in the future, there's a way to manually set names
            // and the ViewModel sends a non-null, non-empty name.
            gasForPersistence = gasToSave;
        }
        return gasRepository.saveGas(gasForPersistence);
    }
} 