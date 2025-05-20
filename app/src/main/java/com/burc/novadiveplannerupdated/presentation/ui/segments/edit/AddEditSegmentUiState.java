package com.burc.novadiveplannerupdated.presentation.ui.segments.edit;

import androidx.annotation.Nullable;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import java.util.List;
import java.util.Objects;

public final class AddEditSegmentUiState {

    private final String dialogTitle;
    private final boolean isLoading;
    private final boolean isSaving;

    // Segment Data Fields (as strings for direct UI binding)
    private final String timeText; // e.g., "10 min"
    private final String depthText; // e.g., "30 ft" or "10 m"
    private final String ascentRateText; // e.g., "30 ft/min"
    private final String descentRateText; // e.g., "60 ft/min"
    private final String selectedGasText; // e.g., "Air (OC)"
    private final String setPointText; // e.g., "1.3 ata" or "--"

    // Visibility/Availability
    private final boolean isSetPointSectionVisible;
    private final boolean isAdvancedSectionExpanded;

    // Units for hints/labels (though these might be static or derived differently)
    private final String depthUnitLabel; // "ft" or "m"
    private final String rateUnitLabel; // "ft/min" or "m/min"

    // Available gases for picker
    private final List<Gas> availableGases;
    private final int initialSelectedGasIndex;


    // Error messages
    @Nullable private final String timeError;
    @Nullable private final String depthError;
    @Nullable private final String ascentRateError;
    @Nullable private final String descentRateError;
    @Nullable private final String gasError;
    @Nullable private final String setPointError;
    @Nullable private final String generalError;

    private final boolean dismissDialog;

    public AddEditSegmentUiState(
            String dialogTitle,
            boolean isLoading,
            boolean isSaving,
            String timeText,
            String depthText,
            String ascentRateText,
            String descentRateText,
            String selectedGasText,
            String setPointText,
            boolean isSetPointSectionVisible,
            boolean isAdvancedSectionExpanded,
            String depthUnitLabel,
            String rateUnitLabel,
            List<Gas> availableGases,
            int initialSelectedGasIndex,
            @Nullable String timeError,
            @Nullable String depthError,
            @Nullable String ascentRateError,
            @Nullable String descentRateError,
            @Nullable String gasError,
            @Nullable String setPointError,
            @Nullable String generalError,
            boolean dismissDialog) {
        this.dialogTitle = dialogTitle;
        this.isLoading = isLoading;
        this.isSaving = isSaving;
        this.timeText = timeText;
        this.depthText = depthText;
        this.ascentRateText = ascentRateText;
        this.descentRateText = descentRateText;
        this.selectedGasText = selectedGasText;
        this.setPointText = setPointText;
        this.isSetPointSectionVisible = isSetPointSectionVisible;
        this.isAdvancedSectionExpanded = isAdvancedSectionExpanded;
        this.depthUnitLabel = depthUnitLabel;
        this.rateUnitLabel = rateUnitLabel;
        this.availableGases = availableGases;
        this.initialSelectedGasIndex = initialSelectedGasIndex;
        this.timeError = timeError;
        this.depthError = depthError;
        this.ascentRateError = ascentRateError;
        this.descentRateError = descentRateError;
        this.gasError = gasError;
        this.setPointError = setPointError;
        this.generalError = generalError;
        this.dismissDialog = dismissDialog;
    }

    public static AddEditSegmentUiState initialState() {
        return new AddEditSegmentUiState(
                "Segment", // Default title
                true,      // isLoading
                false,     // isSaving
                "", "", "", "", "", "--", // text fields
                false,     // isSetPointSectionVisible
                false,     // isAdvancedSectionExpanded
                "ft", "ft/min", // unit labels
                List.of(), // availableGases
                0,         // initialSelectedGasIndex
                null, null, null, null, null, null, null, // errors
                false      // dismissDialog
        );
    }

    // Getters
    public String getDialogTitle() { return dialogTitle; }
    public boolean isLoading() { return isLoading; }
    public boolean isSaving() { return isSaving; }
    public String getTimeText() { return timeText; }
    public String getDepthText() { return depthText; }
    public String getAscentRateText() { return ascentRateText; }
    public String getDescentRateText() { return descentRateText; }
    public String getSelectedGasText() { return selectedGasText; }
    public String getSetPointText() { return setPointText; }
    public boolean isSetPointSectionVisible() { return isSetPointSectionVisible; }
    public boolean isAdvancedSectionExpanded() { return isAdvancedSectionExpanded; }
    public String getDepthUnitLabel() { return depthUnitLabel; }
    public String getRateUnitLabel() { return rateUnitLabel; }
    public List<Gas> getAvailableGases() { return availableGases; }
    public int getInitialSelectedGasIndex() { return initialSelectedGasIndex; }
    @Nullable public String getTimeError() { return timeError; }
    @Nullable public String getDepthError() { return depthError; }
    @Nullable public String getAscentRateError() { return ascentRateError; }
    @Nullable public String getDescentRateError() { return descentRateError; }
    @Nullable public String getGasError() { return gasError; }
    @Nullable public String getSetPointError() { return setPointError; }
    @Nullable public String getGeneralError() { return generalError; }
    public boolean shouldDismissDialog() { return dismissDialog; }

    public AddEditSegmentUiState copy(
            String dialogTitle,
            Boolean isLoading,
            Boolean isSaving,
            String timeText,
            String depthText,
            String ascentRateText,
            String descentRateText,
            String selectedGasText,
            String setPointText,
            Boolean isSetPointSectionVisible,
            Boolean isAdvancedSectionExpanded,
            String depthUnitLabel,
            String rateUnitLabel,
            List<Gas> availableGases,
            Integer initialSelectedGasIndex,
            @Nullable String timeError,
            @Nullable String depthError,
            @Nullable String ascentRateError,
            @Nullable String descentRateError,
            @Nullable String gasError,
            @Nullable String setPointError,
            @Nullable String generalError,
            Boolean dismissDialog
    ) {
        return new AddEditSegmentUiState(
                dialogTitle != null ? dialogTitle : this.dialogTitle,
                isLoading != null ? isLoading : this.isLoading,
                isSaving != null ? isSaving : this.isSaving,
                timeText != null ? timeText : this.timeText,
                depthText != null ? depthText : this.depthText,
                ascentRateText != null ? ascentRateText : this.ascentRateText,
                descentRateText != null ? descentRateText : this.descentRateText,
                selectedGasText != null ? selectedGasText : this.selectedGasText,
                setPointText != null ? setPointText : this.setPointText,
                isSetPointSectionVisible != null ? isSetPointSectionVisible : this.isSetPointSectionVisible,
                isAdvancedSectionExpanded != null ? isAdvancedSectionExpanded : this.isAdvancedSectionExpanded,
                depthUnitLabel != null ? depthUnitLabel : this.depthUnitLabel,
                rateUnitLabel != null ? rateUnitLabel : this.rateUnitLabel,
                availableGases != null ? availableGases : this.availableGases,
                initialSelectedGasIndex != null ? initialSelectedGasIndex : this.initialSelectedGasIndex,
                timeError, // Errors are typically replaced
                depthError,
                ascentRateError,
                descentRateError,
                gasError,
                setPointError,
                generalError,
                dismissDialog != null ? dismissDialog : this.dismissDialog
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddEditSegmentUiState that = (AddEditSegmentUiState) o;
        return isLoading == that.isLoading &&
                isSaving == that.isSaving &&
                isSetPointSectionVisible == that.isSetPointSectionVisible &&
                isAdvancedSectionExpanded == that.isAdvancedSectionExpanded &&
                initialSelectedGasIndex == that.initialSelectedGasIndex &&
                dismissDialog == that.dismissDialog &&
                Objects.equals(dialogTitle, that.dialogTitle) &&
                Objects.equals(timeText, that.timeText) &&
                Objects.equals(depthText, that.depthText) &&
                Objects.equals(ascentRateText, that.ascentRateText) &&
                Objects.equals(descentRateText, that.descentRateText) &&
                Objects.equals(selectedGasText, that.selectedGasText) &&
                Objects.equals(setPointText, that.setPointText) &&
                Objects.equals(depthUnitLabel, that.depthUnitLabel) &&
                Objects.equals(rateUnitLabel, that.rateUnitLabel) &&
                Objects.equals(availableGases, that.availableGases) &&
                Objects.equals(timeError, that.timeError) &&
                Objects.equals(depthError, that.depthError) &&
                Objects.equals(ascentRateError, that.ascentRateError) &&
                Objects.equals(descentRateError, that.descentRateError) &&
                Objects.equals(gasError, that.gasError) &&
                Objects.equals(setPointError, that.setPointError) &&
                Objects.equals(generalError, that.generalError);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dialogTitle, isLoading, isSaving, timeText, depthText, ascentRateText,
                descentRateText, selectedGasText, setPointText, isSetPointSectionVisible,
                isAdvancedSectionExpanded, depthUnitLabel, rateUnitLabel, availableGases,
                initialSelectedGasIndex, timeError, depthError, ascentRateError, descentRateError,
                gasError, setPointError, generalError, dismissDialog);
    }

    @Override
    public String toString() {
        return "AddEditSegmentUiState{" +
                "dialogTitle='" + dialogTitle + "'" +
                ", isLoading=" + isLoading +
                ", isSaving=" + isSaving +
                ", timeText='" + timeText + "'" +
                ", depthText='" + depthText + "'" +
                // ... (include other fields for comprehensive logging if needed)
                ", dismissDialog=" + dismissDialog +
                '}';
    }
} 