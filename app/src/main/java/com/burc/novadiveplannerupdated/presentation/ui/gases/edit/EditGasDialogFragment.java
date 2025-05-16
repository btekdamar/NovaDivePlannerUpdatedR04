package com.burc.novadiveplannerupdated.presentation.ui.gases.edit;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.burc.novadiveplannerupdated.R;
import com.burc.novadiveplannerupdated.databinding.DialogEditGasBinding;
import com.burc.novadiveplannerupdated.domain.model.GasType;
import com.burc.novadiveplannerupdated.presentation.ui.common.dialogs.ListPickerDialogFragment;
import com.burc.novadiveplannerupdated.presentation.ui.common.dialogs.NumberPickerDialogFragment;

import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@AndroidEntryPoint
public class EditGasDialogFragment extends DialogFragment {
    private static final String TAG = "EditGasDialogFrag";

    private static final String REQUEST_KEY_MODE = "editGasModeResult";
    private static final String REQUEST_KEY_FO2 = "editGasFo2Result";
    private static final String REQUEST_KEY_FHE = "editGasFheResult";
    private static final String REQUEST_KEY_PO2_MAX = "editGasPo2MaxResult";

    private DialogEditGasBinding binding;
    private EditGasViewModel viewModel;
    private final CompositeDisposable disposables = new CompositeDisposable();

    // To store current values for pickers, potentially fetched from ViewModel's non-UI state if available
    // Or parse from uiState's text values when picker is opened.
    private GasType currentDisplayGasType = GasType.OPEN_CIRCUIT;
    private double currentDisplayFo2 = 21.0; // Default, will be updated from ViewModel
    private double currentDisplayFhe = 0.0;
    private double currentDisplayPo2Max = 1.4;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EditGasViewModel.class);
        // Arguments for ViewModel are handled by Hilt's SavedStateHandle via NavArgs
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Use a basic Dialog and inflate content using onCreateView
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // You can customize dialog properties here if needed, e.g. no title
        // dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogEditGasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
        observeViewModel();
        setupFragmentResultListeners();
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

    private void updateUi(EditGasUiState state) {
        binding.textViewDialogTitle.setText(state.getDialogTitle());
        binding.textViewGasModeValue.setText(state.getGasModeText());
        binding.textViewFo2Value.setText(state.getFo2Text());
        binding.textViewFheValue.setText(state.getFheText());
        binding.textViewPo2MaxValue.setText(state.getPo2MaxText());

        // Update current display values from formatted text for picker initialization
        // More robust: ViewModel could expose raw current values or UiState could carry them.
        try {
            currentDisplayGasType = state.getGasModeText().equals("OC") ? GasType.OPEN_CIRCUIT : GasType.CLOSED_CIRCUIT;
            if (!state.getFo2Text().equals("--")) currentDisplayFo2 = Double.parseDouble(state.getFo2Text());
            if (!state.getFheText().equals("--")) currentDisplayFhe = Double.parseDouble(state.getFheText());
            if (!state.getPo2MaxText().equals("--")) currentDisplayPo2Max = Double.parseDouble(state.getPo2MaxText());
        } catch (NumberFormatException e) {
            Log.w(TAG, "Could not parse current display values from UI state text", e);
        }

        // Handle errors (e.g., by showing them below respective fields or as Toast)
        // For now, using Toast for general error.
        if (!TextUtils.isEmpty(state.getFo2Error())) {
            Toast.makeText(getContext(), "FO2 Error: " + state.getFo2Error(), Toast.LENGTH_SHORT).show();
        }
        if (!TextUtils.isEmpty(state.getFheError())) {
            Toast.makeText(getContext(), "FHe Error: " + state.getFheError(), Toast.LENGTH_SHORT).show();
        }
        if (!TextUtils.isEmpty(state.getPo2MaxError())) {
            Toast.makeText(getContext(), "PO2Max Error: " + state.getPo2MaxError(), Toast.LENGTH_SHORT).show();
        }
        if (!TextUtils.isEmpty(state.getGeneralError())) {
            Toast.makeText(getContext(), state.getGeneralError(), Toast.LENGTH_LONG).show();
        }

        // Handle loading/saving states (e.g., disable buttons or show progress)
        binding.buttonSave.setEnabled(!state.isLoading() && !state.isSaving());
        binding.buttonCancel.setEnabled(!state.isLoading() && !state.isSaving());

        if (state.shouldDismissDialog()) {
            dismiss();
        }
    }

    private void setupClickListeners() {
        binding.textViewGasModeValue.setOnClickListener(v -> openModePicker());
        binding.textViewFo2Value.setOnClickListener(v -> openFo2Picker());
        binding.textViewFheValue.setOnClickListener(v -> openFhePicker());
        binding.textViewPo2MaxValue.setOnClickListener(v -> {
            if (currentDisplayGasType == GasType.OPEN_CIRCUIT) { // Only allow editing PO2 Max for OC
                openPo2MaxPicker();
            } else {
                Toast.makeText(getContext(), "PO2 Max is only applicable for Open Circuit gases.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.buttonSave.setOnClickListener(v -> viewModel.onSaveClicked());
        binding.buttonCancel.setOnClickListener(v -> viewModel.onCancelClicked());
    }

    private void openModePicker() {
        String[] modes = { "OC", "CC" };
        int initialIndex = currentDisplayGasType == GasType.OPEN_CIRCUIT ? 0 : 1;
        ListPickerDialogFragment.newInstance("Select Gas Mode", modes, initialIndex, REQUEST_KEY_MODE)
                .show(getParentFragmentManager(), ListPickerDialogFragment.TAG);
    }

    private void openFo2Picker() {
        // FO2 is percentage 7-100. Max FO2 is 100 - current FHe (as percentage).
        double fHePercent = currentDisplayFhe; // Already a percentage if parsed from UI
        int maxFo2 = (int) (100.0 - fHePercent);
        maxFo2 = Math.max(7, maxFo2); // Ensure maxFo2 is at least 7
        int currentFo2Int = (int) Math.round(currentDisplayFo2);
        currentFo2Int = Math.min(currentFo2Int, maxFo2); // Ensure current value is not > max
        currentFo2Int = Math.max(7, currentFo2Int);     // Ensure current value is not < min

        NumberPickerDialogFragment.newIntegerInstance(
                getString(R.string.edit_gas_dialog_set_fo2_title), // "Set FO2 (%)"
                REQUEST_KEY_FO2,
                currentFo2Int, 
                7, // Min FO2
                maxFo2, // Max FO2 based on FHe
                1, "%"
        ).show(getParentFragmentManager(), NumberPickerDialogFragment.TAG);
    }

    private void openFhePicker() {
        // FHe is percentage 0-93. Max FHe is 100 - current FO2 (as percentage).
        double fo2Percent = currentDisplayFo2; // Already a percentage if parsed from UI
        int maxFhe = (int) (100.0 - fo2Percent);
        maxFhe = Math.max(0, maxFhe); // Ensure maxFhe is at least 0
        int currentFheInt = (int) Math.round(currentDisplayFhe);
        currentFheInt = Math.min(currentFheInt, maxFhe); // Ensure current value is not > max
        currentFheInt = Math.max(0, currentFheInt);     // Ensure current value is not < min

        NumberPickerDialogFragment.newIntegerInstance(
                getString(R.string.edit_gas_dialog_set_fhe_title), // "Set FHe (%)"
                REQUEST_KEY_FHE,
                currentFheInt,
                0, // Min FHe
                maxFhe, // Max FHe based on FO2
                1, "%"
        ).show(getParentFragmentManager(), NumberPickerDialogFragment.TAG);
    }

    private void openPo2MaxPicker() {
        // Assuming PO2 Max is float 0.1-3.0
        NumberPickerDialogFragment.newFloatInstance(
                "Set PO2 Max (ata)", REQUEST_KEY_PO2_MAX,
                (float) currentDisplayPo2Max, 
                0.1f, 3.0f, 0.01f, 
                "ata", "%.2f", 2
        ).show(getParentFragmentManager(), NumberPickerDialogFragment.TAG);
    }

    private void setupFragmentResultListeners() {
        getParentFragmentManager().setFragmentResultListener(REQUEST_KEY_MODE, this, (requestKey, bundle) -> {
            String selectedValue = bundle.getString(ListPickerDialogFragment.RESULT_SELECTED_VALUE);
            if (selectedValue != null) {
                viewModel.updateGasMode(selectedValue.equals("OC") ? GasType.OPEN_CIRCUIT : GasType.CLOSED_CIRCUIT);
            }
        });

        getParentFragmentManager().setFragmentResultListener(REQUEST_KEY_FO2, this, (requestKey, bundle) -> {
            int selectedValue = bundle.getInt(NumberPickerDialogFragment.RESULT_PICKER_1_VALUE);
            viewModel.updateFo2(selectedValue / 100.0); // Convert percentage back to fraction
        });

        getParentFragmentManager().setFragmentResultListener(REQUEST_KEY_FHE, this, (requestKey, bundle) -> {
            int selectedValue = bundle.getInt(NumberPickerDialogFragment.RESULT_PICKER_1_VALUE);
            viewModel.updateFhe(selectedValue / 100.0); // Convert percentage back to fraction
        });

        getParentFragmentManager().setFragmentResultListener(REQUEST_KEY_PO2_MAX, this, (requestKey, bundle) -> {
            float selectedValue = bundle.getFloat(NumberPickerDialogFragment.RESULT_PICKER_1_VALUE);
            viewModel.updatePo2Max((double) selectedValue);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // Consider adding dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // if you want to control the background entirely from your XML's root layout.
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
} 