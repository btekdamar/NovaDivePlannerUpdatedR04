package com.burc.novadiveplannerupdated.presentation.ui.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.burc.novadiveplannerupdated.databinding.FragmentSettingsBinding;
import com.burc.novadiveplannerupdated.domain.entity.DiveSettings;
import com.burc.novadiveplannerupdated.domain.model.AlarmSettings;
import com.burc.novadiveplannerupdated.domain.model.GradientFactors;
import com.burc.novadiveplannerupdated.domain.model.SurfaceConsumptionRates;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;
import com.burc.novadiveplannerupdated.domain.model.AltitudeLevel;
import com.burc.novadiveplannerupdated.domain.model.LastStopDepthOption;
import com.burc.novadiveplannerupdated.presentation.ui.common.dialogs.ListPickerDialogFragment;
import com.burc.novadiveplannerupdated.presentation.ui.common.dialogs.NumberPickerDialogFragment;
import com.burc.novadiveplannerupdated.presentation.common.PresentationConstants;
import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;
import com.burc.novadiveplannerupdated.domain.util.UnitConverter;

import java.util.Arrays;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupObservers();
        setupClickListeners();
        setupFragmentResultListeners();
    }

    private void setupObservers() {
        viewModel.uiState.observe(getViewLifecycleOwner(), uiState -> {
            if (uiState == null) return;

            binding.progressBarSettings.setVisibility(uiState.isLoading() ? View.VISIBLE : View.GONE);

            if (uiState.getErrorMessage() != null && !uiState.getErrorMessage().isEmpty()) {
                Toast.makeText(getContext(), uiState.getErrorMessage(), Toast.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }

            if (!uiState.isLoading() && (uiState.getErrorMessage() == null || uiState.getErrorMessage().isEmpty())) {
                if (uiState.getDiveSettings() != null) {
                    updateUi(uiState);
                } else if (!uiState.isLoading()) {
                    Log.e("SettingsFragment", "UI State has null DiveSettings but not loading and no error.");
                }
            }
        });
    }

    private void updateUi(SettingsScreenUiState uiState) {
        if (uiState == null || uiState.getDiveSettings() == null) {
            Log.e("SettingsFragment", "updateUi called with null uiState or null DiveSettings");
            return;
        }

        DiveSettings diveSettings = uiState.getDiveSettings();

        binding.textViewUnitValue.setText(uiState.getUnitSystemDisplay());

        if (diveSettings.getAltitudeLevel() != null) {
            if (diveSettings.getUnitSystem() != null) {
                binding.textViewAltitudeValue.setText(uiState.getAltitudeLevelDisplay());
            } else {
                binding.textViewAltitudeValue.setText(diveSettings.getAltitudeLevel().getImperialDisplayName());
            }
        } else {
            binding.textViewAltitudeValue.setText("-");
        }

        if (diveSettings.getLastStopDepthOption() != null) {
            if (diveSettings.getUnitSystem() != null) {
                binding.textViewLastStopValue.setText(uiState.getLastStopDepthDisplay());
            } else {
                binding.textViewLastStopValue.setText(diveSettings.getLastStopDepthOption().getImperialDisplayName());
            }
        } else {
            binding.textViewLastStopValue.setText("-");
        }

        if (uiState.getGfLowConfig() != null) {
            binding.textViewGfLowValue.setText(uiState.getGfLowConfig().getDisplayedValueString());
        }
        if (uiState.getGfHighConfig() != null) {
            binding.textViewGfHighValue.setText(uiState.getGfHighConfig().getDisplayedValueString());
        }

        binding.checkboxEndAlarm.setChecked(uiState.isEndAlarmEnabled());
        binding.checkboxWobAlarm.setChecked(uiState.isWobAlarmEnabled());
        binding.checkboxOxygenNarcotic.setChecked(uiState.isOxygenNarcoticEnabled());

        binding.layoutEndThreshold.setEnabled(uiState.isEndAlarmEnabled());
        if (uiState.isEndAlarmEnabled() && uiState.getEndAlarmThresholdConfig() != null) {
            binding.textViewEndThresholdValue.setText(uiState.getEndAlarmThresholdConfig().getDisplayedValueString());
            binding.textViewEndThresholdValue.setAlpha(1f);
        } else {
            binding.textViewEndThresholdValue.setText("-");
            binding.textViewEndThresholdValue.setAlpha(0.5f);
        }

        binding.layoutWobThreshold.setEnabled(uiState.isWobAlarmEnabled());
        if (uiState.isWobAlarmEnabled() && uiState.getWobAlarmThresholdConfig() != null) {
            binding.textViewWobThresholdValue.setText(uiState.getWobAlarmThresholdConfig().getDisplayedValueString());
            binding.textViewWobThresholdValue.setAlpha(1f);
        } else {
            binding.textViewWobThresholdValue.setText("-");
            binding.textViewWobThresholdValue.setAlpha(0.5f);
        }

        if (uiState.getSacDiveConfig() != null) {
            binding.textViewSacDiveValue.setText(uiState.getSacDiveConfig().getDisplayedValueString());
        }
        if (uiState.getSacDecoConfig() != null) {
            binding.textViewSacDecoValue.setText(uiState.getSacDecoConfig().getDisplayedValueString());
        }
    }

    private void setupClickListeners() {
        binding.textViewUnitValue.setOnClickListener(v -> showUnitSystemPickerDialog());
        binding.layoutUnits.setOnClickListener(v -> showUnitSystemPickerDialog());

        binding.textViewAltitudeValue.setOnClickListener(v -> showAltitudePickerDialog());
        binding.layoutAltitude.setOnClickListener(v -> showAltitudePickerDialog());

        binding.textViewLastStopValue.setOnClickListener(v -> showLastStopDepthPickerDialog());
        binding.layoutLastStop.setOnClickListener(v -> showLastStopDepthPickerDialog());

        binding.layoutGfHigh.setOnClickListener(v -> showGfHighPickerDialog());
        binding.textViewGfHighValue.setOnClickListener(v -> showGfHighPickerDialog());

        binding.layoutGfLow.setOnClickListener(v -> showGfLowPickerDialog());
        binding.textViewGfLowValue.setOnClickListener(v -> showGfLowPickerDialog());

        binding.checkboxEndAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsScreenUiState currentUiState = viewModel.uiState.getValue();
            if (currentUiState == null || currentUiState.getDiveSettings() == null) {
                Log.e("SettingsFragment", "END Alarm checkbox changed but currentUiState or DiveSettings is null");
                return;
            }
            DiveSettings domainSettings = currentUiState.getDiveSettings();
            AlarmSettings currentAlarms = domainSettings.getAlarmSettings();

            int endThresholdImperialFt = (int) currentAlarms.getEndAlarmThresholdFt();
            if (isChecked && endThresholdImperialFt < DomainDefaults.MIN_END_ALARM_THRESHOLD_FT) {
                // Bu mantık ViewModel'e taşınabilir veya burada bırakılabilir.
                // Şimdilik, domaindeki değeri koruyalım, ViewModel'in map'lemesi bunu düzeltecektir.
            }

            viewModel.setLastUserSelection(PresentationConstants.REQUEST_KEY_END_THRESHOLD_PICKER, isChecked ? 1 : 0, domainSettings.getUnitSystem());
            viewModel.updateAlarmSettings(
                    isChecked,
                    endThresholdImperialFt,
                    currentAlarms.isWobAlarmEnabled(),
                    currentAlarms.getWobAlarmThresholdFt(),
                    currentAlarms.isOxygenNarcoticEnabled()
            );
        });

        binding.layoutEndThreshold.setOnClickListener(v -> showEndThresholdPickerDialog());

        binding.checkboxWobAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsScreenUiState currentUiState = viewModel.uiState.getValue();
            if (currentUiState == null || currentUiState.getDiveSettings() == null) {
                Log.e("SettingsFragment", "WOB Alarm checkbox changed but currentUiState or DiveSettings is null");
                return;
            }
            DiveSettings domainSettings = currentUiState.getDiveSettings();
            AlarmSettings currentAlarms = domainSettings.getAlarmSettings();
            int wobThresholdImperialFt = (int) currentAlarms.getWobAlarmThresholdFt();

            viewModel.setLastUserSelection(PresentationConstants.REQUEST_KEY_WOB_THRESHOLD_PICKER, isChecked ? 1:0, domainSettings.getUnitSystem());
            viewModel.updateAlarmSettings(
                    currentAlarms.isEndAlarmEnabled(),
                    currentAlarms.getEndAlarmThresholdFt(),
                    isChecked,
                    wobThresholdImperialFt,
                    currentAlarms.isOxygenNarcoticEnabled()
            );
        });

        binding.layoutWobThreshold.setOnClickListener(v -> showWobThresholdPickerDialog());

        binding.checkboxOxygenNarcotic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsScreenUiState currentUiState = viewModel.uiState.getValue();
            if (currentUiState == null || currentUiState.getDiveSettings() == null) {
                 Log.e("SettingsFragment", "O2 Narcotic checkbox changed but currentUiState or DiveSettings is null");
                return;
            }
            DiveSettings domainSettings = currentUiState.getDiveSettings();
            AlarmSettings currentAlarms = domainSettings.getAlarmSettings();

            viewModel.updateAlarmSettings(
                    currentAlarms.isEndAlarmEnabled(),
                    currentAlarms.getEndAlarmThresholdFt(),
                    currentAlarms.isWobAlarmEnabled(),
                    currentAlarms.getWobAlarmThresholdFt(),
                    isChecked
            );
        });

        binding.layoutSacDive.setOnClickListener(v -> showSacDivePickerDialog());
        binding.layoutSacDeco.setOnClickListener(v -> showSacDecoPickerDialog());
    }

    private void showUnitSystemPickerDialog() {
        SettingsScreenUiState uiState = viewModel.uiState.getValue();
        if (uiState == null || uiState.getDiveSettings() == null) return;

        UnitSystem[] unitSystems = UnitSystem.values();
        String[] displayValues = Arrays.stream(unitSystems)
                .map(UnitSystem::toString)
                .toArray(String[]::new);

        UnitSystem currentUnitSystem = uiState.getDiveSettings().getUnitSystem();
        int currentIndex = currentUnitSystem.ordinal();

        ListPickerDialogFragment dialog = ListPickerDialogFragment.newInstance(
                "Units",
                displayValues,
                currentIndex,
                PresentationConstants.REQUEST_KEY_UNITS_PICKER
        );
        dialog.show(getParentFragmentManager(), PresentationConstants.TAG_DIALOG_UNITS_PICKER);
    }

    private void showAltitudePickerDialog() {
        SettingsScreenUiState uiState = viewModel.uiState.getValue();
        if (uiState == null || uiState.getDiveSettings() == null || uiState.getDiveSettings().getUnitSystem() == null) return;

        UnitSystem currentDisplaySystem = uiState.getDiveSettings().getUnitSystem();
        AltitudeLevel[] altitudeLevels = AltitudeLevel.values();
        String[] displayValues = Arrays.stream(altitudeLevels)
                .map(level -> level.getDisplayString(currentDisplaySystem))
                .toArray(String[]::new);

        AltitudeLevel currentAltitudeLevel = uiState.getDiveSettings().getAltitudeLevel();
        int currentIndex = currentAltitudeLevel.ordinal();

        ListPickerDialogFragment dialog = ListPickerDialogFragment.newInstance(
                "Altitude Level",
                displayValues,
                currentIndex,
                PresentationConstants.REQUEST_KEY_ALTITUDE_PICKER
        );
        dialog.show(getParentFragmentManager(), PresentationConstants.TAG_DIALOG_ALTITUDE_PICKER);
    }

    private void showLastStopDepthPickerDialog() {
        SettingsScreenUiState uiState = viewModel.uiState.getValue();
        if (uiState == null || uiState.getDiveSettings() == null || uiState.getDiveSettings().getUnitSystem() == null) return;

        UnitSystem currentDisplaySystem = uiState.getDiveSettings().getUnitSystem();
        LastStopDepthOption[] lastStopDepthOptions = LastStopDepthOption.values();
        String[] displayValues = Arrays.stream(lastStopDepthOptions)
                .map(option -> option.getDisplayString(currentDisplaySystem))
                .toArray(String[]::new);

        LastStopDepthOption currentOption = uiState.getDiveSettings().getLastStopDepthOption();
        int currentIndex = currentOption.ordinal();

        ListPickerDialogFragment dialog = ListPickerDialogFragment.newInstance(
                "Last Stop Depth",
                displayValues,
                currentIndex,
                PresentationConstants.REQUEST_KEY_LAST_STOP_DEPTH_PICKER
        );
        dialog.show(getParentFragmentManager(), PresentationConstants.TAG_DIALOG_LAST_STOP_DEPTH_PICKER);
    }

    private void showGfHighPickerDialog() {
        SettingsScreenUiState uiState = viewModel.uiState.getValue();
        if (uiState == null || uiState.getGfHighConfig() == null) {
            if (uiState == null) Log.e("SettingsFragment", "showGfHighPickerDialog: uiState is null");
            else Log.e("SettingsFragment", "showGfHighPickerDialog: GfHighConfig is null");
            return;
        }
        SettingsScreenUiState.NumericSettingConfig config = uiState.getGfHighConfig();
        NumberPickerDialogFragment.newIntegerInstance(
                "GF High",
                PresentationConstants.REQUEST_KEY_GF_HIGH_PICKER,
                config.getCurrentPickerValue().intValue(),
                config.getMinPickerValue().intValue(),
                config.getMaxPickerValue().intValue(),
                config.getStepPickerValue().intValue(),
                config.getUnitSuffix()
        ).show(getParentFragmentManager(), PresentationConstants.TAG_DIALOG_GF_HIGH_PICKER);
    }

    private void showGfLowPickerDialog() {
        SettingsScreenUiState uiState = viewModel.uiState.getValue();
        if (uiState == null || uiState.getGfLowConfig() == null) {
             if (uiState == null) Log.e("SettingsFragment", "showGfLowPickerDialog: uiState is null");
            else Log.e("SettingsFragment", "showGfLowPickerDialog: GfLowConfig is null");
            return;
        }
        SettingsScreenUiState.NumericSettingConfig config = uiState.getGfLowConfig();
        NumberPickerDialogFragment.newIntegerInstance(
                "GF Low",
                PresentationConstants.REQUEST_KEY_GF_LOW_PICKER,
                config.getCurrentPickerValue().intValue(),
                config.getMinPickerValue().intValue(),
                config.getMaxPickerValue().intValue(),
                config.getStepPickerValue().intValue(),
                config.getUnitSuffix()
        ).show(getParentFragmentManager(), PresentationConstants.TAG_DIALOG_GF_LOW_PICKER);
    }

    private void showEndThresholdPickerDialog() {
        SettingsScreenUiState uiState = viewModel.uiState.getValue();
        if (uiState == null || !uiState.isEndAlarmEnabled() || uiState.getEndAlarmThresholdConfig() == null) {
            if (uiState != null && !uiState.isEndAlarmEnabled()){
                 Toast.makeText(getContext(), "Enable END Alarm to set threshold", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("SettingsFragment", "Cannot show END Threshold Picker: uiState="+uiState+", isEndAlarmEnabled="+(uiState != null && uiState.isEndAlarmEnabled())+", configNull="+(uiState != null && uiState.getEndAlarmThresholdConfig()==null) );
            }
            return;
        }
        SettingsScreenUiState.NumericSettingConfig config = uiState.getEndAlarmThresholdConfig();
        NumberPickerDialogFragment.newIntegerInstance(
                "END Threshold",
                PresentationConstants.REQUEST_KEY_END_THRESHOLD_PICKER,
                config.getCurrentPickerValue().intValue(),
                config.getMinPickerValue().intValue(),
                config.getMaxPickerValue().intValue(),
                config.getStepPickerValue().intValue(),
                config.getUnitSuffix()
        ).show(getParentFragmentManager(), PresentationConstants.TAG_DIALOG_END_THRESHOLD_PICKER);
    }

    private void showWobThresholdPickerDialog() {
        SettingsScreenUiState uiState = viewModel.uiState.getValue();
        if (uiState == null || !uiState.isWobAlarmEnabled() || uiState.getWobAlarmThresholdConfig() == null) {
            if (uiState != null && !uiState.isWobAlarmEnabled()){
                 Toast.makeText(getContext(), "Enable WOB Alarm to set threshold", Toast.LENGTH_SHORT).show();
            } else {
                 Log.e("SettingsFragment", "Cannot show WOB Threshold Picker: uiState="+uiState+", isWobAlarmEnabled="+(uiState != null && uiState.isWobAlarmEnabled())+", configNull="+(uiState != null && uiState.getWobAlarmThresholdConfig()==null) );
            }
            return;
        }
        SettingsScreenUiState.NumericSettingConfig config = uiState.getWobAlarmThresholdConfig();
        NumberPickerDialogFragment.newIntegerInstance(
                "WOB Threshold",
                PresentationConstants.REQUEST_KEY_WOB_THRESHOLD_PICKER,
                config.getCurrentPickerValue().intValue(),
                config.getMinPickerValue().intValue(),
                config.getMaxPickerValue().intValue(),
                config.getStepPickerValue().intValue(),
                config.getUnitSuffix()
        ).show(getParentFragmentManager(), PresentationConstants.TAG_DIALOG_WOB_THRESHOLD_PICKER);
    }

    private void showSacDivePickerDialog() {
        SettingsScreenUiState uiState = viewModel.uiState.getValue();
        if (uiState == null || uiState.getSacDiveConfig() == null) {
            if (uiState == null) Log.e("SettingsFragment", "showSacDivePickerDialog: uiState is null");
            else Log.e("SettingsFragment", "showSacDivePickerDialog: SacDiveConfig is null");
            return;
        }
        SettingsScreenUiState.NumericSettingConfig config = uiState.getSacDiveConfig();
        NumberPickerDialogFragment.newFloatInstance(
                "SAC Dive",
                PresentationConstants.REQUEST_KEY_SAC_DIVE_PICKER,
                config.getCurrentPickerValue().floatValue(),
                config.getMinPickerValue().floatValue(),
                config.getMaxPickerValue().floatValue(),
                config.getStepPickerValue().floatValue(),
                config.getUnitSuffix(),
                config.getDecimalFormatPattern(),
                config.getDecimalPlacesToRound()
        ).show(getParentFragmentManager(), PresentationConstants.TAG_DIALOG_SAC_DIVE_PICKER);
    }

    private void showSacDecoPickerDialog() {
        SettingsScreenUiState uiState = viewModel.uiState.getValue();
        if (uiState == null || uiState.getSacDecoConfig() == null) {
            if (uiState == null) Log.e("SettingsFragment", "showSacDecoPickerDialog: uiState is null");
            else Log.e("SettingsFragment", "showSacDecoPickerDialog: SacDecoConfig is null");
            return;
        }
        SettingsScreenUiState.NumericSettingConfig config = uiState.getSacDecoConfig();
        NumberPickerDialogFragment.newFloatInstance(
                "SAC Deco",
                PresentationConstants.REQUEST_KEY_SAC_DECO_PICKER,
                config.getCurrentPickerValue().floatValue(),
                config.getMinPickerValue().floatValue(),
                config.getMaxPickerValue().floatValue(),
                config.getStepPickerValue().floatValue(),
                config.getUnitSuffix(),
                config.getDecimalFormatPattern(),
                config.getDecimalPlacesToRound()
        ).show(getParentFragmentManager(), PresentationConstants.TAG_DIALOG_SAC_DECO_PICKER);
    }

    private void setupFragmentResultListeners() {
        getParentFragmentManager().setFragmentResultListener(PresentationConstants.REQUEST_KEY_UNITS_PICKER,
                this, (requestKey, bundle) -> {
                    SettingsScreenUiState currentUiState = viewModel.uiState.getValue();
                    if (currentUiState == null || currentUiState.getDiveSettings() == null) return;

                    int selectedIndex = bundle.getInt(ListPickerDialogFragment.RESULT_SELECTED_INDEX, -1);
                    if (selectedIndex != -1) {
                        UnitSystem selectedUnitSystem = UnitSystem.values()[selectedIndex];
                        if (currentUiState.getDiveSettings().getUnitSystem() != selectedUnitSystem) {
                            viewModel.updateUnitSystem(selectedUnitSystem);
                        }
                    }
                });

        getParentFragmentManager().setFragmentResultListener(PresentationConstants.REQUEST_KEY_ALTITUDE_PICKER,
                this, (requestKey, bundle) -> {
                    SettingsScreenUiState currentUiState = viewModel.uiState.getValue();
                    if (currentUiState == null || currentUiState.getDiveSettings() == null) return;

                    int selectedIndex = bundle.getInt(ListPickerDialogFragment.RESULT_SELECTED_INDEX, -1);
                    if (selectedIndex != -1) {
                        AltitudeLevel selectedAltitudeLevel = AltitudeLevel.values()[selectedIndex];
                        if (currentUiState.getDiveSettings().getAltitudeLevel() != selectedAltitudeLevel) {
                            viewModel.updateAltitudeLevel(selectedAltitudeLevel);
                        }
                    }
                });

        getParentFragmentManager().setFragmentResultListener(PresentationConstants.REQUEST_KEY_LAST_STOP_DEPTH_PICKER,
                this, (requestKey, bundle) -> {
                    SettingsScreenUiState currentUiState = viewModel.uiState.getValue();
                    if (currentUiState == null || currentUiState.getDiveSettings() == null) return;

                    int selectedIndex = bundle.getInt(ListPickerDialogFragment.RESULT_SELECTED_INDEX, -1);
                    if (selectedIndex != -1) {
                        LastStopDepthOption selectedOption = LastStopDepthOption.values()[selectedIndex];
                        if (currentUiState.getDiveSettings().getLastStopDepthOption() != selectedOption) {
                            viewModel.updateLastStopDepthOption(selectedOption);
                        }
                    }
                });

        getParentFragmentManager().setFragmentResultListener(PresentationConstants.REQUEST_KEY_GF_HIGH_PICKER,
                this, (requestKey, bundle) -> {
                    SettingsScreenUiState currentUiState = viewModel.uiState.getValue();
                    if (currentUiState == null || currentUiState.getDiveSettings() == null) return;

                    int newGfHigh = bundle.getInt(NumberPickerDialogFragment.RESULT_PICKER_1_VALUE, -1);
                    if (newGfHigh != -1) {
                        int currentGfLow = currentUiState.getDiveSettings().getGradientFactors().getGfLow();
                        viewModel.setLastUserSelection(requestKey, newGfHigh, UnitSystem.IMPERIAL);
                        try {
                            viewModel.updateGradientFactors(currentGfLow, newGfHigh);
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        getParentFragmentManager().setFragmentResultListener(PresentationConstants.REQUEST_KEY_GF_LOW_PICKER,
                this, (requestKey, bundle) -> {
                    SettingsScreenUiState currentUiState = viewModel.uiState.getValue();
                    if (currentUiState == null || currentUiState.getDiveSettings() == null) return;

                    int newGfLow = bundle.getInt(NumberPickerDialogFragment.RESULT_PICKER_1_VALUE, -1);
                    if (newGfLow != -1) {
                        int currentGfHigh = currentUiState.getDiveSettings().getGradientFactors().getGfHigh();
                        viewModel.setLastUserSelection(requestKey, newGfLow, UnitSystem.IMPERIAL);
                        try {
                            viewModel.updateGradientFactors(newGfLow, currentGfHigh);
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        getParentFragmentManager().setFragmentResultListener(PresentationConstants.REQUEST_KEY_END_THRESHOLD_PICKER,
                this, (requestKey, bundle) -> {
                    SettingsScreenUiState currentUiState = viewModel.uiState.getValue();
                    if (currentUiState == null || currentUiState.getDiveSettings() == null) return;
                    DiveSettings domainSettings = currentUiState.getDiveSettings();
                    UnitSystem displaySystem = domainSettings.getUnitSystem();

                    int newThresholdInDisplayUnit = bundle.getInt(NumberPickerDialogFragment.RESULT_PICKER_1_VALUE, -1);
                    if (newThresholdInDisplayUnit != -1) {
                        viewModel.setLastUserSelection(requestKey, newThresholdInDisplayUnit, displaySystem);

                        double endThresholdForStorage;
                        if (displaySystem == UnitSystem.METRIC) {
                            endThresholdForStorage = UnitConverter.convertMetersToPreciseFeet(newThresholdInDisplayUnit);
                        } else { // IMPERIAL
                            endThresholdForStorage = (double) newThresholdInDisplayUnit;
                        }

                        try {
                            // Mevcut WOB eşiğini ve diğer flag'leri koru
                            AlarmSettings currentAlarms = domainSettings.getAlarmSettings();
                            viewModel.updateAlarmSettings(
                                    currentAlarms.isEndAlarmEnabled(), // Bu, checkbox listener tarafından güncellenir
                                    endThresholdForStorage,            // Yeni güncellenen END eşiği
                                    currentAlarms.isWobAlarmEnabled(),
                                    currentAlarms.getWobAlarmThresholdFt(), // Mevcut WOB eşiği (double)
                                    currentAlarms.isOxygenNarcoticEnabled()
                            );
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        getParentFragmentManager().setFragmentResultListener(PresentationConstants.REQUEST_KEY_WOB_THRESHOLD_PICKER,
                this, (requestKey, bundle) -> {
                    SettingsScreenUiState currentUiState = viewModel.uiState.getValue();
                    if (currentUiState == null || currentUiState.getDiveSettings() == null) return;
                    DiveSettings domainSettings = currentUiState.getDiveSettings();
                    UnitSystem displaySystem = domainSettings.getUnitSystem();

                    int newThresholdInDisplayUnit = bundle.getInt(NumberPickerDialogFragment.RESULT_PICKER_1_VALUE, -1);
                    if (newThresholdInDisplayUnit != -1) {
                        viewModel.setLastUserSelection(requestKey, newThresholdInDisplayUnit, displaySystem);

                        double wobThresholdForStorage;
                        if (displaySystem == UnitSystem.METRIC) {
                            wobThresholdForStorage = UnitConverter.convertMetersToPreciseFeet(newThresholdInDisplayUnit);
                        } else { // IMPERIAL
                            wobThresholdForStorage = (double) newThresholdInDisplayUnit;
                        }
                        try {
                            // Mevcut END eşiğini ve diğer flag'leri koru
                            AlarmSettings currentAlarms = domainSettings.getAlarmSettings();
                            viewModel.updateAlarmSettings(
                                    currentAlarms.isEndAlarmEnabled(),
                                    currentAlarms.getEndAlarmThresholdFt(), // Mevcut END eşiği (double)
                                    currentAlarms.isWobAlarmEnabled(),      // Bu, checkbox listener tarafından güncellenir
                                    wobThresholdForStorage,                 // Yeni güncellenen WOB eşiği
                                    currentAlarms.isOxygenNarcoticEnabled()
                            );
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        getParentFragmentManager().setFragmentResultListener(PresentationConstants.REQUEST_KEY_SAC_DIVE_PICKER,
                this, (requestKey, bundle) -> {
                    SettingsScreenUiState currentUiState = viewModel.uiState.getValue();
                    if (currentUiState == null || currentUiState.getDiveSettings() == null) return;
                    DiveSettings domainSettings = currentUiState.getDiveSettings();
                    UnitSystem displaySystem = domainSettings.getUnitSystem();

                    float newSacInDisplayUnit = bundle.getFloat(NumberPickerDialogFragment.RESULT_PICKER_1_VALUE, -1f);
                    if (newSacInDisplayUnit != -1f) {
                        viewModel.setLastUserSelection(requestKey, newSacInDisplayUnit, displaySystem);

                        double sacDiveForStorage;
                        if (displaySystem == UnitSystem.METRIC) {
                            sacDiveForStorage = (double) UnitConverter.convertLitersPerMinuteToPreciseCuFt(newSacInDisplayUnit);
                        } else { // IMPERIAL
                            sacDiveForStorage = (double) newSacInDisplayUnit;
                        }
                        try {
                            // Mevcut Deco SAC değerini koru
                            SurfaceConsumptionRates currentRates = domainSettings.getSurfaceConsumptionRates();
                            viewModel.updateSurfaceConsumptionRates(
                                    sacDiveForStorage,                      // Yeni güncellenen Dalış SAC değeri
                                    currentRates.getRmvDecoCuFtMin()        // Mevcut Deko SAC değeri (double)
                            );
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        getParentFragmentManager().setFragmentResultListener(PresentationConstants.REQUEST_KEY_SAC_DECO_PICKER,
                this, (requestKey, bundle) -> {
                    SettingsScreenUiState currentUiState = viewModel.uiState.getValue();
                    if (currentUiState == null || currentUiState.getDiveSettings() == null) return;
                    DiveSettings domainSettings = currentUiState.getDiveSettings();
                    UnitSystem displaySystem = domainSettings.getUnitSystem();

                    float newSacInDisplayUnit = bundle.getFloat(NumberPickerDialogFragment.RESULT_PICKER_1_VALUE, -1f);
                    if (newSacInDisplayUnit != -1f) {
                        viewModel.setLastUserSelection(requestKey, newSacInDisplayUnit, displaySystem);

                        double sacDecoForStorage;
                        if (displaySystem == UnitSystem.METRIC) {
                            sacDecoForStorage = (double) UnitConverter.convertLitersPerMinuteToPreciseCuFt(newSacInDisplayUnit);
                        } else { // IMPERIAL
                            sacDecoForStorage = (double) newSacInDisplayUnit;
                        }
                        try {
                            // Mevcut Dalış SAC değerini koru
                            SurfaceConsumptionRates currentRates = domainSettings.getSurfaceConsumptionRates();
                            viewModel.updateSurfaceConsumptionRates(
                                    currentRates.getRmvDiveCuFtMin(),       // Mevcut Dalış SAC değeri (double)
                                    sacDecoForStorage                       // Yeni güncellenen Deko SAC değeri
                            );
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
