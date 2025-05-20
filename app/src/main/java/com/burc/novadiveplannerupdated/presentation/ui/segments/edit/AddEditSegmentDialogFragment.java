package com.burc.novadiveplannerupdated.presentation.ui.segments.edit;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.burc.novadiveplannerupdated.R;
import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;
import com.burc.novadiveplannerupdated.domain.util.UnitConverter;
import com.burc.novadiveplannerupdated.presentation.ui.common.dialogs.ListPickerDialogFragment;
import com.burc.novadiveplannerupdated.presentation.ui.common.dialogs.NumberPickerDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@AndroidEntryPoint
public class AddEditSegmentDialogFragment extends DialogFragment {

    private static final String TAG = "AddEditSegmentDialog";
    private AddEditSegmentViewModel viewModel;
    private final CompositeDisposable disposables = new CompositeDisposable();

    // View Binding later, for now findViewById
    private TextView textViewDialogTitle;
    private AppCompatTextView textViewTimeValue;
    private AppCompatTextView textViewDepthValue;
    private AppCompatTextView textViewAscentRateValue;
    private AppCompatTextView textViewDescentRateValue;
    private AppCompatTextView textViewGasValue;
    private LinearLayout layoutSetPointSection;
    private AppCompatTextView textViewSetPointValue;
    private LinearLayout layoutAdvancedToggle;
    private ImageView imageViewAdvancedArrow;
    private LinearLayout layoutAdvancedContent;
    private TextView textViewError;
    private MaterialButton buttonCancel;
    private MaterialButton buttonSave;

    // Request keys for pickers
    private static final String TIME_PICKER_REQUEST_KEY = "timePickerRequestKey_AddEditSegment";
    private static final String DEPTH_PICKER_REQUEST_KEY = "depthPickerRequestKey_AddEditSegment";
    private static final String ASCENT_RATE_PICKER_REQUEST_KEY = "ascentRatePickerRequestKey_AddEditSegment";
    private static final String DESCENT_RATE_PICKER_REQUEST_KEY = "descentRatePickerRequestKey_AddEditSegment";
    private static final String SP_PICKER_REQUEST_KEY = "spPickerRequestKey_AddEditSegment";
    private static final String GAS_PICKER_REQUEST_KEY = "gasPickerRequestKey_AddEditSegment";

    // Temporary storage for picker default values, derived from UiState when picker is launched
    private int currentPickerTimeMinutes;
    private double currentPickerDepthNative;
    private double currentPickerAscentRateNative;
    private double currentPickerDescentRateNative;
    private double currentPickerSetPoint;
    private int currentPickerGasIndex;
    private UnitSystem currentUnitSystemForPickers;
    private List<Gas> currentAvailableGasesForPicker;

    public static AddEditSegmentDialogFragment newInstance(Integer segmentNumberToEdit) {
        AddEditSegmentDialogFragment fragment = new AddEditSegmentDialogFragment();
        Bundle args = new Bundle();
        if (segmentNumberToEdit != null) {
            args.putInt(AddEditSegmentViewModel.ARG_SEGMENT_NUMBER_TO_EDIT, segmentNumberToEdit);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddEditSegmentViewModel.class);
        setupFragmentResultListeners();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_edit_segment, container, false);
        bindViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
        observeViewModel();
    }

    private void bindViews(View view) {
        textViewDialogTitle = view.findViewById(R.id.textViewDialogTitle);
        textViewTimeValue = view.findViewById(R.id.textViewTimeValue);
        textViewDepthValue = view.findViewById(R.id.textViewDepthValue);
        textViewAscentRateValue = view.findViewById(R.id.textViewAscentRateValue);
        textViewDescentRateValue = view.findViewById(R.id.textViewDescentRateValue);
        textViewGasValue = view.findViewById(R.id.textViewGasValue);
        layoutSetPointSection = view.findViewById(R.id.layoutSetPointSection);
        textViewSetPointValue = view.findViewById(R.id.textViewSetPointValue);
        layoutAdvancedToggle = view.findViewById(R.id.layoutAdvancedToggle);
        imageViewAdvancedArrow = view.findViewById(R.id.imageViewAdvancedArrow);
        layoutAdvancedContent = view.findViewById(R.id.layoutAdvancedContent);
        textViewError = view.findViewById(R.id.textViewError);
        buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonSave = view.findViewById(R.id.buttonSave);
    }

    private void setupClickListeners() {
        textViewTimeValue.setOnClickListener(v -> launchTimePicker());
        textViewDepthValue.setOnClickListener(v -> launchDepthPicker());
        textViewAscentRateValue.setOnClickListener(v -> launchAscentRatePicker());
        textViewDescentRateValue.setOnClickListener(v -> launchDescentRatePicker());
        textViewGasValue.setOnClickListener(v -> launchGasPicker());
        textViewSetPointValue.setOnClickListener(v -> launchSpPicker());

        layoutAdvancedToggle.setOnClickListener(v -> viewModel.toggleAdvancedSection());

        buttonSave.setOnClickListener(v -> viewModel.onSaveClicked());
        buttonCancel.setOnClickListener(v -> viewModel.onCancelClicked());
    }

    private void observeViewModel() {
        disposables.add(
                viewModel.uiState
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::updateUi,
                                throwable -> {
                                    Log.e(TAG, "Error observing UI state", throwable);
                                    Toast.makeText(getContext(), "Error: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                                    dismiss();
                                }
                        )
        );
    }

    private void updateUi(AddEditSegmentUiState state) {
        textViewDialogTitle.setText(state.getDialogTitle());

        textViewTimeValue.setText(state.getTimeText());
        textViewDepthValue.setText(state.getDepthText());
        textViewAscentRateValue.setText(state.getAscentRateText());
        textViewDescentRateValue.setText(state.getDescentRateText());
        textViewGasValue.setText(state.getSelectedGasText());
        textViewSetPointValue.setText(state.getSetPointText());

        layoutSetPointSection.setVisibility(state.isSetPointSectionVisible() ? View.VISIBLE : View.GONE);
        layoutAdvancedContent.setVisibility(state.isAdvancedSectionExpanded() ? View.VISIBLE : View.GONE);
        imageViewAdvancedArrow.setImageResource(state.isAdvancedSectionExpanded() ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);

        // Update temporary values for pickers based on current UI state text (parsing needed)
        // This is a bit coupled; ideally ViewModel would also provide raw values or UiState would.
        try {
            // Time: "10 min" -> 10
            if (!TextUtils.isEmpty(state.getTimeText())) {
                String timeOnly = state.getTimeText().split(" ")[0];
                currentPickerTimeMinutes = Integer.parseInt(timeOnly);
            }
            // Depth: "100 ft" or "30 m" -> 100 or 30
            if (!TextUtils.isEmpty(state.getDepthText())) {
                String depthOnly = state.getDepthText().split(" ")[0];
                currentPickerDepthNative = Double.parseDouble(depthOnly);
            }
            // Ascent Rate: "30 ft/min" -> 30
            if (!TextUtils.isEmpty(state.getAscentRateText())) {
                String ascentOnly = state.getAscentRateText().split(" ")[0];
                currentPickerAscentRateNative = Double.parseDouble(ascentOnly);
            }
             // Descent Rate: "60 ft/min" -> 60
            if (!TextUtils.isEmpty(state.getDescentRateText())) {
                String descentOnly = state.getDescentRateText().split(" ")[0];
                currentPickerDescentRateNative = Double.parseDouble(descentOnly);
            }
            // Set Point: "1.3 ata" -> 1.3
            if (state.isSetPointSectionVisible() && !state.getSetPointText().equals("--")) {
                 String spOnly = state.getSetPointText().split(" ")[0];
                 currentPickerSetPoint = Double.parseDouble(spOnly);
            } else {
                currentPickerSetPoint = DomainDefaults.DEFAULT_CC_SET_POINT;
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not parse current display values from UI state text for pickers", e);
            // Initialize with some defaults if parsing fails to prevent picker crashes
            currentPickerTimeMinutes = (int) TimeUnit.SECONDS.toMinutes(DomainDefaults.DEFAULT_SEGMENT_DURATION_SECONDS);
            currentPickerDepthNative = state.getDepthUnitLabel().equals("m") ? UnitConverter.toMeters(DomainDefaults.DEFAULT_SEGMENT_DEPTH_FT) : DomainDefaults.DEFAULT_SEGMENT_DEPTH_FT;
            currentPickerAscentRateNative = state.getRateUnitLabel().equals("m/min") ? DomainDefaults.DEFAULT_ASCENT_RATE_M_MIN : DomainDefaults.DEFAULT_ASCENT_RATE_FT_MIN;
            currentPickerDescentRateNative = state.getRateUnitLabel().equals("m/min") ? DomainDefaults.DEFAULT_DESCENT_RATE_M_MIN : DomainDefaults.DEFAULT_DESCENT_RATE_FT_MIN;
            currentPickerSetPoint = DomainDefaults.DEFAULT_CC_SET_POINT;
        }
        currentUnitSystemForPickers = state.getDepthUnitLabel().equals("m") ? UnitSystem.METRIC : UnitSystem.IMPERIAL;
        currentAvailableGasesForPicker = state.getAvailableGases();
        currentPickerGasIndex = state.getInitialSelectedGasIndex();


        // Handle errors
        String combinedError = getCombinedError(state);
        if (!TextUtils.isEmpty(combinedError)) {
            textViewError.setText(combinedError);
            textViewError.setVisibility(View.VISIBLE);
        } else {
            textViewError.setVisibility(View.GONE);
        }

        // Handle loading/saving states
        buttonSave.setEnabled(!state.isLoading() && !state.isSaving());
        buttonCancel.setEnabled(!state.isLoading() && !state.isSaving());

        if (state.shouldDismissDialog()) {
            dismiss();
        }
    }

    private String getCombinedError(AddEditSegmentUiState state) {
        StringBuilder errorBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(state.getTimeError())) errorBuilder.append(state.getTimeError()).append("\n");
        if (!TextUtils.isEmpty(state.getDepthError())) errorBuilder.append(state.getDepthError()).append("\n");
        if (!TextUtils.isEmpty(state.getAscentRateError())) errorBuilder.append(state.getAscentRateError()).append("\n");
        if (!TextUtils.isEmpty(state.getDescentRateError())) errorBuilder.append(state.getDescentRateError()).append("\n");
        if (!TextUtils.isEmpty(state.getGasError())) errorBuilder.append(state.getGasError()).append("\n");
        if (!TextUtils.isEmpty(state.getSetPointError())) errorBuilder.append(state.getSetPointError()).append("\n");
        if (!TextUtils.isEmpty(state.getGeneralError())) errorBuilder.append(state.getGeneralError()).append("\n");

        if (errorBuilder.length() > 0) {
            errorBuilder.setLength(errorBuilder.length() - 1); // Remove last newline
        }
        return errorBuilder.toString();
    }

    private void launchTimePicker() {
        NumberPickerDialogFragment.newIntegerInstance(
                getString(R.string.set_time),
                TIME_PICKER_REQUEST_KEY,
                currentPickerTimeMinutes,
                1, // min time
                999, // max time (e.g., from DomainDefaults or practical limit)
                1, // step
                getString(R.string.unit_min_short)
        ).show(getParentFragmentManager(), NumberPickerDialogFragment.TAG + "_Time");
    }

    private void launchDepthPicker() {
        int minDepth, maxDepth, stepDepth;
        String unitLabel;

        if (currentUnitSystemForPickers == UnitSystem.METRIC) {
            minDepth = 1;
            maxDepth = (int) UnitConverter.toMeters(DomainDefaults.MAX_DEPTH_FT_PLANNING); // approx 150m
            stepDepth = 1;
            unitLabel = getString(R.string.unit_m_short);
        } else {
            minDepth = 1;
            maxDepth = (int) DomainDefaults.MAX_DEPTH_FT_PLANNING; // 499 ft
            stepDepth = 1;
            unitLabel = getString(R.string.unit_ft_short);
        }

        NumberPickerDialogFragment.newIntegerInstance(
                getString(R.string.set_depth),
                DEPTH_PICKER_REQUEST_KEY,
                (int) Math.round(currentPickerDepthNative),
                minDepth,
                maxDepth,
                stepDepth,
                unitLabel
        ).show(getParentFragmentManager(), NumberPickerDialogFragment.TAG + "_Depth");
    }

    private void launchAscentRatePicker() {
        int minRate, maxRate, stepRate;
        String unitLabel;

        if (currentUnitSystemForPickers == UnitSystem.METRIC) {
            minRate = (int) DomainDefaults.MIN_ASCENT_RATE_M_MIN;
            maxRate = (int) DomainDefaults.MAX_ASCENT_RATE_M_MIN;
            stepRate = 1;
            unitLabel = getString(R.string.unit_m_min_short);
        } else {
            minRate = (int) DomainDefaults.MIN_ASCENT_RATE_FT_MIN;
            maxRate = (int) DomainDefaults.MAX_ASCENT_RATE_FT_MIN;
            stepRate = 1;
            unitLabel = getString(R.string.unit_ft_min_short);
        }
        NumberPickerDialogFragment.newIntegerInstance(
                getString(R.string.set_ascent_rate),
                ASCENT_RATE_PICKER_REQUEST_KEY,
                (int) Math.round(currentPickerAscentRateNative),
                minRate,
                maxRate,
                stepRate,
                unitLabel
        ).show(getParentFragmentManager(), NumberPickerDialogFragment.TAG + "_AscentRate");
    }

    private void launchDescentRatePicker() {
        int minRate, maxRate, stepRate;
        String unitLabel;

        if (currentUnitSystemForPickers == UnitSystem.METRIC) {
            minRate = (int) DomainDefaults.MIN_DESCENT_RATE_M_MIN;
            maxRate = (int) DomainDefaults.MAX_DESCENT_RATE_M_MIN;
            stepRate = 1;
            unitLabel = getString(R.string.unit_m_min_short);
        } else {
            minRate = (int) DomainDefaults.MIN_DESCENT_RATE_FT_MIN;
            maxRate = (int) DomainDefaults.MAX_DESCENT_RATE_FT_MIN;
            stepRate = 1;
            unitLabel = getString(R.string.unit_ft_min_short);
        }
        NumberPickerDialogFragment.newIntegerInstance(
                getString(R.string.set_descent_rate),
                DESCENT_RATE_PICKER_REQUEST_KEY,
                (int) Math.round(currentPickerDescentRateNative),
                minRate,
                maxRate,
                stepRate,
                unitLabel
        ).show(getParentFragmentManager(), NumberPickerDialogFragment.TAG + "_DescentRate");
    }

    private void launchSpPicker() {
        NumberPickerDialogFragment.newFloatInstance(
                getString(R.string.set_set_point),
                SP_PICKER_REQUEST_KEY,
                (float) currentPickerSetPoint,
                (float) DomainDefaults.MIN_SET_POINT,
                (float) DomainDefaults.MAX_SET_POINT,
                0.01f,
                getString(R.string.unit_ata_short),
                "%.2f",
                2
        ).show(getParentFragmentManager(), NumberPickerDialogFragment.TAG + "_SP");
    }

    private void launchGasPicker() {
        if (currentAvailableGasesForPicker == null || currentAvailableGasesForPicker.isEmpty()) {
            Toast.makeText(getContext(), R.string.no_gases_available, Toast.LENGTH_SHORT).show();
            return;
        }
        String[] gasDisplayNames = currentAvailableGasesForPicker.stream()
                .map(gas -> String.format("%s (%s)", gas.getGasName(), gas.getGasType().getShortName()))
                .toArray(String[]::new);

        ListPickerDialogFragment.newInstance(
                getString(R.string.select_gas),
                gasDisplayNames,
                currentPickerGasIndex, // Use index from UiState
                GAS_PICKER_REQUEST_KEY
        ).show(getParentFragmentManager(), ListPickerDialogFragment.TAG + "_Gas");
    }


    private void setupFragmentResultListeners() {
        getParentFragmentManager().setFragmentResultListener(TIME_PICKER_REQUEST_KEY, this, (requestKey, bundle) -> {
            int selectedTime = bundle.getInt(NumberPickerDialogFragment.RESULT_PICKER_1_VALUE);
            viewModel.updateSegmentTime(selectedTime);
        });

        getParentFragmentManager().setFragmentResultListener(DEPTH_PICKER_REQUEST_KEY, this, (requestKey, bundle) -> {
            int selectedDepth = bundle.getInt(NumberPickerDialogFragment.RESULT_PICKER_1_VALUE);
            viewModel.updateSegmentDepth(selectedDepth); // Pass native value (int)
        });

        getParentFragmentManager().setFragmentResultListener(ASCENT_RATE_PICKER_REQUEST_KEY, this, (requestKey, bundle) -> {
            int selectedRate = bundle.getInt(NumberPickerDialogFragment.RESULT_PICKER_1_VALUE);
            viewModel.updateAscentRate(selectedRate); // Pass native value (int)
        });

        getParentFragmentManager().setFragmentResultListener(DESCENT_RATE_PICKER_REQUEST_KEY, this, (requestKey, bundle) -> {
            int selectedRate = bundle.getInt(NumberPickerDialogFragment.RESULT_PICKER_1_VALUE);
            viewModel.updateDescentRate(selectedRate); // Pass native value (int)
        });

        getParentFragmentManager().setFragmentResultListener(SP_PICKER_REQUEST_KEY, this, (requestKey, bundle) -> {
            float selectedSp = bundle.getFloat(NumberPickerDialogFragment.RESULT_PICKER_1_VALUE);
            viewModel.updateSetPoint(selectedSp);
        });

        getParentFragmentManager().setFragmentResultListener(GAS_PICKER_REQUEST_KEY, this, (requestKey, bundle) -> {
            int selectedIndex = bundle.getInt(ListPickerDialogFragment.RESULT_SELECTED_INDEX);
            if (currentAvailableGasesForPicker != null && selectedIndex >= 0 && selectedIndex < currentAvailableGasesForPicker.size()) {
                viewModel.updateSelectedGas(currentAvailableGasesForPicker.get(selectedIndex));
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        // Nullify views to avoid memory leaks if not using ViewBinding
        textViewDialogTitle = null;
        textViewTimeValue = null;
        textViewDepthValue = null;
        textViewAscentRateValue = null;
        textViewDescentRateValue = null;
        textViewGasValue = null;
        layoutSetPointSection = null;
        textViewSetPointValue = null;
        layoutAdvancedToggle = null;
        imageViewAdvancedArrow = null;
        layoutAdvancedContent = null;
        textViewError = null;
        buttonCancel = null;
        buttonSave = null;
    }
} 