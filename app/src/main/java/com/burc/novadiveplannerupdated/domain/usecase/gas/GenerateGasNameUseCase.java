package com.burc.novadiveplannerupdated.domain.usecase.gas;

import com.burc.novadiveplannerupdated.domain.entity.Gas;

import java.util.Locale;

import javax.inject.Inject;

public class GenerateGasNameUseCase {

    private static final double NITROGEN_FRACTION_IN_AIR = 0.79;
    private static final double OXYGEN_FRACTION_IN_AIR = 0.21;
    private static final double EPSILON = 0.005; // For floating point comparisons

    @Inject
    public GenerateGasNameUseCase() {
    }

    public String execute(Gas gas) {
        if (gas == null) {
            return "--";
        }

        double fo2 = gas.getFo2();
        double fHe = gas.getFhe();
        double fN2 = 1.0 - fo2 - fHe;

        // Round percentages to nearest whole number for naming
        int fo2Percent = (int) Math.round(fo2 * 100);
        int fHePercent = (int) Math.round(fHe * 100);
        // int fN2Percent = (int) Math.round(fN2 * 100); // Not directly used in standard names like Trimix X/Y/Z

        // Check for Air
        if (Math.abs(fo2 - OXYGEN_FRACTION_IN_AIR) < EPSILON && Math.abs(fHe - 0.0) < EPSILON) {
            // Check if nitrogen is also close to air's nitrogen, effectively making it air.
            if (Math.abs(fN2 - (1.0 - OXYGEN_FRACTION_IN_AIR)) < EPSILON) {
                 return "AIR";
            }
        }

        // Check for Nitrox (FHe is effectively zero)
        if (Math.abs(fHe - 0.0) < EPSILON && fo2Percent > 0) {
            return String.format(Locale.US, "NX %d%%", fo2Percent);
        }

        // Check for Heliox (FN2 is effectively zero, FO2 and FHe are present)
        if (Math.abs(fN2 - 0.0) < EPSILON && fo2Percent > 0 && fHePercent > 0) {
            return String.format(Locale.US, "HX %d/%d", fo2Percent, fHePercent);
        }

        // Check for Trimix (FO2, FHe, and FN2 are all present)
        if (fo2Percent > 0 && fHePercent > 0 && fN2 > EPSILON) { // fN2 check to ensure it's not Heliox
            return String.format(Locale.US, "TX %d/%d", fo2Percent, fHePercent);
        }
        
        // Fallback for custom mixes or pure gases not fitting above categories
        if (fo2Percent == 100 && fHePercent == 0) return "OXYHEN";
        if (fHePercent == 100 && fo2Percent == 0) return "HELİUM";
        if (Math.abs(fN2 - 1.0) < EPSILON && fo2Percent == 0 && fHePercent == 0 ) return "NITROGEN";


        // Default or undefined name if no specific category fits
        // This case should ideally be handled by more specific checks or return a generic name
        return String.format(Locale.US, "CUSTOM MIX %d/%d", fo2Percent, fHePercent);
    }
} 