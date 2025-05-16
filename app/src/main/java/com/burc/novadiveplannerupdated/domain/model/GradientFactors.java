package com.burc.novadiveplannerupdated.domain.model;

import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;

import java.util.Objects;

/**
 * Represents the Gradient Factors (GF Low and GF High) settings.
 * Values are stored as percentages (e.g., 30 for 30%).
 */
public class GradientFactors {
    private final int gfLow;  // Percentage value, e.g., 30 for 30%
    private final int gfHigh; // Percentage value, e.g., 85 for 85%

    /**
     * Constructs a new GradientFactors instance.
     *
     * @param gfLow The Gradient Factor Low percentage (0-100).
     * @param gfHigh The Gradient Factor High percentage (0-100).
     * @throws IllegalArgumentException if gfLow or gfHigh are outside the 0-100 range,
     *                                  or if gfLow is greater than gfHigh.
     */
    public GradientFactors(int gfLow, int gfHigh) {
        if (gfLow < DomainDefaults.MIN_GF_VALUE || gfLow > DomainDefaults.MAX_GF_VALUE) {
            throw new IllegalArgumentException("GF Low must be between " + DomainDefaults.MIN_GF_VALUE +
                " and " + DomainDefaults.MAX_GF_VALUE + ", inclusive. Was: " + gfLow);
        }
        if (gfHigh < DomainDefaults.MIN_GF_VALUE || gfHigh > DomainDefaults.MAX_GF_VALUE) {
            throw new IllegalArgumentException("GF High must be between " + DomainDefaults.MIN_GF_VALUE +
                " and " + DomainDefaults.MAX_GF_VALUE + ", inclusive. Was: " + gfHigh);
        }
        if (gfLow > gfHigh) {
            throw new IllegalArgumentException("GF Low (" + gfLow + ") cannot be greater than GF High (" + gfHigh + ").");
        }
        this.gfLow = gfLow;
        this.gfHigh = gfHigh;
    }

    public int getGfLow() {
        return gfLow;
    }

    public int getGfHigh() {
        return gfHigh;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GradientFactors that = (GradientFactors) o;
        return gfLow == that.gfLow && gfHigh == that.gfHigh;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gfLow, gfHigh);
    }

    @Override
    public String toString() {
        return "GradientFactors{" +
                "gfLow=" + gfLow +
                ", gfHigh=" + gfHigh +
                '}';
    }
} 