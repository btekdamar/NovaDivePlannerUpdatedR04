package com.burc.novadiveplannerupdated.presentation.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Log;

import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;
import com.burc.novadiveplannerupdated.domain.entity.DiveSettings;
import com.burc.novadiveplannerupdated.domain.model.AlarmSettings;
import com.burc.novadiveplannerupdated.domain.model.AltitudeLevel;
import com.burc.novadiveplannerupdated.domain.model.GradientFactors;
import com.burc.novadiveplannerupdated.domain.model.LastStopDepthOption;
import com.burc.novadiveplannerupdated.domain.model.SurfaceConsumptionRates;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;
import com.burc.novadiveplannerupdated.domain.usecase.settings.GetSettingsUseCase;
import com.burc.novadiveplannerupdated.domain.usecase.settings.SaveSettingsUseCase;
import com.burc.novadiveplannerupdated.domain.util.UnitConverter;
import com.burc.novadiveplannerupdated.presentation.common.PresentationConstants;

import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class SettingsViewModel extends ViewModel {

    private static final String TAG = "SettingsViewModel";

    private final GetSettingsUseCase getSettingsUseCase;
    private final SaveSettingsUseCase saveSettingsUseCase;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final MutableLiveData<SettingsScreenUiState> _uiState = new MutableLiveData<>();
    public LiveData<SettingsScreenUiState> uiState = _uiState;

    private DiveSettings currentSettings = new DiveSettings.Builder().build();

    private static class UserSelection {
        final String settingKey;
        final Number value;
        final UnitSystem unit;

        UserSelection(String settingKey, Number value, UnitSystem unit) {
            this.settingKey = settingKey;
            this.value = value;
            this.unit = unit;
        }
    }
    private UserSelection transientLastUserSelection;

    @Inject
    public SettingsViewModel(GetSettingsUseCase getSettingsUseCase, SaveSettingsUseCase saveSettingsUseCase) {
        this.getSettingsUseCase = getSettingsUseCase;
        this.saveSettingsUseCase = saveSettingsUseCase;
        loadSettings();
    }

    public void loadSettings() {
        _uiState.setValue(SettingsScreenUiState.loading());
        Log.d(TAG, "loadSettings called, setting UI state to loading.");
        compositeDisposable.add(
                getSettingsUseCase.execute()
                        .distinctUntilChanged()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                diveSettings -> {
                                    Log.d(TAG, "Settings loaded successfully: " + (diveSettings != null ? diveSettings.toString() : "null"));
                                    currentSettings = diveSettings != null ? diveSettings : new DiveSettings.Builder().build();
                                    // clearLastUserSelection(); // Fresh load from DB, clear any transient state
                                    _uiState.setValue(mapDiveSettingsToUiState(currentSettings));
                                },
                                throwable -> {
                                    Log.e(TAG, "Failed to load settings", throwable);
                                    _uiState.setValue(SettingsScreenUiState.error("Failed to load settings: " + throwable.getMessage()));
                                }
                        )
        );
    }

    private void saveSettingsInternal(DiveSettings settingsToSave) {
        if (settingsToSave == null) {
            Log.w(TAG, "No settings to save, settingsToSave is null.");
            // Update UI with current state but indicate error indirectly if needed, or rely on loadSettings to refresh
            _uiState.setValue(mapDiveSettingsToUiState(currentSettings)); // Show current state before potential error
            _uiState.setValue(SettingsScreenUiState.error("Attempted to save null settings.")); // Or a more specific error UI state update
            return;
        }
        Log.d(TAG, "saveSettingsInternal called for: " + settingsToSave.toString());

        // Indicate saving process by re-mapping current state with isLoading true, or a specific loading state if preferred
        // For simplicity, we'll rely on the GetSettingsUseCase Flowable to update the UI after save confirmation.
        // The immediate UI update comes from mapDiveSettingsToUiState before this save call.
        compositeDisposable.add(
                saveSettingsUseCase.execute(settingsToSave)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    Log.d(TAG, "Settings saved successfully. Waiting for Flowable to update UI.");
                                    // UI should auto-update due to GetSettingsUseCase emitting new DiveSettings
                                    // If not, a manual refresh might be needed, or ensure saveSettings triggers it.
                                    // For now, assume the Flowable from getSettingsUseCase handles the refresh.
                                },
                                throwable -> {
                                    Log.e(TAG, "Failed to save settings", throwable);
                                    // Revert to previous state or show error. Current approach re-maps for consistency.
                                    _uiState.setValue(mapDiveSettingsToUiState(currentSettings)); // Show current (unsaved) state
                                    _uiState.setValue(SettingsScreenUiState.error("Failed to save settings: " + throwable.getMessage()));
                                }
                        )
        );
    }

    private SettingsScreenUiState mapDiveSettingsToUiState(DiveSettings settings) {
        if (settings == null) { // Should ideally not happen if currentSettings is initialized
            Log.e(TAG, "mapDiveSettingsToUiState called with null DiveSettings");
            return SettingsScreenUiState.error("Internal error: Settings data missing.");
        }

        UnitSystem displaySystem = settings.getUnitSystem();

        String unitSystemDisplay = displaySystem.toString();
        String altitudeLevelDisplay = settings.getAltitudeLevel().getDisplayString(displaySystem);
        String lastStopDepthDisplay = settings.getLastStopDepthOption().getDisplayString(displaySystem);

        GradientFactors gf = settings.getGradientFactors();
        SettingsScreenUiState.NumericSettingConfig gfLowConfig = new SettingsScreenUiState.NumericSettingConfig(
                String.format(Locale.US, "%d%%", gf.getGfLow()),
                gf.getGfLow(), DomainDefaults.MIN_GF_VALUE, Math.min(DomainDefaults.MAX_GF_VALUE, gf.getGfHigh()), 1, PresentationConstants.UNIT_SUFFIX_PERCENT
        );
        SettingsScreenUiState.NumericSettingConfig gfHighConfig = new SettingsScreenUiState.NumericSettingConfig(
                String.format(Locale.US, "%d%%", gf.getGfHigh()),
                gf.getGfHigh(), Math.max(DomainDefaults.MIN_GF_VALUE, gf.getGfLow()), DomainDefaults.MAX_GF_VALUE, 1, PresentationConstants.UNIT_SUFFIX_PERCENT
        );

        AlarmSettings alarms = settings.getAlarmSettings();
        boolean isEndAlarmEnabled = alarms.isEndAlarmEnabled();
        boolean isWobAlarmEnabled = alarms.isWobAlarmEnabled();
        boolean isOxygenNarcoticEnabled = alarms.isOxygenNarcoticEnabled();
        SettingsScreenUiState.NumericSettingConfig endAlarmConfig = null;
        if (isEndAlarmEnabled) {
            endAlarmConfig = createDepthAlarmConfig("END_ALARM_THRESHOLD", alarms.getEndAlarmThresholdFt(), displaySystem,
                    DomainDefaults.MIN_END_ALARM_THRESHOLD_FT, DomainDefaults.MAX_END_ALARM_THRESHOLD_FT, PresentationConstants.ALARM_THRESHOLD_IMPERIAL_STEP_FT,
                    PresentationConstants.ALARM_THRESHOLD_METRIC_MIN_M, PresentationConstants.ALARM_THRESHOLD_METRIC_MAX_M, PresentationConstants.ALARM_THRESHOLD_METRIC_STEP_M);
        }

        SettingsScreenUiState.NumericSettingConfig wobAlarmConfig = null;
        if (isWobAlarmEnabled) {
            wobAlarmConfig = createDepthAlarmConfig("WOB_ALARM_THRESHOLD", alarms.getWobAlarmThresholdFt(), displaySystem,
                    DomainDefaults.MIN_WOB_ALARM_THRESHOLD_FT, DomainDefaults.MAX_WOB_ALARM_THRESHOLD_FT, PresentationConstants.ALARM_THRESHOLD_IMPERIAL_STEP_FT,
                    PresentationConstants.ALARM_THRESHOLD_METRIC_MIN_M, PresentationConstants.ALARM_THRESHOLD_METRIC_MAX_M, PresentationConstants.ALARM_THRESHOLD_METRIC_STEP_M);
        }

        SurfaceConsumptionRates scr = settings.getSurfaceConsumptionRates();
        SettingsScreenUiState.NumericSettingConfig sacDiveConfig = createSacConfig("SAC_DIVE", scr.getRmvDiveCuFtMin(), displaySystem);
        SettingsScreenUiState.NumericSettingConfig sacDecoConfig = createSacConfig("SAC_DECO", scr.getRmvDecoCuFtMin(), displaySystem);
        return SettingsScreenUiState.success(settings, unitSystemDisplay, altitudeLevelDisplay, lastStopDepthDisplay,
                gfLowConfig, gfHighConfig, isEndAlarmEnabled, endAlarmConfig, isWobAlarmEnabled, wobAlarmConfig, isOxygenNarcoticEnabled,
                sacDiveConfig, sacDecoConfig);
    }

    private SettingsScreenUiState.NumericSettingConfig createDepthAlarmConfig(
            String key, double imperialValueFtAsDouble, UnitSystem displaySystem,
            int imperialMinFt, int imperialMaxFt, int imperialStepFt,
            int metricMinM, int metricMaxM, int metricStepM) {
        Log.d(TAG, "createDepthAlarmConfig called for key: " + key + ", imperialValueFtAsDouble: " + imperialValueFtAsDouble + ", displaySystem: " + displaySystem);
        String displayedValueString;
        int currentPickerValue;
        int minPickerValue;
        int maxPickerValue;
        int stepPickerValue;
        String unitSuffix;

        if (transientLastUserSelection != null &&
            Objects.equals(transientLastUserSelection.settingKey, key) &&
            transientLastUserSelection.unit == displaySystem &&
            transientLastUserSelection.value instanceof Integer) {
            Log.d(TAG, "createDepthAlarmConfig - Using transientLastUserSelection for key: " + key + " value: " + transientLastUserSelection.value);
            currentPickerValue = (Integer) transientLastUserSelection.value;
            if (displaySystem == UnitSystem.METRIC) {
                displayedValueString = String.format(Locale.US, "%d %s", currentPickerValue, PresentationConstants.UNIT_SUFFIX_METERS);
                minPickerValue = metricMinM;
                maxPickerValue = metricMaxM;
                stepPickerValue = metricStepM;
                unitSuffix = PresentationConstants.UNIT_SUFFIX_METERS;
            } else { // IMPERIAL
                displayedValueString = String.format(Locale.US, "%d %s", currentPickerValue, PresentationConstants.UNIT_SUFFIX_FEET);
                minPickerValue = imperialMinFt;
                maxPickerValue = imperialMaxFt;
                stepPickerValue = imperialStepFt;
                unitSuffix = PresentationConstants.UNIT_SUFFIX_FEET;
            }
        } else {
            Log.d(TAG, "createDepthAlarmConfig - NOT using transientLastUserSelection for key: " + key + ". Converting from imperialValueFtAsDouble: " + imperialValueFtAsDouble);
            if (displaySystem == UnitSystem.METRIC) {
                currentPickerValue = UnitConverter.convertAndClampFeetToMetersForDisplay(imperialValueFtAsDouble, metricMinM, metricMaxM);
                displayedValueString = String.format(Locale.US, "%d %s", currentPickerValue, PresentationConstants.UNIT_SUFFIX_METERS);
                minPickerValue = metricMinM;
                maxPickerValue = metricMaxM;
                stepPickerValue = metricStepM;
                unitSuffix = PresentationConstants.UNIT_SUFFIX_METERS;
            } else { // IMPERIAL
                // For imperial display, ensure it's a multiple of step, then clamp.
                currentPickerValue = UnitConverter.roundToNearestMultiple(imperialValueFtAsDouble, imperialStepFt);
                currentPickerValue = Math.max(imperialMinFt, Math.min(imperialMaxFt, currentPickerValue));
                displayedValueString = String.format(Locale.US, "%d %s", currentPickerValue, PresentationConstants.UNIT_SUFFIX_FEET);
                minPickerValue = imperialMinFt;
                maxPickerValue = imperialMaxFt;
                stepPickerValue = imperialStepFt;
                unitSuffix = PresentationConstants.UNIT_SUFFIX_FEET;
            }
        }
        return new SettingsScreenUiState.NumericSettingConfig(displayedValueString, currentPickerValue, minPickerValue, maxPickerValue, stepPickerValue, unitSuffix);
    }

    private SettingsScreenUiState.NumericSettingConfig createSacConfig(String key, double imperialValueCuFtMin, UnitSystem displaySystem) {
        Log.d(TAG, "createSacConfig called for key: " + key + ", imperialValueCuFtMin: " + imperialValueCuFtMin + ", displaySystem: " + displaySystem);
        String displayedValueString;
        float currentPickerValue;
        float minPickerValue;
        float maxPickerValue;
        float stepPickerValue;
        String unitSuffix;
        String formatPattern = "%.2f";

        final float imperialMinCuFtMin = (float) DomainDefaults.MIN_RMV_CUFT_MIN;
        final float imperialMaxCuFtMin = (float) DomainDefaults.MAX_RMV_CUFT_MIN;
        final float imperialStepCuFtMin = PresentationConstants.RMV_IMPERIAL_STEP_CUFT_MIN;

        final float metricMinLMin = UnitConverter.roundToNearestMultiple((float) UnitConverter.convertCuFtToLiters(DomainDefaults.MIN_RMV_CUFT_MIN), PresentationConstants.RMV_METRIC_STEP_L_MIN);
        final float metricMaxLMin = UnitConverter.roundToNearestMultiple((float) UnitConverter.convertCuFtToLiters(DomainDefaults.MAX_RMV_CUFT_MIN), PresentationConstants.RMV_METRIC_STEP_L_MIN);
        final float metricStepLMin = PresentationConstants.RMV_METRIC_STEP_L_MIN;

        if (transientLastUserSelection != null &&
            Objects.equals(transientLastUserSelection.settingKey, key) &&
            transientLastUserSelection.unit == displaySystem &&
            transientLastUserSelection.value instanceof Float) {
            Log.d(TAG, "createSacConfig - Using transientLastUserSelection for key: " + key + " value: " + transientLastUserSelection.value);
            currentPickerValue = (Float) transientLastUserSelection.value;
            if (displaySystem == UnitSystem.METRIC) {
                displayedValueString = String.format(Locale.US, formatPattern + " %s", currentPickerValue, PresentationConstants.UNIT_SUFFIX_LITERS_MIN);
                minPickerValue = metricMinLMin;
                maxPickerValue = metricMaxLMin;
                stepPickerValue = metricStepLMin;
                unitSuffix = PresentationConstants.UNIT_SUFFIX_LITERS_MIN;
            } else { // IMPERIAL
                displayedValueString = String.format(Locale.US, formatPattern + " %s", currentPickerValue, PresentationConstants.UNIT_SUFFIX_CUFT_MIN);
                minPickerValue = imperialMinCuFtMin;
                maxPickerValue = imperialMaxCuFtMin;
                stepPickerValue = imperialStepCuFtMin;
                unitSuffix = PresentationConstants.UNIT_SUFFIX_CUFT_MIN;
            }
        } else {
            Log.d(TAG, "createSacConfig - NOT using transientLastUserSelection for key: " + key + ". Converting from imperialValueCuFtMin: " + imperialValueCuFtMin);
            if (displaySystem == UnitSystem.METRIC) {
                currentPickerValue = UnitConverter.convertAndClampCuFtToLitersForDisplay(imperialValueCuFtMin, metricMinLMin, metricMaxLMin, metricStepLMin);
                displayedValueString = String.format(Locale.US, formatPattern + " %s", currentPickerValue, PresentationConstants.UNIT_SUFFIX_LITERS_MIN);
                minPickerValue = metricMinLMin;
                maxPickerValue = metricMaxLMin;
                stepPickerValue = metricStepLMin;
                unitSuffix = PresentationConstants.UNIT_SUFFIX_LITERS_MIN;
            } else { // IMPERIAL
                currentPickerValue = UnitConverter.convertAndClampLitersToCuFtForDisplay(UnitConverter.convertCuFtToLiters(imperialValueCuFtMin), imperialMinCuFtMin, imperialMaxCuFtMin, imperialStepCuFtMin); // First convert to liters then clamp to cuft display
                // More direct approach for imperial:
                currentPickerValue = UnitConverter.roundToNearestMultiple((float)imperialValueCuFtMin, imperialStepCuFtMin);
                currentPickerValue = Math.max(imperialMinCuFtMin, Math.min(imperialMaxCuFtMin, currentPickerValue));

                displayedValueString = String.format(Locale.US, formatPattern + " %s", currentPickerValue, PresentationConstants.UNIT_SUFFIX_CUFT_MIN);
                minPickerValue = imperialMinCuFtMin;
                maxPickerValue = imperialMaxCuFtMin;
                stepPickerValue = imperialStepCuFtMin;
                unitSuffix = PresentationConstants.UNIT_SUFFIX_CUFT_MIN;
            }
        }
        return new SettingsScreenUiState.NumericSettingConfig(displayedValueString, currentPickerValue, minPickerValue, maxPickerValue, stepPickerValue, unitSuffix, formatPattern, 2);
    }

    // --- Methods to receive updates from Fragment ---
    // These methods expect IMPERIAL values if the setting is unit-dependent.
    // Fragment is responsible for conversion from display units if necessary.
    // Fragment also calls setLastUserSelection before calling these.
    public void setLastUserSelection(String key, Number value, UnitSystem unit) {
        this.transientLastUserSelection = new UserSelection(key, value, unit);
        Log.d(TAG, "setLastUserSelection: key=" + key + ", value=" + value + ", unit=" + unit);
    }

    public void clearLastUserSelection() {
        this.transientLastUserSelection = null;
        Log.d(TAG, "clearLastUserSelection called.");
    }

    public void updateUnitSystem(UnitSystem newSystem) {
        if (currentSettings == null || currentSettings.getUnitSystem() == newSystem) return;
        DiveSettings updated = new DiveSettings.Builder(currentSettings).unitSystem(newSystem).build();
        currentSettings = updated;
        clearLastUserSelection(); // Crucial: unit system change invalidates prior user selections
        _uiState.setValue(mapDiveSettingsToUiState(currentSettings));
        saveSettingsInternal(currentSettings);
    }

    public void updateAltitudeLevel(AltitudeLevel newAltitude) {
        if (currentSettings == null || currentSettings.getAltitudeLevel() == newAltitude) return;
        DiveSettings updated = new DiveSettings.Builder(currentSettings).altitudeLevel(newAltitude).build();
        currentSettings = updated;
        _uiState.setValue(mapDiveSettingsToUiState(currentSettings));
        saveSettingsInternal(currentSettings);
    }

    public void updateLastStopDepthOption(LastStopDepthOption newStopDepth) {
        if (currentSettings == null || currentSettings.getLastStopDepthOption() == newStopDepth) return;
        DiveSettings updated = new DiveSettings.Builder(currentSettings).lastStopDepthOption(newStopDepth).build();
        currentSettings = updated;
        _uiState.setValue(mapDiveSettingsToUiState(currentSettings));
        saveSettingsInternal(currentSettings);
    }

    public void updateGradientFactors(int gfLowImperial, int gfHighImperial) {
        if (currentSettings == null) return;
        try {
            GradientFactors newGf = new GradientFactors(gfLowImperial, gfHighImperial);
            if (currentSettings.getGradientFactors().equals(newGf)) return;
            DiveSettings updated = new DiveSettings.Builder(currentSettings).gradientFactors(newGf).build();
            currentSettings = updated;
            _uiState.setValue(mapDiveSettingsToUiState(currentSettings));
            saveSettingsInternal(currentSettings);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error updating GF: " + e.getMessage());
            _uiState.setValue(SettingsScreenUiState.error("Invalid GF values: " + e.getMessage()));
        }
    }

    public void updateAlarmSettings(boolean endEnabled, double newEndThresholdFt,
                                  boolean wobEnabled, double newWobThresholdFt,
                                  boolean o2Narcotic) {
        if (currentSettings == null) return;
        try {
            AlarmSettings newAlarms = new AlarmSettings(endEnabled, newEndThresholdFt, wobEnabled, newWobThresholdFt, o2Narcotic);
            if (currentSettings.getAlarmSettings().equals(newAlarms)) return;
            DiveSettings updated = new DiveSettings.Builder(currentSettings).alarmSettings(newAlarms).build();
            currentSettings = updated;
            _uiState.setValue(mapDiveSettingsToUiState(currentSettings));
            saveSettingsInternal(currentSettings);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error updating Alarms: " + e.getMessage());
            _uiState.setValue(SettingsScreenUiState.error("Invalid Alarm values: " + e.getMessage()));
        }
    }

    public void updateSurfaceConsumptionRates(double rmvDiveImperialCuFtMin, double rmvDecoImperialCuFtMin) {
        if (currentSettings == null) return;
        try {
            SurfaceConsumptionRates newRates = new SurfaceConsumptionRates(rmvDiveImperialCuFtMin, rmvDecoImperialCuFtMin);
            if (currentSettings.getSurfaceConsumptionRates().equals(newRates)) return;
            DiveSettings updated = new DiveSettings.Builder(currentSettings).surfaceConsumptionRates(newRates).build();
            currentSettings = updated;
            _uiState.setValue(mapDiveSettingsToUiState(currentSettings));
            saveSettingsInternal(currentSettings);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error updating SCR: " + e.getMessage());
            _uiState.setValue(SettingsScreenUiState.error("Invalid RMV values: " + e.getMessage()));
        }
    }

    public void clearErrorMessage() {
        if (_uiState.getValue() != null && _uiState.getValue().getErrorMessage() != null) {
            if (currentSettings != null) {
                _uiState.setValue(mapDiveSettingsToUiState(currentSettings)); // Revert to success state with mapped data
            } else { // Should not happen if loadSettings populates currentSettings
                loadSettings(); // Attempt to reload if current settings are somehow null
            }
        }
    }

    // Getter for current settings, primarily for Fragment to read for initial picker values or direct display
    // if not using the full mapped UiState for some reason (though UiState should be preferred).
    public DiveSettings getDomainDiveSettings() {
        return currentSettings;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
} 