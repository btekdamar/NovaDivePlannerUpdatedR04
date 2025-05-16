package com.burc.novadiveplannerupdated.domain.usecase.gas;

import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;

import javax.inject.Inject;

public class CalculateEndAlarmDepthUseCase {

    private static final double EPSILON = 0.00001; // For floating point comparisons to avoid division by zero

    @Inject
    public CalculateEndAlarmDepthUseCase() {
    }

    /**
     * Calculates the depth at which a target Equivalent Narcotic Depth (END) is reached.
     *
     * @param gas The breathing gas.
     * @param unitSystem The unit system (METRIC or IMPERIAL).
     * @param isOxygenNarcotic True if oxygen should be considered narcotic.
     * @param targetEndDepth The target END value (in depth units corresponding to unitSystem).
     * @return The calculated depth at which the target END is reached, rounded to the nearest integer.
     *         Returns null if the target END is unachievable with the given gas (e.g., narcotic fraction is zero and target END > 0).
     *         Returns 0 if the calculated depth is negative.
     */
    public Integer execute(Gas gas, UnitSystem unitSystem, boolean isOxygenNarcotic, int targetEndDepth) {
        if (gas == null) {
            return null; // Or handle as an error
        }
        if (targetEndDepth < 0) targetEndDepth = 0; // Target END cannot be negative

        double fo2 = gas.getFo2();
        double fHe = gas.getFhe();
        double fn2 = 1.0 - fo2 - fHe;

        if (fn2 < -EPSILON) { // Allow for slight positive if fo2+fhe is a bit less than 1
            fn2 = 0;
        }
        if (fn2 < 0 && fn2 > -EPSILON) fn2 = 0; // Clamp small negatives from precision issues to 0


        double narcoticFraction;
        if (isOxygenNarcotic) {
            narcoticFraction = fn2 + fo2; // Equivalent to 1.0 - fHe
        } else {
            narcoticFraction = fn2;
        }

        // If narcotic fraction is effectively zero:
        // - If target END is positive, it's unachievable -> null
        // - If target END is zero, any depth results in END <= 0 (effectively 0). 
        //   Since there's no specific depth *limit* to reach 0 END, null is appropriate for "no limit / not applicable".
        if (Math.abs(narcoticFraction) < EPSILON) {
            return null; 
        }

        double depthConstant = (unitSystem == UnitSystem.METRIC) ?
                DomainDefaults.DEPTH_CONSTANT_METRIC :
                DomainDefaults.DEPTH_CONSTANT_IMPERIAL;

        // Depth_alarm = ((TargetEND_depth + DepthConstant) / NarcoticFraction) - DepthConstant
        double alarmDepth = (( (double)targetEndDepth + depthConstant) / narcoticFraction) - depthConstant;

        if (alarmDepth < 0) {
            // If the calculation results in a negative depth, it means the target END
            // (if positive) is met at or above the surface. For a depth *limit*,
            // 0 is the shallowest meaningful limit.
            // If targetEndDepth was 0, and alarmDepth is <0, END is already 0 or less at surface.
            return 0;
        }
        
        // As per Gas.vb, cap at a high value if it's extremely large
        // This also prevents overflow if converting to int later directly without checks
        if (alarmDepth > 2000) { // Using a practical upper limit like 2000 units (ft or m)
            return null; // Or a specific large integer if MAX_VALUE is too abstract for UI
        }

        return (int) Math.round(alarmDepth);
    }
} 