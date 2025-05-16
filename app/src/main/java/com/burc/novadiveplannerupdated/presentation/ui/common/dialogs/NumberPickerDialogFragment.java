package com.burc.novadiveplannerupdated.presentation.ui.common.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.burc.novadiveplannerupdated.R;
import com.burc.novadiveplannerupdated.databinding.DialogValuePickerBinding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public class NumberPickerDialogFragment extends DialogFragment {

    public static final String TAG = "NumberPickerDialog";

    // Result Bundle Keys
    public static final String RESULT_PICKER_1_VALUE = "picker1Value";
    public static final String RESULT_PICKER_2_VALUE = "picker2Value";

    // Argument Keys
    private static final String ARG_TITLE = "title";
    private static final String ARG_REQUEST_KEY = "requestKey";
    private static final String ARG_PICKER_COUNT = "pickerCount";
    private static final String ARG_VALUE_TYPE = "valueType";
    private static final String VALUE_TYPE_INTEGER = "INTEGER";
    private static final String VALUE_TYPE_FLOAT = "FLOAT";

    // Picker 1 Args
    private static final String ARG_P1_INITIAL_VALUE = "p1InitialValue";
    private static final String ARG_P1_MIN_VALUE = "p1MinValue";
    private static final String ARG_P1_MAX_VALUE = "p1MaxValue";
    private static final String ARG_P1_STEP_VALUE = "p1StepValue";
    private static final String ARG_P1_UNIT_LABEL = "p1UnitLabel";
    private static final String ARG_P1_FORMAT_STRING = "p1FormatString";
    private static final String ARG_P1_SCALE = "p1Scale";

    // Picker 2 Args
    private static final String ARG_P2_INITIAL_VALUE = "p2InitialValue";
    private static final String ARG_P2_MIN_VALUE = "p2MinValue";
    private static final String ARG_P2_MAX_VALUE = "p2MaxValue";
    private static final String ARG_P2_STEP_VALUE = "p2StepValue";
    private static final String ARG_P2_UNIT_LABEL = "p2UnitLabel";
    private static final String ARG_P2_FORMAT_STRING = "p2FormatString";
    private static final String ARG_P2_SCALE = "p2Scale";

    private DialogValuePickerBinding binding;
    private String requestKey;
    private int pickerCount;
    private String valueType;

    private Number currentPicker1Value;
    private Number currentPicker2Value;

    public static NumberPickerDialogFragment newIntegerInstance(String title, String requestKey,
                                                                int initialValue, int minValue, int maxValue, int stepValue,
                                                                @Nullable String unitLabel) {
        NumberPickerDialogFragment fragment = new NumberPickerDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_REQUEST_KEY, requestKey);
        args.putInt(ARG_PICKER_COUNT, 1);
        args.putString(ARG_VALUE_TYPE, VALUE_TYPE_INTEGER);
        args.putInt(ARG_P1_INITIAL_VALUE, initialValue);
        args.putInt(ARG_P1_MIN_VALUE, minValue);
        args.putInt(ARG_P1_MAX_VALUE, maxValue);
        args.putInt(ARG_P1_STEP_VALUE, stepValue);
        if (unitLabel != null) args.putString(ARG_P1_UNIT_LABEL, unitLabel);
        fragment.setArguments(args);
        return fragment;
    }

    public static NumberPickerDialogFragment newFloatInstance(String title, String requestKey,
                                                              float initialValue, float minValue, float maxValue, float stepValue,
                                                              @Nullable String unitLabel, @Nullable String formatString, int scale) {
        NumberPickerDialogFragment fragment = new NumberPickerDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_REQUEST_KEY, requestKey);
        args.putInt(ARG_PICKER_COUNT, 1);
        args.putString(ARG_VALUE_TYPE, VALUE_TYPE_FLOAT);
        args.putFloat(ARG_P1_INITIAL_VALUE, initialValue);
        args.putFloat(ARG_P1_MIN_VALUE, minValue);
        args.putFloat(ARG_P1_MAX_VALUE, maxValue);
        args.putFloat(ARG_P1_STEP_VALUE, stepValue);
        if (unitLabel != null) args.putString(ARG_P1_UNIT_LABEL, unitLabel);
        if (formatString != null) args.putString(ARG_P1_FORMAT_STRING, formatString);
        args.putInt(ARG_P1_SCALE, scale);
        fragment.setArguments(args);
        return fragment;
    }

    public static NumberPickerDialogFragment newDoubleIntegerInstance(String title, String requestKey,
                                                                      int initialValue1, int minValue1, int maxValue1, int stepValue1, @Nullable String unitLabel1,
                                                                      int initialValue2, int minValue2, int maxValue2, int stepValue2, @Nullable String unitLabel2) {
        NumberPickerDialogFragment fragment = new NumberPickerDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_REQUEST_KEY, requestKey);
        args.putInt(ARG_PICKER_COUNT, 2);
        args.putString(ARG_VALUE_TYPE, VALUE_TYPE_INTEGER);

        // Picker 1 Args
        args.putInt(ARG_P1_INITIAL_VALUE, initialValue1);
        args.putInt(ARG_P1_MIN_VALUE, minValue1);
        args.putInt(ARG_P1_MAX_VALUE, maxValue1);
        args.putInt(ARG_P1_STEP_VALUE, stepValue1);
        if (unitLabel1 != null) args.putString(ARG_P1_UNIT_LABEL, unitLabel1);

        // Picker 2 Args
        args.putInt(ARG_P2_INITIAL_VALUE, initialValue2);
        args.putInt(ARG_P2_MIN_VALUE, minValue2);
        args.putInt(ARG_P2_MAX_VALUE, maxValue2);
        args.putInt(ARG_P2_STEP_VALUE, stepValue2);
        if (unitLabel2 != null) args.putString(ARG_P2_UNIT_LABEL, unitLabel2);

        fragment.setArguments(args);
        return fragment;
    }

    // TODO: Add newInstance for Double Float Picker (or mixed)

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogValuePickerBinding.inflate(LayoutInflater.from(requireContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(binding.getRoot());

        if (getArguments() != null) {
            requestKey = getArguments().getString(ARG_REQUEST_KEY);
            pickerCount = getArguments().getInt(ARG_PICKER_COUNT, 1);
            valueType = getArguments().getString(ARG_VALUE_TYPE, VALUE_TYPE_INTEGER);
            binding.textViewDialogTitle.setText(getArguments().getString(ARG_TITLE));
            configurePickers();
        }

        binding.buttonSet.setOnClickListener(v -> {
            if (requestKey == null) {
                dismiss();
                return;
            }
            Bundle result = new Bundle();
            if (VALUE_TYPE_INTEGER.equals(valueType)) {
                if (currentPicker1Value != null) result.putInt(RESULT_PICKER_1_VALUE, currentPicker1Value.intValue());
                if (pickerCount == 2 && currentPicker2Value != null) {
                    result.putInt(RESULT_PICKER_2_VALUE, currentPicker2Value.intValue());
                }
            } else if (VALUE_TYPE_FLOAT.equals(valueType)) {
                if (currentPicker1Value != null) result.putFloat(RESULT_PICKER_1_VALUE, currentPicker1Value.floatValue());
                // TODO: Handle double picker for float
            }
            getParentFragmentManager().setFragmentResult(requestKey, result);
            dismiss();
        });

        binding.buttonCancel.setOnClickListener(v -> dismiss());

        Dialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    private void configurePickers() {
        Bundle args = getArguments();
        if (args == null) return;

        // Configure Picker 1
        String p1FormatString = args.getString(ARG_P1_FORMAT_STRING);
        if (VALUE_TYPE_INTEGER.equals(valueType)) {
            currentPicker1Value = args.getInt(ARG_P1_INITIAL_VALUE);
            binding.textViewPicker1Value.setText(String.format(Locale.getDefault(), "%d", currentPicker1Value.intValue()));
        } else if (VALUE_TYPE_FLOAT.equals(valueType)) {
            currentPicker1Value = args.getFloat(ARG_P1_INITIAL_VALUE);
            if (!TextUtils.isEmpty(p1FormatString)) {
                binding.textViewPicker1Value.setText(String.format(Locale.US, p1FormatString, currentPicker1Value.floatValue()));
            } else {
                binding.textViewPicker1Value.setText(String.valueOf(currentPicker1Value.floatValue()));
            }
        }
        String unitLabel1 = args.getString(ARG_P1_UNIT_LABEL);
        if (unitLabel1 != null) {
            binding.textViewPicker1Unit.setText(unitLabel1);
            binding.textViewPicker1Unit.setVisibility(View.VISIBLE);
        } else {
            binding.textViewPicker1Unit.setVisibility(View.GONE);
        }
        binding.buttonPicker1Up.setOnClickListener(v -> updatePickerValue(1, true));
        binding.buttonPicker1Down.setOnClickListener(v -> updatePickerValue(1, false));

        // Configure Picker 2
        if (pickerCount == 2) {
            binding.picker2Container.setVisibility(View.VISIBLE);
            String p2FormatString = args.getString(ARG_P2_FORMAT_STRING);
            if (VALUE_TYPE_INTEGER.equals(valueType)) {
                currentPicker2Value = args.getInt(ARG_P2_INITIAL_VALUE);
                binding.textViewPicker2Value.setText(String.format(Locale.getDefault(), "%d", currentPicker2Value.intValue()));
            } else if (VALUE_TYPE_FLOAT.equals(valueType)) {
                // TODO: Handle float initial value for Picker 2
            }
            String unitLabel2 = args.getString(ARG_P2_UNIT_LABEL);
            if (unitLabel2 != null) {
                binding.textViewPicker2Unit.setText(unitLabel2);
                binding.textViewPicker2Unit.setVisibility(View.VISIBLE);
            } else {
                binding.textViewPicker2Unit.setVisibility(View.GONE);
            }
            binding.buttonPicker2Up.setOnClickListener(v -> updatePickerValue(2, true));
            binding.buttonPicker2Down.setOnClickListener(v -> updatePickerValue(2, false));
        } else {
            binding.picker2Container.setVisibility(View.GONE);
            binding.textViewPicker2Unit.setVisibility(View.GONE);
        }
    }

    private void updatePickerValue(int pickerNum, boolean increment) {
        Bundle args = getArguments();
        if (args == null) return;

        if (pickerNum == 1) {
            if (VALUE_TYPE_INTEGER.equals(valueType)) {
                int currentValueInt = currentPicker1Value.intValue();
                int step = args.getInt(ARG_P1_STEP_VALUE, 1);
                int min = args.getInt(ARG_P1_MIN_VALUE, Integer.MIN_VALUE);
                int max = args.getInt(ARG_P1_MAX_VALUE, Integer.MAX_VALUE);
                if (increment) currentValueInt = Math.min(max, currentValueInt + step);
                else currentValueInt = Math.max(min, currentValueInt - step);
                currentPicker1Value = currentValueInt;
                binding.textViewPicker1Value.setText(String.format(Locale.getDefault(), "%d", currentValueInt));
            } else if (VALUE_TYPE_FLOAT.equals(valueType)) {
                BigDecimal currentValueBd = BigDecimal.valueOf(currentPicker1Value.doubleValue());
                BigDecimal stepBd = BigDecimal.valueOf(args.getFloat(ARG_P1_STEP_VALUE, 0.1f));
                BigDecimal minBd = BigDecimal.valueOf(args.getFloat(ARG_P1_MIN_VALUE, Float.MIN_VALUE));
                BigDecimal maxBd = BigDecimal.valueOf(args.getFloat(ARG_P1_MAX_VALUE, Float.MAX_VALUE));
                int scale = args.getInt(ARG_P1_SCALE, 2);
                if (increment) currentValueBd = currentValueBd.add(stepBd);
                else currentValueBd = currentValueBd.subtract(stepBd);
                if (currentValueBd.compareTo(maxBd) > 0) currentValueBd = maxBd;
                if (currentValueBd.compareTo(minBd) < 0) currentValueBd = minBd;
                currentPicker1Value = currentValueBd.setScale(scale, RoundingMode.HALF_UP).floatValue();
                String p1FormatString = args.getString(ARG_P1_FORMAT_STRING);
                if (!TextUtils.isEmpty(p1FormatString)) {
                    binding.textViewPicker1Value.setText(String.format(Locale.US, p1FormatString, currentPicker1Value.floatValue()));
                } else {
                    binding.textViewPicker1Value.setText(String.valueOf(currentPicker1Value.floatValue()));
                }
            }
        } else if (pickerNum == 2) {
            if (VALUE_TYPE_INTEGER.equals(valueType)) {
                int currentValueInt = currentPicker2Value.intValue();
                int step = args.getInt(ARG_P2_STEP_VALUE, 1);
                int min = args.getInt(ARG_P2_MIN_VALUE, Integer.MIN_VALUE);
                int max = args.getInt(ARG_P2_MAX_VALUE, Integer.MAX_VALUE);
                if (increment) currentValueInt = Math.min(max, currentValueInt + step);
                else currentValueInt = Math.max(min, currentValueInt - step);
                currentPicker2Value = currentValueInt;
                binding.textViewPicker2Value.setText(String.format(Locale.getDefault(), "%d", currentValueInt));
            } else if (VALUE_TYPE_FLOAT.equals(valueType)) {
                // TODO: Handle float value update for Picker 2
            }
        }
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