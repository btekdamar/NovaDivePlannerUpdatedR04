package com.burc.novadiveplannerupdated.domain.model;

import com.burc.novadiveplannerupdated.domain.entity.TissueState;
import java.util.Objects;

public class SegmentCalculationResult {

    private final TissueState tissueStateAtEndOfSegment;
    private final double cnsAddedPercent; // CNS percentage added in this segment
    private final double otusAdded;       // OTUs added in this segment
    private final double gasConsumedCuft; // Gas consumed in this segment (cuft)
    private final Double calculatedTransitDurationSeconds; // Nullable, as some segments might not have transit

    public SegmentCalculationResult(
            TissueState tissueStateAtEndOfSegment,
            double cnsAddedPercent,
            double otusAdded,
            double gasConsumedCuft,
            Double calculatedTransitDurationSeconds) {

        this.tissueStateAtEndOfSegment = Objects.requireNonNull(tissueStateAtEndOfSegment, "tissueStateAtEndOfSegment cannot be null");
        
        // Basic checks (can be made stricter if needed)
        this.cnsAddedPercent = cnsAddedPercent;
        this.otusAdded = otusAdded;
        this.gasConsumedCuft = gasConsumedCuft;
        this.calculatedTransitDurationSeconds = calculatedTransitDurationSeconds;
    }

    // Getters
    public TissueState getTissueStateAtEndOfSegment() {
        return new TissueState(tissueStateAtEndOfSegment); // Return a defensive copy
    }

    public double getCnsAddedPercent() {
        return cnsAddedPercent;
    }

    public double getOtusAdded() {
        return otusAdded;
    }

    public double getGasConsumedCuft() {
        return gasConsumedCuft;
    }

    public Double getCalculatedTransitDurationSeconds() {
        return calculatedTransitDurationSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SegmentCalculationResult that = (SegmentCalculationResult) o;
        return Double.compare(that.cnsAddedPercent, cnsAddedPercent) == 0 &&
               Double.compare(that.otusAdded, otusAdded) == 0 &&
               Double.compare(that.gasConsumedCuft, gasConsumedCuft) == 0 &&
               Objects.equals(tissueStateAtEndOfSegment, that.tissueStateAtEndOfSegment) && // TissueState.equals() is used
               Objects.equals(calculatedTransitDurationSeconds, that.calculatedTransitDurationSeconds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tissueStateAtEndOfSegment, cnsAddedPercent, otusAdded, gasConsumedCuft, calculatedTransitDurationSeconds);
    }

    @Override
    public String toString() {
        return "SegmentCalculationResult{" +
                "tissueStateAtEndOfSegmentHash=" + (tissueStateAtEndOfSegment != null ? tissueStateAtEndOfSegment.hashCode() : "null") +
                ", cnsAddedPercent=" + cnsAddedPercent +
                ", otusAdded=" + otusAdded +
                ", gasConsumedCuft=" + gasConsumedCuft +
                ", calculatedTransitDurationSeconds=" + calculatedTransitDurationSeconds +
                '}';
    }
} 