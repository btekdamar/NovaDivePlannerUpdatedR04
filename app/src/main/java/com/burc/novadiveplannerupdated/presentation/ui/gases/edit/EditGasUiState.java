package com.burc.novadiveplannerupdated.presentation.ui.gases.edit;

import androidx.annotation.Nullable;
import java.util.Objects;

public final class EditGasUiState {
    private final String dialogTitle;
    private final boolean isLoading;
    private final boolean isSaving;
    private final String gasModeText;
    private final String fo2Text;
    private final String fheText;
    private final String po2MaxText;
    @Nullable private final String fo2Error;
    @Nullable private final String fheError;
    @Nullable private final String po2MaxError;
    @Nullable private final String generalError;
    private final boolean dismissDialog;

    public EditGasUiState(
            String dialogTitle,
            boolean isLoading,
            boolean isSaving,
            String gasModeText,
            String fo2Text,
            String fheText,
            String po2MaxText,
            @Nullable String fo2Error,
            @Nullable String fheError,
            @Nullable String po2MaxError,
            @Nullable String generalError,
            boolean dismissDialog) {
        this.dialogTitle = dialogTitle;
        this.isLoading = isLoading;
        this.isSaving = isSaving;
        this.gasModeText = gasModeText;
        this.fo2Text = fo2Text;
        this.fheText = fheText;
        this.po2MaxText = po2MaxText;
        this.fo2Error = fo2Error;
        this.fheError = fheError;
        this.po2MaxError = po2MaxError;
        this.generalError = generalError;
        this.dismissDialog = dismissDialog;
    }

    public static EditGasUiState initialState() {
        return new EditGasUiState("Edit Gas", true, false, "", "", "", "", null, null, null, null, false);
    }

    public String getDialogTitle() { return dialogTitle; }
    public boolean isLoading() { return isLoading; }
    public boolean isSaving() { return isSaving; }
    public String getGasModeText() { return gasModeText; }
    public String getFo2Text() { return fo2Text; }
    public String getFheText() { return fheText; }
    public String getPo2MaxText() { return po2MaxText; }
    @Nullable public String getFo2Error() { return fo2Error; }
    @Nullable public String getFheError() { return fheError; }
    @Nullable public String getPo2MaxError() { return po2MaxError; }
    @Nullable public String getGeneralError() { return generalError; }
    public boolean shouldDismissDialog() { return dismissDialog; }

    public EditGasUiState copy(
            String dialogTitle,
            Boolean isLoading,
            Boolean isSaving,
            String gasModeText,
            String fo2Text,
            String fheText,
            String po2MaxText,
            @Nullable String fo2Error,
            @Nullable String fheError,
            @Nullable String po2MaxError,
            @Nullable String generalError,
            Boolean dismissDialog
    ) {
        return new EditGasUiState(
            dialogTitle != null ? dialogTitle : this.dialogTitle,
            isLoading != null ? isLoading : this.isLoading,
            isSaving != null ? isSaving : this.isSaving,
            gasModeText != null ? gasModeText : this.gasModeText,
            fo2Text != null ? fo2Text : this.fo2Text,
            fheText != null ? fheText : this.fheText,
            po2MaxText != null ? po2MaxText : this.po2MaxText,
            fo2Error, // Errors are typically replaced, not kept if null
            fheError,
            po2MaxError,
            generalError,
            dismissDialog != null ? dismissDialog : this.dismissDialog
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EditGasUiState that = (EditGasUiState) o;
        return isLoading == that.isLoading &&
                isSaving == that.isSaving &&
                dismissDialog == that.dismissDialog &&
                Objects.equals(dialogTitle, that.dialogTitle) &&
                Objects.equals(gasModeText, that.gasModeText) &&
                Objects.equals(fo2Text, that.fo2Text) &&
                Objects.equals(fheText, that.fheText) &&
                Objects.equals(po2MaxText, that.po2MaxText) &&
                Objects.equals(fo2Error, that.fo2Error) &&
                Objects.equals(fheError, that.fheError) &&
                Objects.equals(po2MaxError, that.po2MaxError) &&
                Objects.equals(generalError, that.generalError);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dialogTitle, isLoading, isSaving, gasModeText, fo2Text, fheText, po2MaxText, fo2Error, fheError, po2MaxError, generalError, dismissDialog);
    }

    @Override
    public String toString() {
        return "EditGasUiState{" +
                "dialogTitle='" + dialogTitle + '\'' +
                ", isLoading=" + isLoading +
                ", isSaving=" + isSaving +
                ", gasModeText='" + gasModeText + '\'' +
                ", fo2Text='" + fo2Text + '\'' +
                ", fheText='" + fheText + '\'' +
                ", po2MaxText='" + po2MaxText + '\'' +
                ", fo2Error='" + fo2Error + '\'' +
                ", fheError='" + fheError + '\'' +
                ", po2MaxError='" + po2MaxError + '\'' +
                ", generalError='" + generalError + '\'' +
                ", dismissDialog=" + dismissDialog +
                '}';
    }
} 