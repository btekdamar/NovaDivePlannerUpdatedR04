package com.burc.novadiveplannerupdated.presentation.ui.segments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.burc.novadiveplannerupdated.domain.entity.Dive;
import com.burc.novadiveplannerupdated.domain.entity.DivePlan;
import com.burc.novadiveplannerupdated.domain.entity.DiveSegment;
import com.burc.novadiveplannerupdated.domain.model.GasType;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;
import com.burc.novadiveplannerupdated.domain.usecase.diveplan.GetActiveDivePlanUseCase;
import com.burc.novadiveplannerupdated.domain.util.UnitConverter;
import com.burc.novadiveplannerupdated.domain.common.DomainDefaults; // Varsayılan birim sistemi için

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers; // AndroidSchedulers importu
import io.reactivex.rxjava3.annotations.Nullable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import android.util.Log; // Loglama için

@HiltViewModel
public class SegmentsViewModel extends ViewModel {
    private static final String TAG = "SegmentsViewModel";

    private final GetActiveDivePlanUseCase getActiveDivePlanUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<SegmentsScreenUiState> _uiState = new MutableLiveData<>();
    public LiveData<SegmentsScreenUiState> uiState = _uiState;

    @Inject
    public SegmentsViewModel(GetActiveDivePlanUseCase getActiveDivePlanUseCase) {
        this.getActiveDivePlanUseCase = getActiveDivePlanUseCase;
        // Başlangıç state'ini yükle
        _uiState.setValue(SegmentsScreenUiState.initialState(DomainDefaults.DEFAULT_UNIT_SYSTEM));
        loadActiveDivePlan();
    }

    private void loadActiveDivePlan() {
        Log.d(TAG, "loadActiveDivePlan called");
        // Mevcut state'i al, isLoading = true yap
        SegmentsScreenUiState currentState = _uiState.getValue();
        if (currentState == null) { // Bu durum olmamalı ama güvenlik için
            currentState = SegmentsScreenUiState.initialState(DomainDefaults.DEFAULT_UNIT_SYSTEM);
        } else {
            currentState = currentState.copy(true, null, false, null, null, null, null, false, false, false);
        }
        _uiState.setValue(currentState);

        disposables.add(
            getActiveDivePlanUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()) // Ana thread'e geçiş
                .subscribe(
                    plan -> {
                        Log.d(TAG, "DivePlan loaded: " + (plan != null ? plan.getId() : "null"));
                        updateUiStates(plan, false, null); // Hata yok, navigasyon yok
                    },
                    throwable -> {
                        Log.e(TAG, "Error loading active dive plan", throwable);
                        updateUiStates(null, false, throwable.getMessage());
                    }
                )
        );
    }

    private void updateUiStates(DivePlan plan, boolean clearNavigationTriggers, @Nullable String errorMessage) {
        Log.d(TAG, String.format("updateUiStates called. Plan: %s, ClearNav: %s, Error: %s",
                (plan != null ? plan.getId() : "null"), clearNavigationTriggers, errorMessage));

        SegmentsScreenUiState currentState = _uiState.getValue();
        if (currentState == null) { // Güvenlik
            currentState = SegmentsScreenUiState.initialState(DomainDefaults.DEFAULT_UNIT_SYSTEM);
        }

        UnitSystem currentUnitSystem = currentState.getUnitSystem();
        if (plan != null && plan.getSettings() != null && currentUnitSystem != plan.getSettings().getUnitSystem()) {
            currentUnitSystem = plan.getSettings().getUnitSystem();
        }

        if (plan == null || plan.getDives().isEmpty()) {
            _uiState.setValue(new SegmentsScreenUiState(
                    false, // isLoading false
                    errorMessage,
                    Collections.emptyList(),
                    false,
                    currentUnitSystem,
                    null, false // Navigasyonları temizle
            ));
            Log.d(TAG, "New UI State posted (empty plan). Error: " + errorMessage);
            return;
        }

        // Her zaman son dalışı göster
        Dive currentDive = plan.getDives().get(plan.getDives().size() - 1);
        List<DiveSegment> segments = currentDive.getSegments();
        List<DisplayableSegmentItem> segmentItems = new ArrayList<>();

        for (int i = 0; i < segments.size(); i++) {
            DiveSegment segment = segments.get(i);
            boolean isLastSegmentInDive = i == segments.size() - 1;
            // Add segment her zaman son dalış için etkin, ve son segment düzenlenebilir.
            segmentItems.add(new DisplayableSegmentItem(
                segment,
                "SEG. " + segment.getSegmentNumber(),
                formatDepth(segment.getTargetDepth(), currentUnitSystem),
                formatTime(segment.getUserInputTotalDurationInSeconds()),
                formatGas(segment.getGas()),
                formatSp(segment.getSetPoint(), segment.getGas().getGasType()),
                isLastSegmentInDive // Sadece son dalıştaki son segment düzenlenebilir
            ));
        }
        
        // Add segment butonu her zaman etkin (eğer bir dalış varsa)
        boolean isAddSegmentEnabled = !plan.getDives().isEmpty();

        _uiState.setValue(new SegmentsScreenUiState(
                false, // isLoading false
                errorMessage,
                segmentItems,
                isAddSegmentEnabled,
                currentUnitSystem,
                clearNavigationTriggers ? null : currentState.getNavigateToEditSegmentFor(),
                clearNavigationTriggers ? false : currentState.isNavigateToAddSegmentTrigger()
        ));
        Log.d(TAG, "New UI State posted. Segments: " + segmentItems.size() + ", AddEnabled: " + isAddSegmentEnabled);
    }

    public void addSegmentClicked() {
        SegmentsScreenUiState currentState = _uiState.getValue();
        if (currentState != null && currentState.isAddSegmentEnabled()) {
            Log.d(TAG, "addSegmentClicked - Triggering add segment dialog state");
            _uiState.setValue(currentState.copy(null, null, false, null, null, null, null, false, true, false));
        } else {
            Log.w(TAG, "addSegmentClicked but not enabled. Current state: " + currentState);
        }
    }

    public void editSegmentClicked(DiveSegment segmentToEdit) {
        SegmentsScreenUiState currentState = _uiState.getValue();
        if (currentState != null && segmentToEdit != null) {
            Log.d(TAG, "editSegmentClicked - Triggering edit segment dialog state for segment: " + segmentToEdit.getSegmentNumber());
            _uiState.setValue(currentState.copy(null, null, false, null, null, null, segmentToEdit.getSegmentNumber(), false, null, false));
        } else {
            Log.w(TAG, "editSegmentClicked with null segment or state. Segment: " + segmentToEdit + ", State: " + currentState);
        }
    }

    public void onAddSegmentNavigationConsumed() {
        SegmentsScreenUiState currentState = _uiState.getValue();
        if (currentState != null && currentState.isNavigateToAddSegmentTrigger()) {
            Log.d(TAG, "onAddSegmentNavigationConsumed - Clearing add segment dialog trigger");
            _uiState.setValue(currentState.copy(null, null, false, null, null, null, null, true, null, true));
        }
    }

    public void onEditSegmentNavigationConsumed() {
        SegmentsScreenUiState currentState = _uiState.getValue();
        if (currentState != null && currentState.getNavigateToEditSegmentFor() != null) {
            Log.d(TAG, "onEditSegmentNavigationConsumed - Clearing edit segment dialog trigger");
            _uiState.setValue(currentState.copy(null, null, false, null, null, null, null, true, null, true)); // navigateToEditSegmentFor null olacak (clearNavigateToEditSegmentFor=true), navigateToAdd false olacak (clearNavigateToAddSegmentTrigger=true)
        }
    }
    
    public void onErrorMessageShown() {
        SegmentsScreenUiState currentState = _uiState.getValue();
        if (currentState != null && currentState.getErrorMessage() != null) {
            Log.d(TAG, "onErrorMessageShown - Clearing error message");
            _uiState.setValue(currentState.copy(null, null, true, null, null, null, null, false, null, false)); // clearErrorMessage = true
        }
    }

    // --- Formatting Helpers --- 
    private String formatDepth(double depthInFeet, UnitSystem unitSystem) {
        double displayDepth = (unitSystem == UnitSystem.METRIC) ? UnitConverter.toMeters(depthInFeet) : depthInFeet;
        String unit = (unitSystem == UnitSystem.METRIC) ? "m" : "ft";
        return String.format(Locale.getDefault(), "%.0f %s", displayDepth, unit);
    }

    private String formatTime(long totalSeconds) {
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds);
        return String.format(Locale.getDefault(), "%d min", minutes);
    }

    private String formatGas(com.burc.novadiveplannerupdated.domain.entity.Gas gas) {
        if (gas == null) return "--";
        // return gas.getGasName() + " - " + gas.getGasType().name(); // Orjinal hali
        return String.format("%s - %s", gas.getGasName(), gas.getGasType().getShortName()); // OC / CC şeklinde kısaltma
    }

    private String formatSp(double setPoint, GasType gasType) {
        if (gasType == GasType.OPEN_CIRCUIT || setPoint == 0.0) { // 0.0 da CC için geçerli bir SP olabilir mi? Şimdilik OC ise -- varsayalım.
            return "--";
        }
        return String.format(Locale.getDefault(), "%.2f", setPoint);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "onCleared called, disposing disposables.");
        disposables.clear();
    }

    public static class DisplayableSegmentItem {
        public final DiveSegment originalSegment;
        public final String segmentNumberText;
        public final String depthText;
        public final String timeText;
        public final String gasText;
        public final String spText;
        public final boolean canEdit;

        public DisplayableSegmentItem(DiveSegment originalSegment, String segmentNumberText, String depthText, String timeText, String gasText, String spText, boolean canEdit) {
            this.originalSegment = originalSegment;
            this.segmentNumberText = segmentNumberText;
            this.depthText = depthText;
            this.timeText = timeText;
            this.gasText = gasText;
            this.spText = spText;
            this.canEdit = canEdit;
        }
         @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DisplayableSegmentItem that = (DisplayableSegmentItem) o;
            return canEdit == that.canEdit &&
                    Objects.equals(originalSegment, that.originalSegment) &&
                    Objects.equals(segmentNumberText, that.segmentNumberText) &&
                    Objects.equals(depthText, that.depthText) &&
                    Objects.equals(timeText, that.timeText) &&
                    Objects.equals(gasText, that.gasText) &&
                    Objects.equals(spText, that.spText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(originalSegment, segmentNumberText, depthText, timeText, gasText, spText, canEdit);
        }
    }
} 