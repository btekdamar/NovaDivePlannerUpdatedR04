package com.burc.novadiveplannerupdated.presentation.ui.segments.edit;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import android.util.Log;

import com.burc.novadiveplannerupdated.R;
import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;
import com.burc.novadiveplannerupdated.domain.entity.Dive;
import com.burc.novadiveplannerupdated.domain.entity.DivePlan;
import com.burc.novadiveplannerupdated.domain.entity.DiveSegment;
import com.burc.novadiveplannerupdated.domain.entity.DiveSettings;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.model.GasType;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;
import com.burc.novadiveplannerupdated.domain.usecase.diveplan.AddSegmentToCurrentDiveUseCase;
import com.burc.novadiveplannerupdated.domain.usecase.diveplan.GetActiveDivePlanUseCase;
import com.burc.novadiveplannerupdated.domain.usecase.diveplan.UpdateSegmentInCurrentDiveUseCase;
import com.burc.novadiveplannerupdated.domain.usecase.gas.GetAvailableGasesUseCase;
import com.burc.novadiveplannerupdated.domain.usecase.settings.GetSettingsUseCase;
import com.burc.novadiveplannerupdated.domain.util.UnitConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.core.Completable;

@HiltViewModel
public class AddEditSegmentViewModel extends ViewModel {

    private static final String TAG = "AddEditSegmentVM";
    public static final String ARG_SEGMENT_NUMBER_TO_EDIT = "segmentNumberToEdit";

    private final GetActiveDivePlanUseCase getActiveDivePlanUseCase;
    private final AddSegmentToCurrentDiveUseCase addSegmentToCurrentDiveUseCase;
    private final UpdateSegmentInCurrentDiveUseCase updateSegmentInCurrentDiveUseCase;
    private final GetAvailableGasesUseCase getAvailableGasesUseCase;
    private final GetSettingsUseCase getSettingsUseCase;

    private final CompositeDisposable disposables = new CompositeDisposable();

    private final BehaviorSubject<AddEditSegmentUiState> _uiState = BehaviorSubject.createDefault(AddEditSegmentUiState.initialState());
    public final Flowable<AddEditSegmentUiState> uiState = _uiState.toFlowable(BackpressureStrategy.LATEST);

    // Internal state holders for current segment data being edited
    private DivePlan currentDivePlan;
    private DiveSegment segmentToEdit; // Original segment if in edit mode
    private boolean isEditMode = false;
    private int segmentNumberForOperation; // Either segmentToEdit.getSegmentNumber() or nextSegmentNumber

    // Temporary holders for values being edited before creating/updating UiState
    // These are usually updated by picker results or direct input change handlers
    private Integer currentSegmentTimeMinutes;
    private Double currentSegmentDepthNative; // Stored in native unit (ft or m based on current settings when loaded)
    private Double currentAscentRateNative;
    private Double currentDescentRateNative;
    private Gas currentSelectedGas;
    private Double currentSetPoint;
    private DiveSettings currentDiveSettings;
    private List<Gas> currentAvailableGases = new ArrayList<>();


    @Inject
    public AddEditSegmentViewModel(
            GetActiveDivePlanUseCase getActiveDivePlanUseCase,
            AddSegmentToCurrentDiveUseCase addSegmentToCurrentDiveUseCase,
            UpdateSegmentInCurrentDiveUseCase updateSegmentInCurrentDiveUseCase,
            GetAvailableGasesUseCase getAvailableGasesUseCase,
            GetSettingsUseCase getSettingsUseCase,
            SavedStateHandle savedStateHandle
    ) {
        this.getActiveDivePlanUseCase = getActiveDivePlanUseCase;
        this.addSegmentToCurrentDiveUseCase = addSegmentToCurrentDiveUseCase;
        this.updateSegmentInCurrentDiveUseCase = updateSegmentInCurrentDiveUseCase;
        this.getAvailableGasesUseCase = getAvailableGasesUseCase;
        this.getSettingsUseCase = getSettingsUseCase;

        Integer segmentNumberArg = savedStateHandle.get(ARG_SEGMENT_NUMBER_TO_EDIT);
        this.isEditMode = segmentNumberArg != null;
        if (isEditMode) {
            this.segmentNumberForOperation = segmentNumberArg;
        }
        // For add mode, segmentNumberForOperation will be determined after loading plan

        loadInitialData();
    }

    private void loadInitialData() {
        _uiState.onNext(_uiState.getValue().copy(null, true, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null));

        disposables.add(
                Single.zip(
                        getSettingsUseCase.execute().firstOrError(),
                        getAvailableGasesUseCase.execute(),
                        getActiveDivePlanUseCase.execute().firstOrError(),
                        InitialDataWrapper::new
                )
                .subscribeOn(Schedulers.io())
                .subscribe(
                        initialData -> {
                            currentDiveSettings = initialData.settings;
                            currentAvailableGases = initialData.gases != null ? initialData.gases : Collections.emptyList();
                            currentDivePlan = initialData.plan;

                            String dialogTitleText;
                            if (isEditMode) {
                                dialogTitleText = String.format(Locale.getDefault(), "Edit Segment %d", segmentNumberForOperation);
                                findSegmentAndInitializeFields(segmentNumberForOperation);
                            } else {
                                dialogTitleText = "Add New Segment";
                                initializeFieldsForNewSegment();
                            }
                            _uiState.onNext(buildCurrentUiState(dialogTitleText, false, false, null));
                        },
                        throwable -> {
                            Log.e(TAG, "Error loading initial data", throwable);
                            _uiState.onNext(_uiState.getValue().copy(null, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "Error loading initial data: " + throwable.getMessage(), null));
                        }
                )
        );
    }

    private void findSegmentAndInitializeFields(int segmentNumber) {
        if (currentDivePlan == null || currentDivePlan.getDives().isEmpty()) {
            Log.e(TAG, "Current dive plan is null or has no dives for editing segment.");
            currentSegmentTimeMinutes = (int) TimeUnit.SECONDS.toMinutes(DomainDefaults.DEFAULT_SEGMENT_DURATION_SECONDS);
            initializeDepthAndRatesWithDefaults(currentDiveSettings.getUnitSystem());
            currentSelectedGas = !currentAvailableGases.isEmpty() ? currentAvailableGases.get(0) : null;
            currentSetPoint = currentSelectedGas != null && currentSelectedGas.getGasType() == GasType.CLOSED_CIRCUIT ? DomainDefaults.DEFAULT_CC_SET_POINT : null;
            return;        }

        Dive currentDive = currentDivePlan.getDives().get(currentDivePlan.getDives().size() - 1);
        Optional<DiveSegment> foundSegmentOpt = currentDive.getSegments().stream()
                .filter(s -> s.getSegmentNumber() == segmentNumber)
                .findFirst();

        if (foundSegmentOpt.isPresent()) {
            segmentToEdit = foundSegmentOpt.get();
            currentSegmentTimeMinutes = (int) TimeUnit.SECONDS.toMinutes(segmentToEdit.getUserInputTotalDurationInSeconds());
            currentSelectedGas = segmentToEdit.getGas();
            currentSetPoint = segmentToEdit.getSetPoint();

            if (currentDiveSettings.getUnitSystem() == UnitSystem.METRIC) {
                currentSegmentDepthNative = UnitConverter.toMeters(segmentToEdit.getTargetDepth());
                currentAscentRateNative = UnitConverter.toMeters(segmentToEdit.getAscentRate());
                currentDescentRateNative = UnitConverter.toMeters(segmentToEdit.getDescentRate());
            } else { // IMPERIAL
                currentSegmentDepthNative = segmentToEdit.getTargetDepth();
                currentAscentRateNative = segmentToEdit.getAscentRate();
                currentDescentRateNative = segmentToEdit.getDescentRate();
            }
        } else {
            Log.e(TAG, "Segment " + segmentNumber + " not found in current dive. Initializing with defaults.");
            isEditMode = false; // Switch to add mode if segment not found
            initializeFieldsForNewSegment();
        }
    }

    private void initializeFieldsForNewSegment() {
        currentSegmentTimeMinutes = (int) TimeUnit.SECONDS.toMinutes(DomainDefaults.DEFAULT_SEGMENT_DURATION_SECONDS);
        initializeDepthAndRatesWithDefaults(currentDiveSettings.getUnitSystem());

        if (currentDivePlan != null && !currentDivePlan.getDives().isEmpty()) {
            Dive lastDive = currentDivePlan.getDives().get(currentDivePlan.getDives().size() - 1);
            if (!lastDive.getSegments().isEmpty()) {
                DiveSegment lastSegment = lastDive.getSegments().get(lastDive.getSegments().size() - 1);
                currentSelectedGas = lastSegment.getGas(); // Default to last segment's gas
                if (currentDiveSettings.getUnitSystem() == UnitSystem.METRIC) {
                    currentSegmentDepthNative = UnitConverter.toMeters(lastSegment.getTargetDepth());
                } else {
                    currentSegmentDepthNative = lastSegment.getTargetDepth();
                }
                currentSetPoint = lastSegment.getGas().getGasType() == GasType.CLOSED_CIRCUIT ? lastSegment.getSetPoint() : null;
            } else { // No segments in current dive yet
                currentSelectedGas = !currentAvailableGases.isEmpty() ? currentAvailableGases.get(0) : null;
                currentSetPoint = currentSelectedGas != null && currentSelectedGas.getGasType() == GasType.CLOSED_CIRCUIT ? DomainDefaults.DEFAULT_CC_SET_POINT : null;
            }
            segmentNumberForOperation = lastDive.getSegments().size() + 1;
        } else { // No dives in plan yet, or plan is null
            currentSelectedGas = !currentAvailableGases.isEmpty() ? currentAvailableGases.get(0) : null;
            currentSetPoint = currentSelectedGas != null && currentSelectedGas.getGasType() == GasType.CLOSED_CIRCUIT ? DomainDefaults.DEFAULT_CC_SET_POINT : null;
            segmentNumberForOperation = 1;
        }
    }

    private void initializeDepthAndRatesWithDefaults(UnitSystem unitSystem) {
        if (unitSystem == UnitSystem.METRIC) {
            currentSegmentDepthNative = UnitConverter.toMeters(DomainDefaults.DEFAULT_SEGMENT_DEPTH_FT);
            currentAscentRateNative = DomainDefaults.DEFAULT_ASCENT_RATE_M_MIN;
            currentDescentRateNative = DomainDefaults.DEFAULT_DESCENT_RATE_M_MIN;
        } else { // IMPERIAL
            currentSegmentDepthNative = DomainDefaults.DEFAULT_SEGMENT_DEPTH_FT;
            currentAscentRateNative = DomainDefaults.DEFAULT_ASCENT_RATE_FT_MIN;
            currentDescentRateNative = DomainDefaults.DEFAULT_DESCENT_RATE_FT_MIN;
        }
    }

    private AddEditSegmentUiState buildCurrentUiState(String titleOverride, boolean isLoading, boolean isSaving, String generalErrorOverride) {
        String depthUnit = currentDiveSettings.getUnitSystem() == UnitSystem.METRIC ? "m" : "ft";
        String rateUnit = currentDiveSettings.getUnitSystem() == UnitSystem.METRIC ? "m/min" : "ft/min";

        String timeStr = currentSegmentTimeMinutes != null ? String.format(Locale.getDefault(), "%d min", currentSegmentTimeMinutes) : "";
        String depthStr = currentSegmentDepthNative != null ? String.format(Locale.getDefault(), "%.0f %s", currentSegmentDepthNative, depthUnit) : "";
        String ascentRateStr = currentAscentRateNative != null ? String.format(Locale.getDefault(), "%.0f %s", currentAscentRateNative, rateUnit) : "";
        String descentRateStr = currentDescentRateNative != null ? String.format(Locale.getDefault(), "%.0f %s", currentDescentRateNative, rateUnit) : "";

        String gasStr = "";
        int initialGasIndex = 0;
        if (currentSelectedGas != null) {
            gasStr = String.format("%s (%s)", currentSelectedGas.getGasName(), currentSelectedGas.getGasType().getShortName());
            if (currentAvailableGases != null) {
                for (int i = 0; i < currentAvailableGases.size(); i++) {
                    if (currentAvailableGases.get(i).equals(currentSelectedGas)) {
                        initialGasIndex = i;
                        break;
                    }
                }
            }
        }

        boolean spVisible = currentSelectedGas != null && currentSelectedGas.getGasType() == GasType.CLOSED_CIRCUIT;
        String spStr = "--";
        if (spVisible && currentSetPoint != null) {
            spStr = String.format(Locale.getDefault(), "%.2f ata", currentSetPoint);
        }

        AddEditSegmentUiState currentState = _uiState.getValue();
        String title = titleOverride != null ? titleOverride : (currentState != null ? currentState.getDialogTitle() : "Segment");
        boolean advancedExpanded = currentState != null && currentState.isAdvancedSectionExpanded();
        String generalError = generalErrorOverride != null ? generalErrorOverride : (currentState != null ? currentState.getGeneralError() : null);

        return new AddEditSegmentUiState(
                title,
                isLoading,
                isSaving,
                timeStr, depthStr, ascentRateStr, descentRateStr, gasStr, spStr,
                spVisible,
                advancedExpanded,
                depthUnit,
                rateUnit,
                currentAvailableGases != null ? currentAvailableGases : Collections.emptyList(),
                initialGasIndex,
                currentState != null ? currentState.getTimeError() : null, // Preserve existing errors unless cleared
                currentState != null ? currentState.getDepthError() : null,
                currentState != null ? currentState.getAscentRateError() : null,
                currentState != null ? currentState.getDescentRateError() : null,
                currentState != null ? currentState.getGasError() : null,
                currentState != null ? currentState.getSetPointError() : null,
                generalError,
                false // dismissDialog is usually set by an action like save/cancel
        );
    }

    public void updateSegmentTime(int minutes) {
        currentSegmentTimeMinutes = minutes;
        _uiState.onNext(buildCurrentUiState(null, false, false, null)
                .copy(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null, null, null, null, null, null, null, null, null)); // Clear errors on value change
    }

    public void updateSegmentDepth(double depth) {
        currentSegmentDepthNative = depth;
        _uiState.onNext(buildCurrentUiState(null, false, false, null)
                .copy(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null, null, null, null, null, null, null, null, null));
    }

    public void updateAscentRate(double rate) {
        currentAscentRateNative = rate;
        _uiState.onNext(buildCurrentUiState(null, false, false, null)
                .copy(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null, null, null, null, null, null, null, null, null));
    }

    public void updateDescentRate(double rate) {
        currentDescentRateNative = rate;
        _uiState.onNext(buildCurrentUiState(null, false, false, null)
                .copy(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null, null, null, null, null, null, null, null, null));
    }

    public void updateSelectedGas(Gas gas) {
        currentSelectedGas = gas;
        if (gas != null && gas.getGasType() == GasType.CLOSED_CIRCUIT) {
            // If switching to CC, and SP is null or not suitable, set to default.
            // If it was already CC and SP was set, user might expect it to be kept.
            // For simplicity, always set to default SP for CC when gas changes to CC for now.
            currentSetPoint = DomainDefaults.DEFAULT_CC_SET_POINT;
        } else {
            currentSetPoint = null; // Clear SP for OC gases
        }
        _uiState.onNext(buildCurrentUiState(null, false, false, null)
                .copy(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null, null, null, null, null, null, null, null, null));
    }

    public void updateSetPoint(double sp) {
        currentSetPoint = sp;
        _uiState.onNext(buildCurrentUiState(null, false, false, null)
                .copy(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null, null, null, null, null, null, null, null, null));
    }

    public void toggleAdvancedSection() {
        AddEditSegmentUiState currentState = _uiState.getValue();
        if (currentState != null) {
            _uiState.onNext(currentState.copy(null, null, null, null, null, null, null, null, null, null, !currentState.isAdvancedSectionExpanded(), null, null, null, null, null, null, null, null, null, null, null, null));
        }
    }

    public void onSaveClicked() {
        _uiState.onNext(_uiState.getValue().copy(null, false, true, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, false));

        // Validation
        String timeError = validateTime(currentSegmentTimeMinutes);
        String depthError = validateDepth(currentSegmentDepthNative);
        String ascentRateError = validateRate(currentAscentRateNative, "Ascent");
        String descentRateError = validateRate(currentDescentRateNative, "Descent");
        String gasError = currentSelectedGas == null ? "Gas must be selected." : null;
        String spError = null;
        if (currentSelectedGas != null && currentSelectedGas.getGasType() == GasType.CLOSED_CIRCUIT) {
            spError = validateSetPoint(currentSetPoint);
        }

        if (timeError != null || depthError != null || ascentRateError != null || descentRateError != null || gasError != null || spError != null) {
            AddEditSegmentUiState errorState = _uiState.getValue().copy(null, false, false, null, null, null, null, null, null, null, null, null, null, null, null, timeError, depthError, ascentRateError, descentRateError, gasError, spError, null, false);
            _uiState.onNext(errorState);
            return;
        }

        // Convert to base units (ft, ft/min) for DiveSegment
        double depthInFeet;
        double ascentRateInFtMin;
        double descentRateInFtMin;

        if (currentDiveSettings.getUnitSystem() == UnitSystem.METRIC) {
            depthInFeet = UnitConverter.toFeet(currentSegmentDepthNative);
            ascentRateInFtMin = UnitConverter.toFeet(currentAscentRateNative);
            descentRateInFtMin = UnitConverter.toFeet(currentDescentRateNative);
        } else { // IMPERIAL
            depthInFeet = currentSegmentDepthNative;
            ascentRateInFtMin = currentAscentRateNative;
            descentRateInFtMin = currentDescentRateNative;
        }

        DiveSegment.Builder segmentBuilder = new DiveSegment.Builder();
        segmentBuilder.segmentNumber(segmentNumberForOperation)
                .targetDepth(depthInFeet)
                .userInputTotalDurationInSeconds(TimeUnit.MINUTES.toSeconds(currentSegmentTimeMinutes))
                .gas(currentSelectedGas)
                .ascentRate(ascentRateInFtMin)
                .descentRate(descentRateInFtMin);

        if (currentSelectedGas.getGasType() == GasType.CLOSED_CIRCUIT) {
            segmentBuilder.setPoint(currentSetPoint);
        } else {
            segmentBuilder.setPoint(0.0); // Or null, depending on domain model contract for OC SP.
        }

        Completable operation;
        DiveSegment segmentToSave = segmentBuilder.build();

        if (isEditMode) {
            operation = updateSegmentInCurrentDiveUseCase.execute(segmentToSave);
        } else {
            operation = addSegmentToCurrentDiveUseCase.execute(segmentToSave);
        }

        disposables.add(
                operation.subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> _uiState.onNext(_uiState.getValue().copy(null, false, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, true)), // dismissDialog = true
                                throwable -> {
                                    Log.e(TAG, "Error saving segment", throwable);
                                    _uiState.onNext(_uiState.getValue().copy(null, false, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "Save failed: " + throwable.getMessage(), false));
                                }
                        )
        );
    }

    public void onCancelClicked() {
        _uiState.onNext(_uiState.getValue().copy(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, true)); // dismissDialog = true
    }

    // Validation Methods (Simplified - add more robust checks as needed)
    private String validateTime(Integer minutes) {
        if (minutes == null || minutes <= 0) return "Time must be > 0 min.";
        if (minutes > 999) return "Time too long (max 999 min).";
        return null;
    }

    private String validateDepth(Double depth) {
        if (depth == null || depth < 0) return "Depth cannot be negative.";
        // Max depth checks might depend on unit system and practical limits
        if (currentDiveSettings.getUnitSystem() == UnitSystem.IMPERIAL && depth > DomainDefaults.MAX_DEPTH_FT_PLANNING) return String.format(Locale.getDefault(), "Depth too great (max %.0f ft).", DomainDefaults.MAX_DEPTH_FT_PLANNING);
        if (currentDiveSettings.getUnitSystem() == UnitSystem.METRIC && depth > UnitConverter.toMeters(DomainDefaults.MAX_DEPTH_FT_PLANNING)) return String.format(Locale.getDefault(), "Depth too great (max %.0f m).", UnitConverter.toMeters(DomainDefaults.MAX_DEPTH_FT_PLANNING));
        return null;
    }

    private String validateRate(Double rate, String type) {
        if (rate == null || rate <= 0) return type + " rate must be positive.";
        // Max rate checks
        if (currentDiveSettings.getUnitSystem() == UnitSystem.IMPERIAL) {
            if (type.equals("Ascent") && rate > DomainDefaults.MAX_ASCENT_RATE_FT_MIN) return String.format("Ascent rate too high (max %.0f ft/min)", DomainDefaults.MAX_ASCENT_RATE_FT_MIN);
            if (type.equals("Descent") && rate > DomainDefaults.MAX_DESCENT_RATE_FT_MIN) return String.format("Descent rate too high (max %.0f ft/min)", DomainDefaults.MAX_DESCENT_RATE_FT_MIN);
        }
        if (currentDiveSettings.getUnitSystem() == UnitSystem.METRIC) {
             if (type.equals("Ascent") && rate > DomainDefaults.MAX_ASCENT_RATE_M_MIN) return String.format("Ascent rate too high (max %.0f m/min)", DomainDefaults.MAX_ASCENT_RATE_M_MIN);
             if (type.equals("Descent") && rate > DomainDefaults.MAX_DESCENT_RATE_M_MIN) return String.format("Descent rate too high (max %.0f m/min)", DomainDefaults.MAX_DESCENT_RATE_M_MIN);
        }
        return null;
    }

    private String validateSetPoint(Double sp) {
        if (sp == null) return "Set point must be defined for CC gas.";
        if (sp < DomainDefaults.MIN_SET_POINT || sp > DomainDefaults.MAX_SET_POINT) {
            return String.format(Locale.getDefault(), "SP must be %.1f-%.1f ata.", DomainDefaults.MIN_SET_POINT, DomainDefaults.MAX_SET_POINT);
        }
        return null;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

    // Helper class for initial data loading
    private static class InitialDataWrapper {
        final DiveSettings settings;
        final List<Gas> gases;
        final DivePlan plan;

        InitialDataWrapper(DiveSettings settings, List<Gas> gases, DivePlan plan) {
            this.settings = settings;
            this.gases = gases;
            this.plan = plan;
        }
    }
} 