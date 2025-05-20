package com.burc.novadiveplannerupdated.presentation.ui.segments;

import androidx.annotation.Nullable;

import com.burc.novadiveplannerupdated.domain.model.UnitSystem;
import com.burc.novadiveplannerupdated.presentation.ui.segments.SegmentsViewModel.DisplayableSegmentItem;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SegmentsScreenUiState {
    private final boolean isLoading;
    @Nullable
    private final String errorMessage;
    private final List<DisplayableSegmentItem> displayableSegments;
    private final boolean isAddSegmentEnabled;
    private final UnitSystem unitSystem;

    // Navigation Triggers
    @Nullable
    private final Integer navigateToEditSegmentFor; // Segment number to edit
    private final boolean navigateToAddSegmentTrigger; // True to trigger navigation
    // private final boolean navigateToAddDiveTrigger; // For future use

    public SegmentsScreenUiState(
            boolean isLoading,
            @Nullable String errorMessage,
            List<DisplayableSegmentItem> displayableSegments,
            boolean isAddSegmentEnabled,
            UnitSystem unitSystem,
            @Nullable Integer navigateToEditSegmentFor,
            boolean navigateToAddSegmentTrigger) {
        this.isLoading = isLoading;
        this.errorMessage = errorMessage;
        this.displayableSegments = Collections.unmodifiableList(Objects.requireNonNull(displayableSegments));
        this.isAddSegmentEnabled = isAddSegmentEnabled;
        this.unitSystem = Objects.requireNonNull(unitSystem);
        this.navigateToEditSegmentFor = navigateToEditSegmentFor;
        this.navigateToAddSegmentTrigger = navigateToAddSegmentTrigger;
    }

    public boolean isLoading() { return isLoading; }
    @Nullable public String getErrorMessage() { return errorMessage; }
    public List<DisplayableSegmentItem> getDisplayableSegments() { return displayableSegments; }
    public boolean isAddSegmentEnabled() { return isAddSegmentEnabled; }
    public UnitSystem getUnitSystem() { return unitSystem; }
    @Nullable public Integer getNavigateToEditSegmentFor() { return navigateToEditSegmentFor; }
    public boolean isNavigateToAddSegmentTrigger() { return navigateToAddSegmentTrigger; }

    public static SegmentsScreenUiState initialState(UnitSystem defaultUnitSystem) {
        return new SegmentsScreenUiState(
                true, // Initially loading
                null,
                Collections.emptyList(),
                false,
                defaultUnitSystem,
                null,
                false
        );
    }

    public SegmentsScreenUiState copy(
            Boolean isLoading,
            String errorMessage, // Allow clearing by passing null
            boolean clearErrorMessage, // Explicit flag to clear error
            List<DisplayableSegmentItem> displayableSegments,
            Boolean isAddSegmentEnabled,
            UnitSystem unitSystem,
            Integer navigateToEditSegmentFor, // Allow clearing by passing null
            boolean clearNavigateToEditSegmentFor, // Explicit flag
            Boolean navigateToAddSegmentTrigger,
            boolean clearNavigateToAddSegmentTrigger // Explicit flag
    ) {
        return new SegmentsScreenUiState(
                isLoading != null ? isLoading : this.isLoading,
                clearErrorMessage ? null : (errorMessage != null ? errorMessage : this.errorMessage),
                displayableSegments != null ? displayableSegments : this.displayableSegments,
                isAddSegmentEnabled != null ? isAddSegmentEnabled : this.isAddSegmentEnabled,
                unitSystem != null ? unitSystem : this.unitSystem,
                clearNavigateToEditSegmentFor ? null : (navigateToEditSegmentFor != null ? navigateToEditSegmentFor : this.navigateToEditSegmentFor),
                clearNavigateToAddSegmentTrigger ? false : (navigateToAddSegmentTrigger != null ? navigateToAddSegmentTrigger : this.navigateToAddSegmentTrigger)
        );
    }
} 