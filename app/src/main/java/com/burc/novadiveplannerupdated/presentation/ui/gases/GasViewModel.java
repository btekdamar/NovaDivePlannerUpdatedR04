package com.burc.novadiveplannerupdated.presentation.ui.gases;

import androidx.lifecycle.ViewModel;
import com.burc.novadiveplannerupdated.domain.entity.DiveSettings;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.model.GasProperties;
import com.burc.novadiveplannerupdated.domain.model.GasType;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;
import com.burc.novadiveplannerupdated.domain.usecase.gas.CalculateGasPropertiesUseCase;
import com.burc.novadiveplannerupdated.domain.usecase.gas.GetGasesUseCase;
import com.burc.novadiveplannerupdated.domain.usecase.gas.UpdateGasEnabledStateUseCase;
import com.burc.novadiveplannerupdated.domain.usecase.settings.GetSettingsUseCase;
import com.burc.novadiveplannerupdated.presentation.ui.gases.state.GasRowDisplayData;
import com.burc.novadiveplannerupdated.presentation.ui.gases.state.GasScreenUiState;
import com.burc.novadiveplannerupdated.domain.common.DomainDefaults; // For default unit system

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import android.util.Log;


@HiltViewModel
public class GasViewModel extends ViewModel {
    private static final String TAG = "GasViewModel";

    private final GetGasesUseCase getGasesUseCase;
    private final GetSettingsUseCase getSettingsUseCase;
    private final CalculateGasPropertiesUseCase calculateGasPropertiesUseCase;
    private final UpdateGasEnabledStateUseCase updateGasEnabledStateUseCase;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final BehaviorSubject<GasScreenUiState> _uiState = BehaviorSubject.create();
    public final Flowable<GasScreenUiState> uiState = _uiState.hide().toFlowable(BackpressureStrategy.LATEST);

    // TODO: Add Navigation events (e.g., SingleLiveEvent for editing a gas)

    @Inject
    public GasViewModel(
            GetGasesUseCase getGasesUseCase,
            GetSettingsUseCase getSettingsUseCase,
            CalculateGasPropertiesUseCase calculateGasPropertiesUseCase,
            UpdateGasEnabledStateUseCase updateGasEnabledStateUseCase
    ) {
        this.getGasesUseCase = getGasesUseCase;
        this.getSettingsUseCase = getSettingsUseCase;
        this.calculateGasPropertiesUseCase = calculateGasPropertiesUseCase;
        this.updateGasEnabledStateUseCase = updateGasEnabledStateUseCase;

        // Initialize with a default loading state
        // Note: DomainDefaults.DEFAULT_UNIT_SYSTEM might not be available at this exact point if it also comes from settings.
        // If GetSettingsUseCase provides a guaranteed initial emission or a default, use that.
        // For simplicity, using a direct default now.
        _uiState.onNext(GasScreenUiState.initialState(DomainDefaults.DEFAULT_UNIT_SYSTEM)); 
        loadGasesAndSettings();
    }

    private void loadGasesAndSettings() {
        Log.d(TAG, "loadGasesAndSettings called");
        compositeDisposable.add(
            Flowable.combineLatest(
                getGasesUseCase.execute().doOnNext(gases -> Log.d(TAG, "Gases received: " + gases.size())),
                getSettingsUseCase.execute().doOnNext(settings -> Log.d(TAG, "Settings received: " + settings.getUnitSystem())),
                (BiFunction<List<Gas>, DiveSettings, GasScreenUiState>) (gases, settings) -> {
                    Log.d(TAG, "Combining gases and settings. Gas count: " + gases.size());
                    List<GasRowDisplayData> displayDataList = new ArrayList<>();
                    for (Gas gas : gases) {
                        GasProperties properties = calculateGasPropertiesUseCase.execute(gas, settings);
                        displayDataList.add(mapToDisplayData(gas, properties, settings.getUnitSystem()));
                    }
                    Log.d(TAG, "Successfully mapped to display data list. Size: " + displayDataList.size());
                    return new GasScreenUiState(false, null, displayDataList, settings.getUnitSystem());
                }
            )
            .subscribeOn(Schedulers.io()) // Calculations and data fetching on IO thread
            // .observeOn(AndroidSchedulers.mainThread()) // UI updates on main thread - usually handled by Fragment/Activity observing
            .doOnError(throwable -> Log.e(TAG, "Error in combineLatest stream", throwable))
            .onErrorReturn(throwable -> {
                Log.e(TAG, "Error occurred, returning error state", throwable);
                // Preserve current unit system if possible, or use default
                UnitSystem currentSystem = _uiState.getValue() != null ? _uiState.getValue().getCurrentUnitSystem() : DomainDefaults.DEFAULT_UNIT_SYSTEM;
                return new GasScreenUiState(false, throwable.getMessage(), new ArrayList<>(), currentSystem);
            })
            .subscribe(
                state -> {
                    Log.d(TAG, "New GasScreenUiState to be emitted: isLoading=" + state.isLoading() + ", gasListSize=" + state.getGasList().size());
                     _uiState.onNext(state);
                },
                throwable -> {
                    // This should ideally be caught by onErrorReturn, but as a safeguard:
                    Log.e(TAG, "Unhandled error in subscribe", throwable);
                     UnitSystem currentSystem = _uiState.getValue() != null ? _uiState.getValue().getCurrentUnitSystem() : DomainDefaults.DEFAULT_UNIT_SYSTEM;
                    _uiState.onNext(new GasScreenUiState(false, "Unhandled: " + throwable.getMessage(), new ArrayList<>(), currentSystem));
                }
            )
        );
    }

    public void onGasEnabledChanged(int slotNumber, boolean isEnabled) {
        Log.d(TAG, "onGasEnabledChanged called for slot: " + slotNumber + ", isEnabled: " + isEnabled);
        compositeDisposable.add(
            updateGasEnabledStateUseCase.execute(slotNumber, isEnabled)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    () -> Log.d(TAG, "Gas enabled state updated successfully for slot: " + slotNumber),
                    throwable -> {
                        Log.e(TAG, "Error updating gas enabled state for slot: " + slotNumber, throwable);
                        // TODO: Propagate this error to UI, e.g., via a SingleLiveEvent or by updating errorMessage in UiState
                        // For now, just logging. A more robust solution would be to have a temporary error message state.
                         _uiState.onNext(_uiState.getValue().copy(null, "Error updating gas " + slotNumber , null, null));
                    }
                )
        );
    }
    
    // TODO: public void onEditGasClicked(int slotNumber) { ... }

    private GasRowDisplayData mapToDisplayData(Gas gas, GasProperties properties, UnitSystem unitSystem) {
        // TODO: Implement proper String formatting and unit conversion based on UnitSystem
        // This is a placeholder implementation.
        String modText = properties.getMod() != null ? String.format(Locale.US, "%.0f %s", properties.getMod(), unitSystem == UnitSystem.METRIC ? "m" : "ft") : "--";
        String htText = properties.getHt() != null ? String.format(Locale.US, "%.0f %s", properties.getHt(), unitSystem == UnitSystem.METRIC ? "m" : "ft") : "--";
        String endLimitText = properties.getEndLimit() != null ? String.format(Locale.US, "%.0f %s", properties.getEndLimit(), unitSystem == UnitSystem.METRIC ? "m" : "ft") : "--";
        String wobLimitText = properties.getWobLimit() != null ? String.format(Locale.US, "%.0f %s", properties.getWobLimit(), unitSystem == UnitSystem.METRIC ? "m" : "ft") : "--";
        String ppo2MaxText = gas.getPo2Max() != null ? String.format(Locale.US, "%.2f ata", gas.getPo2Max()) : "--";
        String tankCapText = gas.getTankCapacity() > 0 ? String.format(Locale.US, "%.1f %s", gas.getTankCapacity(), unitSystem == UnitSystem.METRIC ? "L" : "cf") : "-";
        String reserveText = gas.getReservePressurePercentage() > 0 ? String.format(Locale.US, "%.0f%%", gas.getReservePressurePercentage()) : "-";

        return new GasRowDisplayData(
            gas.getSlotNumber(),
            gas.isEnabled(),
            gas.getGasName(), // User defined or previously generated
            properties.getCalculatedGasName(), // Standard name
            String.format(Locale.US, "%.0f%%", gas.getFo2() * 100),
            String.format(Locale.US, "%.0f%%", gas.getFhe() * 100),
            ppo2MaxText,
            modText,
            htText,
            endLimitText,
            wobLimitText,
            gas.getGasType() == GasType.OPEN_CIRCUIT ? "OC" : "CC",
            tankCapText,
            reserveText
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "onCleared called, disposing of disposables.");
        compositeDisposable.clear(); // Dispose all RxJava subscriptions
    }
} 