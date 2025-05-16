package com.burc.novadiveplannerupdated.presentation.ui.common.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.burc.novadiveplannerupdated.databinding.DialogListPickerBinding;

public class ListPickerDialogFragment extends DialogFragment {

    public static final String TAG = "ListPickerDialog";
    public static final String RESULT_SELECTED_INDEX = "selectedIndex";
    public static final String RESULT_SELECTED_VALUE = "selectedValue";

    private static final String ARG_TITLE = "title";
    private static final String ARG_DISPLAYED_VALUES = "displayedValues";
    private static final String ARG_INITIAL_INDEX = "initialIndex";
    private static final String ARG_REQUEST_KEY = "customRequestKey";

    private DialogListPickerBinding binding;
    private String[] displayedValues;
    private String customRequestKey;

    public static ListPickerDialogFragment newInstance(String title, String[] displayedValues, int initialIndex, String requestKey) {
        ListPickerDialogFragment fragment = new ListPickerDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putStringArray(ARG_DISPLAYED_VALUES, displayedValues);
        args.putInt(ARG_INITIAL_INDEX, initialIndex);
        args.putString(ARG_REQUEST_KEY, requestKey);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            customRequestKey = getArguments().getString(ARG_REQUEST_KEY);
            displayedValues = getArguments().getStringArray(ARG_DISPLAYED_VALUES);
        } else {
            // Gerekli argümanlar yoksa hata yönetimi yapılabilir veya varsayılan bir davranış belirlenebilir.
            // Örneğin, customRequestKey null ise işlem yapma veya hata fırlat.
            // Şimdilik null kontrolü listener içinde yapılacaktır.
        }

        binding = DialogListPickerBinding.inflate(LayoutInflater.from(requireContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(binding.getRoot());

        if (getArguments() != null) {
            binding.textViewDialogTitle.setText(getArguments().getString(ARG_TITLE));
            int initialIndex = getArguments().getInt(ARG_INITIAL_INDEX);

            if (displayedValues != null && displayedValues.length > 0) {
                binding.numberPicker.setMinValue(0);
                binding.numberPicker.setMaxValue(displayedValues.length - 1);
                binding.numberPicker.setDisplayedValues(displayedValues);
                if (initialIndex >= 0 && initialIndex < displayedValues.length) {
                    binding.numberPicker.setValue(initialIndex);
                } else {
                    binding.numberPicker.setValue(0);
                }
                binding.numberPicker.setWrapSelectorWheel(false);
            } else {
                binding.numberPicker.setEnabled(false);
            }
        }

        binding.buttonSet.setOnClickListener(v -> {
            if (customRequestKey == null) {
                dismiss();
                return;
            }
            Bundle result = new Bundle();
            int selectedIndex = binding.numberPicker.getValue();
            result.putInt(RESULT_SELECTED_INDEX, selectedIndex);
            if (displayedValues != null && selectedIndex >= 0 && selectedIndex < displayedValues.length) {
                result.putString(RESULT_SELECTED_VALUE, displayedValues[selectedIndex]);
            }
            getParentFragmentManager().setFragmentResult(customRequestKey, result);
            dismiss();
        });

        binding.buttonCancel.setOnClickListener(v -> dismiss());

        Dialog dialog = builder.create();
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.BOTTOM);
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 