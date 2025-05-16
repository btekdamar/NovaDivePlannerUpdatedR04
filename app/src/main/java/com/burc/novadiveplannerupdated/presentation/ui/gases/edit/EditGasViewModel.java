package com.burc.novadiveplannerupdated.presentation.ui.gases.edit;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.model.GasType;
import com.burc.novadiveplannerupdated.domain.usecase.gas.GetGasBySlotNumberUseCase;
import com.burc.novadiveplannerupdated.domain.usecase.gas.SaveGasUseCase;
import java.util.Locale;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import android.util.Log;

@HiltViewModel
public class EditGasViewModel extends ViewModel {
    private static final String TAG = "EditGasViewModel";
    public static final String ARG_SLOT_NUMBER = "slotNumber";

    private final GetGasBySlotNumberUseCase getGasBySlotNumberUseCase;
    private final SaveGasUseCase saveGasUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final BehaviorSubject<EditGasUiState> _uiState = BehaviorSubject.createDefault(EditGasUiState.initialState());
    public final Flowable<EditGasUiState> uiState = _uiState.toFlowable(BackpressureStrategy.LATEST);

    private Gas originalGas;
    // Temporary holders for values being edited, before saving
    private GasType currentGasType;
    private Double currentFo2;
    private Double currentFhe;
    private Double currentPo2Max;
    private int slotNumber = -1;

    @Inject
    public EditGasViewModel(
            SavedStateHandle savedStateHandle,
            GetGasBySlotNumberUseCase getGasBySlotNumberUseCase,
            SaveGasUseCase saveGasUseCase) {
        this.getGasBySlotNumberUseCase = getGasBySlotNumberUseCase;
        this.saveGasUseCase = saveGasUseCase;

        Integer slotNumArg = savedStateHandle.get(ARG_SLOT_NUMBER);
        if (slotNumArg == null) {
            Log.e(TAG, "Slot number argument is missing!");
            _uiState.onNext(EditGasUiState.initialState().copy(null, false, false, null, null, null, null, null, null, null, "Error: Slot number missing.", true));
            return;
        }
        this.slotNumber = slotNumArg;
        loadGasDetails(this.slotNumber);
    }

    private void loadGasDetails(int slotNum) {
        _uiState.onNext(_uiState.getValue().copy(String.format(Locale.getDefault(), "Edit Gas %d", slotNum), true, null, null, null, null, null, null, null, null, null, null));
        disposables.add(
            getGasBySlotNumberUseCase.execute(slotNum)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    gas -> {
                        originalGas = gas;
                        currentGasType = gas.getGasType();
                        currentFo2 = gas.getFo2();
                        currentFhe = gas.getFhe();
                        currentPo2Max = gas.getPo2Max();
                        _uiState.onNext(new EditGasUiState(
                                String.format(Locale.getDefault(), "Edit Gas %d", gas.getSlotNumber()),
                                false,
                                false,
                                formatGasType(gas.getGasType()),
                                formatFraction(gas.getFo2(), true),
                                formatFraction(gas.getFhe(), true),
                                formatPo2Max(gas.getPo2Max()),
                                null, null, null, null, false
                        ));
                    },
                    throwable -> {
                        Log.e(TAG, "Error loading gas details for slot: " + slotNum, throwable);
                        _uiState.onNext(_uiState.getValue().copy(null, false, null, null, null, null, null, null, null, null, "Error loading gas: " + throwable.getMessage(), null));
                    },
                    () -> {
                        // Should not happen if slot number is valid, as Maybe should emit or error.
                        // If it completes empty, it means gas not found for a valid slot (e.g. after deletion elsewhere)
                        Log.w(TAG, "Gas not found for slot: " + slotNum);
                        _uiState.onNext(_uiState.getValue().copy(null, false, null, null, null, null, null, null, null, null, "Gas not found.", null));
                    }
                )
        );
    }

    public void updateGasMode(GasType newMode) {
        currentGasType = newMode;
        EditGasUiState currentState = _uiState.getValue();
        if (currentState != null) {
            String po2MaxTextToUpdate = currentState.getPo2MaxText();
            if (newMode == GasType.CLOSED_CIRCUIT) {
                currentPo2Max = null; // Explicitly nullify for CC mode
                po2MaxTextToUpdate = "--";
            } else {
                // If switching back to OC, and originalGas had a PPO2, restore it or keep current if edited.
                // For simplicity, we might need to reload original PPO2 if user wants to revert.
                // Or, if currentPo2Max was already set (e.g. user switched OC->CC->OC), keep it.
                // If currentPo2Max is null (because it was just CC), then use originalGas's PPO2 or a default.
                if (currentPo2Max == null && originalGas != null) {
                    currentPo2Max = originalGas.getPo2Max(); // Restore from original if available
                }
                 // If currentPo2Max is still null (e.g. original was also null or CC), it will show as -- or an error later if OC needs it
                po2MaxTextToUpdate = formatPo2Max(currentPo2Max);
            }
            _uiState.onNext(currentState.copy(null, null, null, formatGasType(newMode), null, null, po2MaxTextToUpdate, null, null, null, null, null));
        }
    }

    public void updateFo2(double newFo2) {
        currentFo2 = newFo2;
        EditGasUiState currentState = _uiState.getValue();
        if (currentState != null) {
            _uiState.onNext(currentState.copy(null, null, null, null, formatFraction(newFo2, true), null, null, null, null, null, null, null));
        }
    }

    public void updateFhe(double newFhe) {
        currentFhe = newFhe;
        EditGasUiState currentState = _uiState.getValue();
        if (currentState != null) {
            _uiState.onNext(currentState.copy(null, null, null, null, null, formatFraction(newFhe, true), null, null, null, null, null, null));
        }
    }

    public void updatePo2Max(Double newPo2Max) {
        currentPo2Max = newPo2Max;
        EditGasUiState currentState = _uiState.getValue();
        if (currentState != null) {
            _uiState.onNext(currentState.copy(null, null, null, null, null, null, formatPo2Max(newPo2Max), null, null, null, null, null));
        }
    }


    public void onSaveClicked() {
        if (originalGas == null || currentFo2 == null || currentFhe == null || currentGasType == null) {
            _uiState.onNext(_uiState.getValue().copy(null, false, false, null, null, null, null, null, null, null, "Error: Original gas data missing or invalid.", false));
            return;
        }

        // Validation
        String fo2Error = validateFo2(currentFo2);
        String fheError = validateFhe(currentFhe);
        String sumError = validateFo2FheSum(currentFo2, currentFhe);
        String po2MaxError = (currentGasType == GasType.OPEN_CIRCUIT) ? validatePo2Max(currentPo2Max) : null;

        if (fo2Error != null || fheError != null || sumError != null || po2MaxError != null) {
            String finalFo2Error = fo2Error != null ? fo2Error : (sumError != null && currentFo2 != null ? sumError : null);
            String finalFheError = fheError != null ? fheError : (sumError != null && currentFhe != null ? sumError : null);
            _uiState.onNext(_uiState.getValue().copy(null, false, false, null, null, null, null, finalFo2Error, finalFheError, po2MaxError, null, false));
            return;
        }
        
        _uiState.onNext(_uiState.getValue().copy(null, false, true, null, null, null, null, null, null, null, null, false));

        // Always pass null for gasName to force SaveGasUseCase to (re)generate it based on new FO2/FHe
        // This assumes that if a user could set a custom name, that logic would be handled differently.
        // Since EditGasDialog currently does not allow setting a custom name, this approach is suitable.
        Gas updatedGas = new Gas.Builder(originalGas)
                .gasType(currentGasType)
                .fo2(currentFo2)
                .fhe(currentFhe)
                .po2Max(currentPo2Max)
                .gasName(null) // Force name regeneration
                .build();

        disposables.add(
            saveGasUseCase.execute(updatedGas)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    () -> _uiState.onNext(_uiState.getValue().copy(null, false, false, null, null, null, null, null, null, null, null, true)),
                    throwable -> {
                        Log.e(TAG, "Error saving gas", throwable);
                        _uiState.onNext(_uiState.getValue().copy(null, false, false, null, null, null, null, null, null, null, "Save failed: " + throwable.getMessage(), false));
                    }
                )
        );
    }

    public void onCancelClicked() {
        _uiState.onNext(_uiState.getValue().copy(null, null, null, null, null, null, null, null, null, null, null, true));
    }

    private String formatGasType(GasType type) {
        return type == GasType.OPEN_CIRCUIT ? "OC" : "CC";
    }

    private String formatFraction(Double value, boolean isPercentage) {
        if (value == null) return "--";
        if (isPercentage) {
            return String.format(Locale.US, "%.0f", value * 100); // No % sign, just number
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private String formatPo2Max(Double value) {
        if (value == null) return "--";
        return String.format(Locale.US, "%.2f", value);
    }

    // Validation Methods
    private String validateFo2(Double fo2) {
        if (fo2 == null) return "FO2 cannot be empty";
        if (fo2 < 0.07 || fo2 > 1.0) return "FO2 must be 7-100%";
        return null;
    }

    private String validateFhe(Double fhe) {
        if (fhe == null) return "FHe cannot be empty"; // Or allow 0 explicitly
        if (fhe < 0.0 || fhe > 0.93) return "FHe must be 0-93%";
        return null;
    }

    private String validateFo2FheSum(Double fo2, Double fhe) {
        if (fo2 == null || fhe == null) return null; // Handled by individual checks
        if (fo2 + fhe > 1.0) return "FO2 + FHe cannot exceed 100%";
        return null;
    }

    private String validatePo2Max(Double po2Max) {
        if (po2Max == null) return "PO2 Max cannot be empty for OC";
        if (po2Max < 0.1 || po2Max > 3.0) return "PO2 Max must be 0.1-3.0 ata"; // Example range
        return null;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
} 