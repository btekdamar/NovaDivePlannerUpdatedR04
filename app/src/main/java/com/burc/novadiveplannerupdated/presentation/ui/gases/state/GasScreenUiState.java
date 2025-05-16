package com.burc.novadiveplannerupdated.presentation.ui.gases.state;

import com.burc.novadiveplannerupdated.domain.model.UnitSystem;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class GasScreenUiState {
    private final boolean isLoading;
    private final String errorMessage;
    private final List<GasRowDisplayData> gasList;
    private final UnitSystem currentUnitSystem;

    public GasScreenUiState(
            boolean isLoading,
            String errorMessage,
            List<GasRowDisplayData> gasList,
            UnitSystem currentUnitSystem
    ) {
        this.isLoading = isLoading;
        this.errorMessage = errorMessage;
        this.gasList = gasList != null ? Collections.unmodifiableList(gasList) : Collections.emptyList();
        this.currentUnitSystem = currentUnitSystem;
    }

    public boolean isLoading() { return isLoading; }
    public String getErrorMessage() { return errorMessage; }
    public List<GasRowDisplayData> getGasList() { return gasList; }
    public UnitSystem getCurrentUnitSystem() { return currentUnitSystem; }

    public static GasScreenUiState initialState(UnitSystem defaultUnitSystem) {
        return new GasScreenUiState(true, null, Collections.emptyList(), defaultUnitSystem);
    }
    
    // Builder pattern or copyWith method can be useful for state updates
    public GasScreenUiState copy(
            Boolean isLoading,
            String errorMessage, // Use a wrapper like Optional<String> or a special marker for clearing vs. no change
            List<GasRowDisplayData> gasList,
            UnitSystem currentUnitSystem
    ) {
        return new GasScreenUiState(
            isLoading != null ? isLoading : this.isLoading,
            errorMessage, // This needs careful handling for clearing vs. keeping existing
            gasList != null ? gasList : this.gasList,
            currentUnitSystem != null ? currentUnitSystem : this.currentUnitSystem
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GasScreenUiState that = (GasScreenUiState) o;
        return isLoading == that.isLoading &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(gasList, that.gasList) &&
                currentUnitSystem == that.currentUnitSystem;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isLoading, errorMessage, gasList, currentUnitSystem);
    }

    @Override
    public String toString() {
        return "GasScreenUiState{" +
                "isLoading=" + isLoading +
                ", errorMessage='" + errorMessage + '\'' +
                ", gasListSize=" + (gasList != null ? gasList.size() : 0) +
                ", currentUnitSystem=" + currentUnitSystem +
                '}';
    }
} 