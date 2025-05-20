package com.burc.novadiveplannerupdated.domain.service;

import com.burc.novadiveplannerupdated.domain.common.DiveConstants;
import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;
import com.burc.novadiveplannerupdated.domain.entity.DiveSegment;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.entity.TissueCompartment;
import com.burc.novadiveplannerupdated.domain.entity.TissueState;
import com.burc.novadiveplannerupdated.domain.entity.DiveSettings;
import com.burc.novadiveplannerupdated.domain.model.DecoStop;
import com.burc.novadiveplannerupdated.domain.model.GasType;
import com.burc.novadiveplannerupdated.domain.model.GradientFactors;
import com.burc.novadiveplannerupdated.domain.model.LastStopDepthOption;
import com.burc.novadiveplannerupdated.domain.model.SegmentCalculationResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Service class implementing the Buhlmann ZHL-16c algorithm for dive calculations.
 * This service is designed to be stateless, taking all necessary inputs for calculations
 * and returning new states or results.
 */
public class BuhlmannAlgorithmService {

    private final List<TissueCompartment> allCompartments;
    private final OxygenToxicityService oxygenToxicityService;
    private final GasConsumptionService gasConsumptionService;

    /**
     * Helper data class to return multiple values from determineNextStop method.
     */
    private static class NextStopInfo {
        final double nextStopDepthFsw;
        final double overallFirstStopDepthFsw; // Tracks the deepest actual stop planned
        final boolean isFirstStopInPlan;    // True if nextStopDepthFsw is the very first deco stop

        NextStopInfo(double nextStopDepthFsw, double overallFirstStopDepthFsw, boolean isFirstStopInPlan) {
            this.nextStopDepthFsw = nextStopDepthFsw;
            this.overallFirstStopDepthFsw = overallFirstStopDepthFsw;
            this.isFirstStopInPlan = isFirstStopInPlan;
        }
    }

    /**
     * Helper data class to return multiple values from ascendToNextStop method.
     */
    private static class AscentToStopResult {
        final TissueState tissueStateAfterAscent;
        final Gas gasUsedForAscent;
        final double ascentDurationSeconds;
        final double newCurrentDepthFsw; // This will be the targetStopDepthFsw

        AscentToStopResult(TissueState tissueStateAfterAscent, Gas gasUsedForAscent, double ascentDurationSeconds, double newCurrentDepthFsw) {
            this.tissueStateAfterAscent = tissueStateAfterAscent;
            this.gasUsedForAscent = gasUsedForAscent;
            this.ascentDurationSeconds = ascentDurationSeconds;
            this.newCurrentDepthFsw = newCurrentDepthFsw;
        }
    }

    /**
     * Helper data class to return the calculated DecoStop (if any) and the final tissue state 
     * from the performDecoAtStop method.
     */
    private static class CalculatedStopDetails {
        final DecoStop decoStopEntry; // Can be null if stopDurationMinutes is 0
        final TissueState tissueStateAtEndOfProcessing;

        CalculatedStopDetails(DecoStop decoStopEntry, TissueState tissueStateAtEndOfProcessing) {
            this.decoStopEntry = decoStopEntry;
            this.tissueStateAtEndOfProcessing = Objects.requireNonNull(tissueStateAtEndOfProcessing, "tissueStateAtEndOfProcessing cannot be null");
        }
    }

    /**
     * Constructor for BuhlmannAlgorithmService.
     * Initializes the list of 17 tissue compartments based on DiveConstants.
     * This list is created once and reused for all calculations to improve efficiency.
     * It also takes instances of OxygenToxicityService and GasConsumptionService
     * for integrated calculations.
     *
     * @param oxygenToxicityService Instance of OxygenToxicityService.
     * @param gasConsumptionService Instance of GasConsumptionService.
     */
    public BuhlmannAlgorithmService(OxygenToxicityService oxygenToxicityService, GasConsumptionService gasConsumptionService) {
        this.oxygenToxicityService = Objects.requireNonNull(oxygenToxicityService, "oxygenToxicityService cannot be null");
        this.gasConsumptionService = Objects.requireNonNull(gasConsumptionService, "gasConsumptionService cannot be null");

        List<TissueCompartment> compartments = new ArrayList<>(DiveConstants.NUMBER_OF_TISSUE_COMPARTMENTS);
        for (int i = 0; i < DiveConstants.NUMBER_OF_TISSUE_COMPARTMENTS; i++) {
            compartments.add(new TissueCompartment(i));
        }
        this.allCompartments = Collections.unmodifiableList(compartments);
    }

    /**
     * Provides access to the pre-initialized list of all tissue compartments.
     * Useful if other parts of the domain logic need to iterate over them with their fixed parameters.
     *
     * @return An unmodifiable list of all 17 TissueCompartment objects.
     */
    public List<TissueCompartment> getAllCompartments() {
        return allCompartments;
    }

    // --- Core Calculation Methods (to be implemented) ---

    /**
     * Calculates the new tissue state after a specific duration at a constant depth or during a continuous ascent/descent.
     * This is the core Schreiner equation implementation.
     *
     * @param initialTissueState        The tissue state at the beginning of this calculation period.
     * @param startDepthFsw             The depth in FSW at the beginning of the period.
     * @param endDepthFsw               The depth in FSW at the end of the period.
     * @param durationSeconds           The total duration of this period in seconds.
     * @param breathingGas              The gas being breathed during this period.
     * @param initialAmbientPressureFsw The initial ambient pressure at surface/altitude for the overall dive (e.g., from AltitudeLevel).
     * @param isSurfaceInterval         True if this calculation is for a surface interval (uses surface half-times and different off-gassing logic for N2).
     * @return The new {@link TissueState} after the specified duration.
     */
    public TissueState calculateTissueStateForDuration(
            TissueState initialTissueState,
            double startDepthFsw,
            double endDepthFsw,
            double durationSeconds,
            Gas breathingGas,
            double initialAmbientPressureFsw,
            boolean isSurfaceInterval) {

        if (initialTissueState == null || breathingGas == null) {
            throw new IllegalArgumentException("Initial tissue state and breathing gas cannot be null.");
        }
        if (durationSeconds < 0) {
            throw new IllegalArgumentException("Duration cannot be negative.");
        }
        if (startDepthFsw < 0 || endDepthFsw < 0) {
            throw new IllegalArgumentException("Depth cannot be negative.");
        }

        // Initialize current tissue pressures from the initial state
        double[] currentN2PressuresFsw = initialTissueState.getNitrogenPressuresFsw();
        double[] currentHePressuresFsw = initialTissueState.getHeliumPressuresFsw();

        double fN2 = breathingGas.getFo2() + breathingGas.getFhe() > 1.0 ? 0.0 : 1.0 - breathingGas.getFo2() - breathingGas.getFhe();
        double fHe = breathingGas.getFhe();

        // Determine the time step: 1 second for depth, 60 seconds for surface interval
        double timeStepSeconds = isSurfaceInterval ? 60.0 : 1.0;
        int numberOfSteps = (int) Math.max(1, Math.round(durationSeconds / timeStepSeconds)); // Ensure at least one step for very short durations, or if duration is 0

        if (durationSeconds == 0) { // If duration is zero, no change in tissue pressures
            return new TissueState(initialTissueState);
        }
        
        for (int step = 0; step < numberOfSteps; step++) {
            double currentDepthFsw;
            // Calculate current depth for this step
            if (startDepthFsw == endDepthFsw || durationSeconds == 0) { // Constant depth or single point in time
                currentDepthFsw = startDepthFsw;
            } else { // Changing depth (ascent/descent)
                // Linear interpolation of depth for the current step
                currentDepthFsw = startDepthFsw + (endDepthFsw - startDepthFsw) * (step * timeStepSeconds) / durationSeconds;
            }
             if (isSurfaceInterval) { // if it's surface interval, depth is effectively 0 relative to P_init
                currentDepthFsw = 0;
            }


            // Total ambient pressure at current depth: P_amb_total = P_water + P_init
            // P_water is currentDepthFsw. P_init is initialAmbientPressureFsw.
            double pAmbientTotalFsw = currentDepthFsw + initialAmbientPressureFsw;

            // Inspired inert gas pressures
            double pInspiredN2 = fN2 * pAmbientTotalFsw;
            double pInspiredHe = fHe * pAmbientTotalFsw;
            
            if (isSurfaceInterval) { // On surface, assume breathing air
                pInspiredN2 = DiveConstants.FN2_IN_AIR * initialAmbientPressureFsw;
                pInspiredHe = DiveConstants.FHE_IN_AIR * initialAmbientPressureFsw; // Typically 0 for air
            }


            for (int i = 0; i < DiveConstants.NUMBER_OF_TISSUE_COMPARTMENTS; i++) {
                TissueCompartment compartment = this.allCompartments.get(i);

                // Nitrogen calculation
                double n2HalfTimeMin = isSurfaceInterval ? compartment.getN2SurfaceHalfTimeMin() : compartment.getN2HalfTimeMin();
                // Check for special surface off-gassing rule (Pelagic doc - Repetitive Diving section)
                // "for compartments i = 1 to 8 τuN2 = 86.6 ... with the exception for tension values of nitrogen less than the initial "clean" values of 0.79Pinit fsw."
                // This implies symmetric off-gassing (using depth half-time) if below initial saturation with P_init.
                // We'll simplify and use N2_SURFACE_HALF_TIMES_MIN directly as defined in DiveConstants,
                // which already has the 86.6 min for faster tissues. More complex logic can be added if strictly following that rule.
                double k_n2_per_step = DiveConstants.LN_2 / (n2HalfTimeMin * (60.0 / timeStepSeconds)); // k per timeStepSeconds
                currentN2PressuresFsw[i] = pInspiredN2 + (currentN2PressuresFsw[i] - pInspiredN2) * Math.exp(-k_n2_per_step * 1.0); // dt is 1 step

                // Helium calculation
                double heHalfTimeMin = isSurfaceInterval ? compartment.getHeSurfaceHalfTimeMin() : compartment.getHeHalfTimeMin();
                double k_he_per_step = DiveConstants.LN_2 / (heHalfTimeMin * (60.0 / timeStepSeconds)); // k per timeStepSeconds
                currentHePressuresFsw[i] = pInspiredHe + (currentHePressuresFsw[i] - pInspiredHe) * Math.exp(-k_he_per_step * 1.0); // dt is 1 step
            }
        }
        return new TissueState(currentN2PressuresFsw, currentHePressuresFsw);
    }

    /**
     * Calculates the tissue loading for an entire dive segment, which may include
     * a descent/ascent phase and a phase at constant target depth.
     *
     * @param previousSegmentEndState   Tissue state at the end of the previous segment (or initial state for the first segment).
     * @param segment                   The DiveSegment to calculate loading for.
     * @param gasForTransit             The gas used during the transit (ascent/descent) to the segment's target depth.
     *                                  This might be different from segment.getGas() if a gas switch occurs upon reaching depth.
     * @param previousDepthFsw          The depth at the end of the previous segment.
     * @param settings                  The overall dive settings, including altitude and GFs.
     * @return The new {@link TissueState} after this segment.
     * @return A {@link SegmentCalculationResult} containing the final tissue state,
     *         CNS and OTU added in this segment, gas consumed in this segment, and calculated transit duration.
     */
    public SegmentCalculationResult calculateLoadingForDiveSegment(
            TissueState previousSegmentEndState,
            DiveSegment segment,
            Gas gasForTransit, // This is the gas used FOR transit
            double previousDepthFsw,
            DiveSettings settings) {

        if (previousSegmentEndState == null || segment == null || gasForTransit == null || settings == null) {
            throw new IllegalArgumentException("All parameters for calculateLoadingForDiveSegment must be non-null.");
        }
        if (previousDepthFsw < 0) {
            throw new IllegalArgumentException("Previous depth cannot be negative.");
        }

        double initialAmbientPressureFsw = settings.getAltitudeLevel().getInitialAmbientPressureFsw();
        double targetDepthFsw = segment.getTargetDepth();
        TissueState tissueStateAfterTransit = previousSegmentEndState;

        // Variables to accumulate toxicity and gas consumption for the segment
        // These are not returned by this method in this iteration, but calculated.
        double segmentCumulativeCNS = 0;
        double segmentCumulativeOTUS = 0;
        // double segmentCumulativeOTUD = 0; // OTUD is usually tracked per dive or globally, not typically per segment directly for accumulation in this manner
        double segmentTotalGasConsumedCuft = 0;

        double rmvDiveCuFtMin = settings.getSurfaceConsumptionRates().getRmvDiveCuFtMin(); // Already in cuft/min

        // 1. Calculate Transit Phase (if there is a depth change)
        double transitDurationSeconds = 0;
        if (Math.abs(targetDepthFsw - previousDepthFsw) > 1e-6) { // Check for meaningful depth change
            double rateFpm;
            if (targetDepthFsw > previousDepthFsw) { // Descent
                rateFpm = segment.getDescentRate();
            } else { // Ascent
                rateFpm = segment.getAscentRate();
            }
            if (rateFpm <= 1e-6) {
                transitDurationSeconds = 0;
            } else {
                transitDurationSeconds = Math.abs(targetDepthFsw - previousDepthFsw) / (rateFpm / 60.0);
            }

            if (transitDurationSeconds > 0) {
                tissueStateAfterTransit = calculateTissueStateForDuration(
                        previousSegmentEndState,
                        previousDepthFsw,
                        targetDepthFsw,
                        transitDurationSeconds,
                        gasForTransit,
                        initialAmbientPressureFsw,
                        false
                );

                // Calculate toxicity and consumption for transit phase
                double averageTransitDepthFsw = (previousDepthFsw + targetDepthFsw) / 2.0;
                Double transitSetPointAta = (gasForTransit.getGasType() == GasType.CLOSED_CIRCUIT) ? segment.getSetPoint() : null;

                double transitPpo2Ata = oxygenToxicityService.calculatePpo2(
                        gasForTransit,
                        averageTransitDepthFsw,
                        initialAmbientPressureFsw,
                        transitSetPointAta
                );

                double transitCnsRate = oxygenToxicityService.calculateCnsToxicityRate(transitPpo2Ata);
                double transitRotd = oxygenToxicityService.calculateRotd(transitPpo2Ata);
                double transitRots = oxygenToxicityService.calculateRots(transitPpo2Ata, transitRotd);
                double transitDurationMinutes = transitDurationSeconds / 60.0;

                segmentCumulativeCNS += transitCnsRate * transitDurationMinutes;
                segmentCumulativeOTUS += transitRots * transitDurationMinutes;
                // segmentCumulativeOTUD += transitRotd * transitDurationMinutes;

                segmentTotalGasConsumedCuft += gasConsumptionService.calculateTotalGasConsumedCuft(
                        rmvDiveCuFtMin, // Use dive RMV for transit associated with a dive segment
                        averageTransitDepthFsw,
                        transitDurationMinutes,
                        initialAmbientPressureFsw
                );
            }
        }

        // 2. Calculate Time at Target Depth Phase
        double timeAtTargetDepthSeconds = segment.getUserInputTotalDurationInSeconds() - transitDurationSeconds;

        if (timeAtTargetDepthSeconds < 0) {
            timeAtTargetDepthSeconds = 0;
        }

        TissueState finalTissueState = tissueStateAfterTransit;
        if (timeAtTargetDepthSeconds > 0) {
            finalTissueState = calculateTissueStateForDuration(
                    tissueStateAfterTransit,
                    targetDepthFsw,
                    targetDepthFsw,
                    timeAtTargetDepthSeconds,
                    segment.getGas(), // Use the segment's designated gas
                    initialAmbientPressureFsw,
                    false
            );

            // Calculate toxicity and consumption for time at target depth
            Double targetDepthSetPointAta = (segment.getGas().getGasType() == GasType.CLOSED_CIRCUIT) ? segment.getSetPoint() : null;
            double targetDepthPpo2Ata = oxygenToxicityService.calculatePpo2(
                    segment.getGas(),
                    targetDepthFsw,
                    initialAmbientPressureFsw,
                    targetDepthSetPointAta
            );

            double targetDepthCnsRate = oxygenToxicityService.calculateCnsToxicityRate(targetDepthPpo2Ata);
            double targetDepthRotd = oxygenToxicityService.calculateRotd(targetDepthPpo2Ata);
            double targetDepthRots = oxygenToxicityService.calculateRots(targetDepthPpo2Ata, targetDepthRotd);
            double timeAtTargetDepthMinutes = timeAtTargetDepthSeconds / 60.0;

            segmentCumulativeCNS += targetDepthCnsRate * timeAtTargetDepthMinutes;
            segmentCumulativeOTUS += targetDepthRots * timeAtTargetDepthMinutes;
            // segmentCumulativeOTUD += targetDepthRotd * timeAtTargetDepthMinutes;

            segmentTotalGasConsumedCuft += gasConsumptionService.calculateTotalGasConsumedCuft(
                    rmvDiveCuFtMin, // Use dive RMV for segment time at depth
                    targetDepthFsw,
                    timeAtTargetDepthMinutes,
                    initialAmbientPressureFsw
            );
        }
        
        // TODO: Store segmentCumulativeCNS, segmentCumulativeOTUS, segmentTotalGasConsumedCuft
        // in the DiveSegment object or a calculation result object in a future step.
        // For now, they are calculated but not explicitly returned or stored beyond this method's scope.

        return new SegmentCalculationResult(
                finalTissueState,
                segmentCumulativeCNS,
                segmentCumulativeOTUS,
                segmentTotalGasConsumedCuft,
                (transitDurationSeconds > 1e-6 ? transitDurationSeconds : null) // Store null if no transit
        );
    }

    // --- Other main methods (NDL, Deco Plan etc. - to be defined later) ---

    /**
     * Calculates the No Decompression Limit (NDL) in minutes for a given depth and initial tissue state.
     * NDL is the maximum additional time a diver can stay at targetDepthFsw before requiring decompression stops
     * upon direct ascent to the surface, considering the initialAmbientPressureFsw and GFHigh.
     *
     * @param initialTissueState        The tissue state at the moment the NDL calculation begins (e.g., upon reaching targetDepthFsw).
     * @param targetDepthFsw            The constant depth for which NDL is being calculated.
     * @param bottomGas                 The gas being breathed at the targetDepthFsw.
     * @param settings                  Dive settings, including GradientFactors and potentially ascent rates.
     * @param initialAmbientPressureFsw The initial ambient pressure at the surface/altitude (P_init from AltitudeLevel).
     * @return The NDL in whole minutes.
     */
    public int calculateNdlMinutes(
            TissueState initialTissueState,
            double targetDepthFsw,
            Gas bottomGas,
            DiveSettings settings,
            double initialAmbientPressureFsw) {

        if (initialTissueState == null || bottomGas == null || settings == null) {
            throw new IllegalArgumentException("Parameters for NDL calculation cannot be null.");
        }
        if (targetDepthFsw < 0) {
            throw new IllegalArgumentException("Target depth for NDL cannot be negative.");
        }

        int ndlMinutes = 0;
        // Max search time for NDL, e.g., 5 hours. Prevents excessively long loops for very shallow depths.
        int maxNdlSearchMinutes = 300; 

        // TODO: Consider making ascent rate for NDL configurable in DiveSettings
        double ascentRateFpm = DomainDefaults.DEFAULT_ASCENT_RATE_FT_MIN;
        // Example if ascent rate were in DiveSettings (e.g., settings.getPlanningAscentRateFpm()):
        // if (settings.getPlanningAscentRateFpm() > 0) { 
        //     ascentRateFpm = settings.getPlanningAscentRateFpm();
        // }

        for (int currentBottomTimeMinutes = 0; currentBottomTimeMinutes <= maxNdlSearchMinutes; currentBottomTimeMinutes++) {
            // 1. Simulate staying at targetDepthFsw for currentBottomTimeMinutes
            TissueState tissueAfterBottomTime = calculateTissueStateForDuration(
                    initialTissueState,
                    targetDepthFsw, // startDepth is targetDepth (already there)
                    targetDepthFsw, // endDepth is targetDepth (constant depth)
                    currentBottomTimeMinutes * 60.0, // duration in seconds
                    bottomGas,
                    initialAmbientPressureFsw,
                    false // isSurfaceInterval
            );

            // 2. Simulate direct ascent to surface
            double ascentDurationSeconds = 0;
            if (targetDepthFsw > 1e-6 && ascentRateFpm > 1e-6) { // Only if there's depth and rate to ascend
                ascentDurationSeconds = (targetDepthFsw / ascentRateFpm) * 60.0;
            }

            TissueState tissueAtSurface;
            if (ascentDurationSeconds > 0) {
                tissueAtSurface = calculateTissueStateForDuration(
                        tissueAfterBottomTime, // Start from state after bottom time
                        targetDepthFsw,        // Start ascent from targetDepth
                        0.0,                   // End ascent at surface (0 fsw relative to P_init)
                        ascentDurationSeconds,
                        bottomGas,             // Assume ascent on bottom gas for NDL planning
                        initialAmbientPressureFsw,
                        false                  // Not a surface interval
                );
            } else { // No ascent needed (e.g., already at surface or zero ascent rate/depth)
                tissueAtSurface = tissueAfterBottomTime;
            }

            // 3. Check M-Values at surface for all compartments
            boolean withinLimits = true;
            for (int i = 0; i < DiveConstants.NUMBER_OF_TISSUE_COMPARTMENTS; i++) {
                TissueCompartment compartment = this.allCompartments.get(i);
                double n2AtSurface = tissueAtSurface.getNitrogenPressureFsw(i);
                double heAtSurface = tissueAtSurface.getHeliumPressureFsw(i);
                double totalInertGasAtSurface = n2AtSurface + heAtSurface;

                // M-Value at surface (P_init) using GFHigh
                double mValueSurface = calculateMValueFsw(
                        compartment,
                        n2AtSurface, // Current tissue pressures for accurate a_mix, b_mix
                        heAtSurface,
                        initialAmbientPressureFsw, // Ambient pressure at target (surface = P_init)
                        settings.getGradientFactors().getGfHigh()
                );

                if (totalInertGasAtSurface > mValueSurface) {
                    withinLimits = false;
                    break; // One compartment over limit is enough
                }
            }

            if (withinLimits) {
                ndlMinutes = currentBottomTimeMinutes;
            } else {
                // Limits exceeded, the NDL is the previous minute value
                break;
            }
        }
        return ndlMinutes;
    }

    /*
    public List<DecoStop> calculateDecommissionPlan(
            TissueState tissueStateAtEndOfBottom,
            double bottomDepthFsw,
            DiveSettings settings,
            List<Gas> availableDecoGases) {
        // TODO: Calculate deco stops if NDL is exceeded.
        // Complex logic involving finding first stop depth, stop times, gas switches.
        return Collections.emptyList(); // Placeholder
    }
    */

    // --- Helper methods for M-Values, GF etc. (to be defined later) ---

    private double getInstantaneousA(TissueCompartment compartment, double n2PressureFsw, double hePressureFsw) {
        double totalPressure = n2PressureFsw + hePressureFsw;
        if (totalPressure <= 1e-6) { // Avoid division by zero if both pressures are effectively zero
            // If only one gas has pressure, its A-value should dominate. If both are zero, return N2's A as a fallback.
            // This case should ideally not happen in normal dive calculations after initial saturation.
            if (n2PressureFsw > 1e-6) return compartment.getN2_A_fsw();
            if (hePressureFsw > 1e-6) return compartment.getHe_A_fsw();
            return compartment.getN2_A_fsw(); // Default if both are zero
        }
        return (n2PressureFsw * compartment.getN2_A_fsw() + hePressureFsw * compartment.getHe_A_fsw()) / totalPressure;
    }

    private double getInstantaneousB(TissueCompartment compartment, double n2PressureFsw, double hePressureFsw) {
        double totalPressure = n2PressureFsw + hePressureFsw;
        if (totalPressure <= 1e-6) { // Avoid division by zero
            if (n2PressureFsw > 1e-6) return compartment.getN2_B();
            if (hePressureFsw > 1e-6) return compartment.getHe_B();
            return compartment.getN2_B(); // Default if both are zero
        }
        return (n2PressureFsw * compartment.getN2_B() + hePressureFsw * compartment.getHe_B()) / totalPressure;
    }

    /**
     * Calculates the interpolated Gradient Factor (GF(D)) based on the current depth,
     * the depth of the first (deepest) decompression stop, and the dive settings (GFlow, GFhigh).
     *
     * @param currentDepthFsw           The current depth (or target depth for GF calculation) in FSW.
     * @param firstStopDepthFsw         The depth of the deepest decompression stop in FSW. For NDL calculations or
     *                                  if no deco stops are required yet, this can be considered 0 (surface),
     *                                  in which case GFhigh is used.
     * @param settings                  The dive settings containing GFlow and GFhigh.
     * @param initialAmbientPressureFsw The initial ambient pressure at the surface/altitude (P_init).
     * @return The interpolated gradient factor.
     */
    public double getInterpolatedGradientFactor(
            double currentDepthFsw,
            double firstStopDepthFsw, // DS in Pelagic formula
            DiveSettings settings,
            double initialAmbientPressureFsw) {

        double gfLow = settings.getGradientFactors().getGfLow();
        double gfHigh = settings.getGradientFactors().getGfHigh();

        if (firstStopDepthFsw <= 1e-6) { // No deco stop defined yet, or checking against surface
            return gfHigh;
        }
        
        // GF(D) = GFlow + (GFhigh - GFlow) * ( (DS - D) / DS )
        // Where D is the depth for which GF is being calculated (currentDepthFsw)
        // And DS is the first (deepest) deco stop depth (firstStopDepthFsw)
        // Note: Pelagic doc uses D in (DS-D)/DS as the depth of the *next shallower stop*,
        // implying GF is constant between stops. Or, D is the current depth for real-time GF display.
        // For M-value calculations for ascent to D_next_stop, GF at D_next_stop is used.

        // If currentDepth is deeper than or at the first stop, use GFLow.
        if (currentDepthFsw >= firstStopDepthFsw) {
            return gfLow;
        }
        // If currentDepth is at surface (0 fsw), use GFHigh.
        if (currentDepthFsw <= 1e-6) { // Effectively at surface
             return gfHigh;
        }

        // Linear interpolation between GFLow at firstStopDepthFsw and GFHigh at surface (0 fsw)
        // GF(D) = GF_high - ( (GF_high - GF_low) * D / DS_actual )
        // This is equivalent to GF_low + (GF_high - GF_low) * ( (DS_actual - D) / DS_actual )
        // where DS_actual is firstStopDepthFsw (the depth where GF_low applies) and D is currentDepthFsw.
        return gfHigh - ((gfHigh - gfLow) * currentDepthFsw / firstStopDepthFsw);
    }


    /**
     * Calculates the maximum allowable inert gas pressure (M-Value) for a given tissue compartment.
     *
     * @param compartment                   The tissue compartment.
     * @param n2PressureFsw                 Current Nitrogen pressure in the compartment (fsw).
     * @param hePressureFsw                 Current Helium pressure in the compartment (fsw).
     * @param ambientPressureAtTargetDepthFsw The ambient pressure (P_water + P_init) at the target depth/stop for which the M-Value is calculated (fsw).
     *                                        For NDL to surface, this would be P_init.
     *                                        For ascent to a deco stop, this would be P_stop_absolute.
     * @param gradientFactor                The gradient factor (GFlow, GFhigh, or interpolated GF(D)) to be applied.
     * @return The calculated M-Value in fsw.
     */
    public double calculateMValueFsw(
            TissueCompartment compartment,
            double n2PressureFsw,
            double hePressureFsw,
            double ambientPressureAtTargetDepthFsw,
            double gradientFactor) {

        double a_mix = getInstantaneousA(compartment, n2PressureFsw, hePressureFsw);
        double b_mix = getInstantaneousB(compartment, n2PressureFsw, hePressureFsw);

        if (b_mix <= 1e-6) { 
            return Double.MAX_VALUE; 
        }
        
        return ambientPressureAtTargetDepthFsw + gradientFactor * (a_mix + ambientPressureAtTargetDepthFsw * ((1.0 / b_mix) - 1.0));
    }

    /**
     * Calculates the deepest ascent ceiling across all tissue compartments based on their current loading and GFLow.
     * The ceiling is the shallowest absolute pressure a tissue can tolerate, then converted to relative depth.
     *
     * @param currentTissueState        The current state of all tissue compartments.
     * @param gradientFactors           The GFLow/GFHigh settings for the dive.
     * @param initialAmbientPressureFsw The initial ambient pressure at the surface/altitude (P_init).
     * @return The depth (fsw, relative to P_init) of the deepest ceiling. Positive if below surface, 0 or negative if at/above surface.
     */
    private double findDeepestCeilingFsw(
            TissueState currentTissueState,
            GradientFactors gradientFactors,
            double initialAmbientPressureFsw) {

        double deepestCeilingAbsoluteFsw = 0; // Smallest absolute pressure a tissue can be at (i.e. most restrictive ceiling)

        for (int i = 0; i < DiveConstants.NUMBER_OF_TISSUE_COMPARTMENTS; i++) {
            TissueCompartment compartment = this.allCompartments.get(i);
            double n2Pressure = currentTissueState.getNitrogenPressureFsw(i);
            double hePressure = currentTissueState.getHeliumPressureFsw(i);
            double totalInertGasPressure = n2Pressure + hePressure;

            double a_mix = getInstantaneousA(compartment, n2Pressure, hePressure);
            double b_mix = getInstantaneousB(compartment, n2Pressure, hePressure);
            double gfLow = gradientFactors.getGfLow();

            // Formula for absolute ceiling pressure (P_ceil_abs) from Pelagic doc (rearranged from M-value):
            // P_ceil_abs = (P_tissue - GF * a_mix) / (1 + GF * (1/b_mix - 1))
            // Where P_tissue is totalInertGasPressure.
            // We use GFLow for conservative ceiling calculation as per common practice and Pelagic page 14 discussion.
            double denominator = (1.0 + gfLow * ((1.0 / b_mix) - 1.0));

            double compartmentCeilingAbsoluteFsw;
            if (Math.abs(denominator) < 1e-9) { // Avoid division by zero if denominator is effectively zero
                // This implies b_mix is such that GF_low * (1 - 1/b_mix) approx 1. Highly unlikely.
                // If P_tissue > GF_low * a_mix, this would mean a very deep ceiling.
                // If P_tissue <= GF_low * a_mix, could mean surface is okay.
                // For safety, if denominator is zero, assume a very deep ceiling or current depth if very high P_tissue.
                // Or, more simply, if P_tissue is already low, then ceiling is high (less restrictive).
                compartmentCeilingAbsoluteFsw = (totalInertGasPressure > gfLow * a_mix) ? Double.MAX_VALUE : initialAmbientPressureFsw; 
            } else {
                compartmentCeilingAbsoluteFsw = (totalInertGasPressure - gfLow * a_mix) / denominator;
            }

            if (compartmentCeilingAbsoluteFsw > deepestCeilingAbsoluteFsw) {
                deepestCeilingAbsoluteFsw = compartmentCeilingAbsoluteFsw;
            }
        }
        // Convert absolute ceiling pressure to depth relative to P_init (surface/altitude ambient pressure)
        // Depth = P_ceil_abs - P_init.
        // If P_ceil_abs is less than P_init, it means the ceiling is at or above the surface (depth is 0 or negative).
        double deepestCeilingDepthFsw = deepestCeilingAbsoluteFsw - initialAmbientPressureFsw;
        
        return Math.max(0, deepestCeilingDepthFsw); // Return 0 if ceiling is at or above surface.
    }

    /**
     * Rounds the calculated ceiling depth (fsw) up to the next standard decompression stop depth.
     * Standard stops are typically in 10 fsw increments.
     * Considers the minimum last stop depth defined in settings.
     *
     * @param ceilingFsw     The raw calculated ceiling depth (fsw, relative to P_init).
     * @param lastStopOption The user's configured last stop depth option.
     * @return The depth of the next standard decompression stop (fsw), or 0 if ceiling is at/above surface.
     */
    private double roundToNextStandardStop(double ceilingFsw, LastStopDepthOption lastStopOption) {
        if (ceilingFsw <= 0) {
            return 0; // Ceiling is at or above the surface.
        }

        double minStopDepth = lastStopOption.getDepthFt();

        // If the ceiling is already shallower than or at the shallowest permissible stop depth,
        // the next stop is that minimum stop depth.
        if (ceilingFsw <= minStopDepth) {
            return minStopDepth;
        }

        // Otherwise, round the ceiling up to the nearest 10ft increment.
        // For example, ceiling 23ft -> 30ft. ceiling 20.1ft -> 30ft.
        return Math.ceil(ceilingFsw / 10.0) * 10.0;
    }

    /**
     * Determines the next decompression stop depth based on the current tissue state and settings.
     * It also tracks if this stop is the first in the overall deco plan and updates the
     * depth of the first actual stop encountered, which is used for GF interpolation.
     *
     * @param currentTissueState          The current tissue saturation state.
     * @param decoPlanSoFar               The list of deco stops already added to the plan.
     * @param currentOverallFirstStopFsw  The depth of the first deco stop determined so far in the plan (0 if none yet).
     * @param settings                    The dive settings.
     * @param initialAmbientPressureFsw   The initial ambient pressure at the surface/altitude.
     * @return A {@link NextStopInfo} object containing the next stop depth, updated overall first stop depth, and whether it's the first stop.
     */
    private NextStopInfo determineNextStop(
            TissueState currentTissueState,
            List<DecoStop> decoPlanSoFar,
            double currentOverallFirstStopFsw, // Pass in the value tracked by the main deco loop
            DiveSettings settings,
            double initialAmbientPressureFsw) {

        double deepestCeilingFsw = findDeepestCeilingFsw(
                currentTissueState,
                settings.getGradientFactors(),
                initialAmbientPressureFsw
        );

        double calculatedNextStopFsw;
        boolean isFirstStop = decoPlanSoFar.isEmpty();
        double updatedOverallFirstStopFsw = currentOverallFirstStopFsw;

        if (deepestCeilingFsw <= 0) {
            calculatedNextStopFsw = 0; // Indicates ascent to surface
            // If overallFirstStopFsw is still 0 (e.g. NDL dive or direct ascent from very shallow),
            // and we are going to surface, set a nominal first stop for GF calculation if needed.
            // Typically, for NDL, GFHigh is used directly. For shallow deco, it might be the last stop depth.
            if (isFirstStop && updatedOverallFirstStopFsw == 0) {
                // This case means no deco stops were previously calculated, and ceiling is now at/above surface.
                // For GF interpolation purposes during ascent check, if a very shallow stop was theoretically needed
                // but now cleared, overallFirstStop might have been the lastStopOption depth.
                // For simplicity, if going to surface and no prior stops, overallFirstStop isn't strictly needed for GF of future stops.
                // However, if the NDL calculation uses interpolated GF, this might need a value.
                // For calculating deco stops, if we reach here it implies no deco required from current state.
            }
        } else {
            calculatedNextStopFsw = roundToNextStandardStop(deepestCeilingFsw, settings.getLastStopDepthOption());
            if (isFirstStop) {
                updatedOverallFirstStopFsw = calculatedNextStopFsw;
            }
        }
        
        // Ensure that the overallFirstStopDepth is never shallower than the absolute minimum stop, even if the first calculated stop is surface.
        // This helps GF interpolation have a sensible deep anchor if the first actual stop is very shallow or surface.
        if (updatedOverallFirstStopFsw == 0 && calculatedNextStopFsw > 0) { // If first stop is not surface, but overall was 0
             updatedOverallFirstStopFsw = calculatedNextStopFsw;
        } else if (updatedOverallFirstStopFsw == 0 && calculatedNextStopFsw == 0) { // NDL case, no stops
            // For GF interpolation in NDL or direct ascent, Pelagic implies GF_High for surface M-Value.
            // If we needed an overallFirstStop for some other GF calc, it could be a nominal depth like lastStopOption.
            // Let's assume for now that if calculatedNextStopFsw is 0, overallFirstStopFsw might remain 0 or a default.
            // The getInterpolatedGradientFactor method should handle overallFirstStopFsw == 0 by returning GFHigh.
        }


        return new NextStopInfo(calculatedNextStopFsw, updatedOverallFirstStopFsw, isFirstStop);
    }

    /**
     * Simulates the ascent from a current depth to a target stop depth, calculating tissue changes.
     *
     * @param tissueStateBeforeAscent   The tissue state before starting the ascent.
     * @param currentActualDepthFsw     The actual depth (fsw) from which the ascent starts.
     * @param targetStopDepthFsw        The target depth (fsw) of the next stop (can be 0 for surface).
     * @param gasAtStartOfAscent        The gas being breathed when ascent begins.
     * @param allAvailableGases         List of all available gases for potential switch during ascent.
     * @param settings                  Dive settings, used for last stop depth and ascent rates.
     * @param initialAmbientPressureFsw Initial ambient pressure at the surface/altitude.
     * @return An {@link AscentToStopResult} object containing the new tissue state, gas used, duration, and new depth.
     */
    private AscentToStopResult ascendToNextStop(
            TissueState tissueStateBeforeAscent,
            double currentActualDepthFsw,
            double targetStopDepthFsw,
            Gas gasAtStartOfAscent,
            List<Gas> allAvailableGases,
            DiveSettings settings,
            double initialAmbientPressureFsw) {

        if (currentActualDepthFsw <= targetStopDepthFsw) {
            // Already at or shallower than the target stop depth, no ascent needed.
            return new AscentToStopResult(tissueStateBeforeAscent, gasAtStartOfAscent, 0, currentActualDepthFsw);
        }

        double ascentRateFpm;
        // Determine ascent rate:
        // If target is surface OR if current depth is already at/shallower than the configured last stop depth heading to surface.
        if (targetStopDepthFsw == 0 || currentActualDepthFsw <= settings.getLastStopDepthOption().getDepthFt()) {
            ascentRateFpm = DomainDefaults.DEFAULT_FINAL_ASCENT_RATE_FPM;
        } else {
            ascentRateFpm = DomainDefaults.DEFAULT_DECO_ASCENT_RATE_FPM;
        }

        // Select gas for ascent - typically, best gas for average depth of this ascent leg.
        Gas gasForAscent = getBestDecoGas(
                (currentActualDepthFsw + targetStopDepthFsw) / 2.0, // Average depth for this specific ascent leg
                allAvailableGases,
                gasAtStartOfAscent, // Current gas can be a candidate
                settings,
                initialAmbientPressureFsw
        );
        if (gasForAscent == null) {
            // Fallback to the gas used at the start of ascent if no better/suitable gas is found.
            // This assumes gasAtStartOfAscent is still valid; getBestDecoGas should ideally handle this.
            // If gasAtStartOfAscent itself is not valid, this could be an issue to flag.
            gasForAscent = gasAtStartOfAscent;
        }

        double ascentDurationSeconds = ((currentActualDepthFsw - targetStopDepthFsw) / ascentRateFpm) * 60.0;

        TissueState tissueStateAfterAscent = calculateTissueStateForDuration(
                tissueStateBeforeAscent,
                currentActualDepthFsw,
                targetStopDepthFsw,
                ascentDurationSeconds,
                gasForAscent,
                initialAmbientPressureFsw,
                false // Not a surface interval
        );

        return new AscentToStopResult(tissueStateAfterAscent, gasForAscent, ascentDurationSeconds, targetStopDepthFsw);
    }

    /**
     * Calculates the required duration at a specific decompression stop and the resulting tissue state.
     * It iterates minute by minute, checking if an ascent to the next shallower stop (or surface)
     * would be permissible according to M-Values and the current interpolated Gradient Factor.
     *
     * @param tissueStateAtArrivalAtStop The tissue state upon arrival at this stop.
     * @param stopDepthFsw               The depth of the current decompression stop (fsw).
     * @param gasInitiallyAtStop         The gas being breathed upon arrival at the stop.
     * @param allAvailableGases          List of all available gases for selection.
     * @param settings                   Dive settings.
     * @param initialAmbientPressureFsw  Initial ambient pressure at the surface/altitude.
     * @param overallFirstStopDepthFsw   The depth of the first actual deco stop in the entire plan (for GF interpolation).
     * @return A {@link CalculatedStopDetails} object containing the calculated DecoStop (if any) and the final tissue state.
     */
    private CalculatedStopDetails performDecoAtStop(
            TissueState tissueStateAtArrivalAtStop,
            double stopDepthFsw,
            Gas gasInitiallyAtStop,
            List<Gas> allAvailableGases,
            DiveSettings settings,
            double initialAmbientPressureFsw,
            double overallFirstStopDepthFsw) {

        Gas gasForThisStop = getBestDecoGas(
                stopDepthFsw,
                allAvailableGases,
                gasInitiallyAtStop,
                settings,
                initialAmbientPressureFsw
        );
        if (gasForThisStop == null) {
            // Fallback if no better gas found, should ideally not happen if gasInitiallyAtStop is valid
            gasForThisStop = gasInitiallyAtStop;
        }

        int calculatedStopDurationMinutes = 0;
        TissueState currentTissueStateInStop = new TissueState(tissueStateAtArrivalAtStop); // Work on a copy
        // TODO: Make maxSingleStopMinutes configurable or a constant in DomainDefaults
        int maxSingleStopMinutes = 240; // Max duration for a single stop (e.g. 4 hours)

        for (int t = 1; t <= maxSingleStopMinutes; t++) {
            // Simulate one more minute at the current stop depth
            TissueState tissueAfterOneMoreMinute = calculateTissueStateForDuration(
                    currentTissueStateInStop,
                    stopDepthFsw,
                    stopDepthFsw,
                    60.0, // 1 minute in seconds
                    gasForThisStop,
                    initialAmbientPressureFsw,
                    false // Not a surface interval
            );

            // Determine the next potential shallower stop to check ascent against
            double nextShallowStopCandidateFsw = Math.max(0, stopDepthFsw - 10.0);

            // Simulate ascent to this next shallower stop candidate
            double ascentRateForCheck;
            if (nextShallowStopCandidateFsw == 0 || stopDepthFsw <= settings.getLastStopDepthOption().getDepthFt()) {
                // If target is surface, or if current stop is the last stop type (or shallower) heading to surface
                ascentRateForCheck = DomainDefaults.DEFAULT_FINAL_ASCENT_RATE_FPM;
            } else {
                ascentRateForCheck = DomainDefaults.DEFAULT_DECO_ASCENT_RATE_FPM;
            }

            Gas gasForAscentCheck = getBestDecoGas(
                (stopDepthFsw + nextShallowStopCandidateFsw) / 2.0, // Avg depth for ascent check
                allAvailableGases,
                gasForThisStop, // Current stop gas is a candidate
                settings,
                initialAmbientPressureFsw
            );
            if (gasForAscentCheck == null) {
                gasForAscentCheck = gasForThisStop; // Fallback
            }

            double ascentToCheckDurationSec = 0;
            if (stopDepthFsw > nextShallowStopCandidateFsw && ascentRateForCheck > 0) {
                ascentToCheckDurationSec = ((stopDepthFsw - nextShallowStopCandidateFsw) / ascentRateForCheck) * 60.0;
            }

            TissueState tissueAtNextShallowCandidate;
            if (ascentToCheckDurationSec > 0) {
                tissueAtNextShallowCandidate = calculateTissueStateForDuration(
                        tissueAfterOneMoreMinute, // Start from state after this minute at stop
                        stopDepthFsw,
                        nextShallowStopCandidateFsw,
                        ascentToCheckDurationSec,
                        gasForAscentCheck,
                        initialAmbientPressureFsw,
                        false
                );
            } else {
                // No ascent needed for check (e.g. already at surface or candidate is same depth - though latter shouldn't happen)
                tissueAtNextShallowCandidate = tissueAfterOneMoreMinute;
            }

            // Check M-Values at the next shallow stop candidate
            boolean safeToAscend = true;
            for (int i = 0; i < DiveConstants.NUMBER_OF_TISSUE_COMPARTMENTS; i++) {
                TissueCompartment compartment = this.allCompartments.get(i);
                double n2AtCandidate = tissueAtNextShallowCandidate.getNitrogenPressureFsw(i);
                double heAtCandidate = tissueAtNextShallowCandidate.getHeliumPressureFsw(i);
                double totalInertGasAtCandidate = n2AtCandidate + heAtCandidate;

                // GF(D) is GF at the target depth of ascent (nextShallowStopCandidateFsw)
                double interpolatedGF = getInterpolatedGradientFactor(
                        nextShallowStopCandidateFsw,
                        overallFirstStopDepthFsw,
                        settings,
                        initialAmbientPressureFsw
                );

                double mValueAtCandidate = calculateMValueFsw(
                        compartment,
                        n2AtCandidate, heAtCandidate, // Tissue pressures in compartment for a/b mix
                        nextShallowStopCandidateFsw + initialAmbientPressureFsw, // Ambient pressure at target depth of ascent
                        interpolatedGF
                );

                if (totalInertGasAtCandidate > mValueAtCandidate) {
                    safeToAscend = false;
                    break; // One compartment over limit is enough
                }
            }

            // Update state for the current stop
            currentTissueStateInStop = tissueAfterOneMoreMinute;
            calculatedStopDurationMinutes = t;

            if (safeToAscend) {
                // It's safe to ascend after 't' minutes at this stop
                break;
            }

            if (t == maxSingleStopMinutes && !safeToAscend) {
                // Max stop time reached, and it's still not safe to ascend.
                // This is an error condition or an excessively long deco.
                // The main deco loop should handle this (e.g., by stopping planning).
                // The loop will break, returning the maxSingleStopMinutes.
                System.err.println("WARNING: Max stop time (" + maxSingleStopMinutes +
                                   " min) reached at " + stopDepthFsw + "ft and still not safe to ascend.");
            }
        }

        DecoStop decoStopEntry = null;
        if (calculatedStopDurationMinutes > 0) {
            // If a stop was calculated, create a DecoStop entry
            decoStopEntry = new DecoStop(stopDepthFsw, calculatedStopDurationMinutes, gasForThisStop);
        }

        return new CalculatedStopDetails(decoStopEntry, currentTissueStateInStop);
    }

    private Gas getBestDecoGas(
            double targetDepthFsw,
            List<Gas> allAvailableGases,
            Gas currentGas, // Can be used to prefer sticking with current gas if still optimal
            DiveSettings settings,
            double initialAmbientPressureFsw) {

        if (allAvailableGases == null || allAvailableGases.isEmpty()) {
            return currentGas; // No other gases to choose from, stick with current or return null if currentGas is also null
        }

        Gas bestGas = null;
        double bestGasHighestFo2 = -1.0; // Prioritize higher FO2
        // double bestGasLowestFHe = Double.MAX_VALUE; // Secondary criteria (optional, for ICD)

        // Global PPO2 Max from settings (e.g., 1.6 ATA as a common default/max)
        // This should ideally come from settings, but DiveSettings doesn't have a direct PO2Max for planning yet.
        // Let's use a common value or a constant for now.
        // TODO: Add a global max PPO2 setting to DiveSettings (e.g., settings.getPlanningMaxPpo2Ata())
        double globalMaxPpo2Ata = 1.6; // Default assumption

        for (Gas candidateGas : allAvailableGases) {
            if (!candidateGas.isEnabled()) { // Skip disabled gases
                continue;
            }
            
            // Calculate PPO2 of the candidate gas at targetDepthFsw
            double pAmbientTotalFsw = targetDepthFsw + initialAmbientPressureFsw;
            double ppo2AtDepthAta = candidateGas.getFo2() * (pAmbientTotalFsw / DomainDefaults.DEPTH_CONSTANT_IMPERIAL);

            // 1. Check against Gas's own PO2Max (if defined, typically for OC)
            if (candidateGas.getGasType() == GasType.OPEN_CIRCUIT && candidateGas.getPo2Max() != null) {
                if (ppo2AtDepthAta > candidateGas.getPo2Max()) {
                    continue; // Exceeds this gas's specific PO2 Max
                }
            }

            // 2. Check against global planning PO2Max
            if (ppo2AtDepthAta > globalMaxPpo2Ata) {
                continue; // Exceeds global PPO2 Max limit for deco
            }

            // 3. Check for Hypoxia (minimum PPO2)
            if (ppo2AtDepthAta < DomainDefaults.MIN_SAFE_PPO2_ATA) {
                continue; // Hypoxic at this depth
            }

            // If all checks pass, consider this gas.
            // Prioritize higher FO2. If FO2 is same, one might consider FHe (lower is better for ICD), but keep it simple for now.
            if (candidateGas.getFo2() > bestGasHighestFo2) {
                bestGasHighestFo2 = candidateGas.getFo2();
                bestGas = candidateGas;
            } else if (candidateGas.getFo2() == bestGasHighestFo2 && bestGas != null) {
                // If FO2 is the same, prefer the one with lower FHe (less narcotic, potentially better for ICD)
                if (candidateGas.getFhe() < bestGas.getFhe()) {
                    bestGas = candidateGas;
                }
            }
        }
        
        // If no suitable new gas found, but currentGas is still valid at this depth, prefer sticking to it if it's not null.
        // This avoids unnecessary gas switches if current gas is already good.
        if (bestGas == null && currentGas != null && currentGas.isEnabled()) {
            double currentGasPpo2AtDepthAta = currentGas.getFo2() * ((targetDepthFsw + initialAmbientPressureFsw) / DomainDefaults.DEPTH_CONSTANT_IMPERIAL);
            boolean currentGasModOk = true;
            if (currentGas.getGasType() == GasType.OPEN_CIRCUIT && currentGas.getPo2Max() != null) {
                if (currentGasPpo2AtDepthAta > currentGas.getPo2Max()) currentGasModOk = false;
            }
            if (currentGasPpo2AtDepthAta > globalMaxPpo2Ata) currentGasModOk = false;
            if (currentGasPpo2AtDepthAta < DomainDefaults.MIN_SAFE_PPO2_ATA) currentGasModOk = false;

            if (currentGasModOk) {
                return currentGas;
            }
        }

        return bestGas;
    }

    /**
     * Calculates the full decompression plan based on the tissue state at the end of the bottom phase.
     *
     * @param tissueStateAtEndOfBottom  The tissue saturation state at the end of the dive's bottom phase.
     * @param bottomDepthFsw            The depth (fsw) at which the bottom phase ended.
     * @param gasAtEndOfBottom          The gas being breathed at the end of the bottom phase.
     * @param settings                  The overall dive settings.
     * @param allAvailableGases         A list of all gases available for decompression.
     *                                  It's assumed this list contains only enabled gases.
     * @param initialAmbientPressureFsw The initial ambient pressure at the surface/altitude (P_init).
     * @return A list of {@link DecoStop} objects representing the decompression plan. Returns an empty list if no deco is required.
     * @throws IllegalStateException if a decompression stop duration becomes excessively long. // Or handle differently
     */
    public List<DecoStop> calculateDecompressionPlan(
            TissueState tissueStateAtEndOfBottom,
            double bottomDepthFsw,
            Gas gasAtEndOfBottom,
            DiveSettings settings,
            List<Gas> allAvailableGases,
            double initialAmbientPressureFsw) {

        List<DecoStop> decoPlan = new ArrayList<>();
        TissueState currentTissueState = new TissueState(tissueStateAtEndOfBottom); // Work on a copy
        double currentActualDepthFsw = bottomDepthFsw;
        Gas currentGasInUse = gasAtEndOfBottom;

        double overallFirstStopDepthFsw = 0; // Will be set by determineNextStop for the first actual stop

        int maxMainLoopIterations = 50; // Max number of stops/calculation cycles to prevent infinite loops
        int iteration = 0;

        while (currentActualDepthFsw > 0 && iteration < maxMainLoopIterations) {
            iteration++;

            // 1. Determine the next stop depth and update overallFirstStopDepthFsw if it's the first stop
            NextStopInfo nextStopInfo = determineNextStop(
                    currentTissueState,
                    decoPlan, // Pass the current plan to see if it's the first stop
                    overallFirstStopDepthFsw, // Pass the current known overall first stop
                    settings,
                    initialAmbientPressureFsw
            );

            double targetStopDepthFsw = nextStopInfo.nextStopDepthFsw;
            overallFirstStopDepthFsw = nextStopInfo.overallFirstStopDepthFsw; // Update with potentially new first stop

            // If the target stop is the surface (0 fsw) and we are already there or ceiling allows direct ascent
            if (targetStopDepthFsw <= 0) {
                // No more stops required, or direct ascent to surface is permissible.
                // The final ascent to surface from currentActualDepthFsw will be handled after the loop.
                break;
            }

            // Safety check: if calculated next stop is not shallower than current depth (and not the first stop)
            // This might indicate an issue or a very deep first stop scenario handled by determineNextStop.
            // For non-first stops, if next stop isn't shallower, something is wrong.
            if (!decoPlan.isEmpty() && targetStopDepthFsw >= currentActualDepthFsw) {
                System.err.println("Error: Next calculated deco stop (" + targetStopDepthFsw +
                                   "ft) is not shallower than current depth (" + currentActualDepthFsw + "ft). Aborting deco calculation.");
                // Optionally, clear decoPlan or throw an exception to indicate a failed calculation
                // decoPlan.clear(); // Or return the plan as is up to this point with a warning
                break; 
            }

            // 2. Ascend to the targetStopDepthFsw
            AscentToStopResult ascentResult = ascendToNextStop(
                    currentTissueState,
                    currentActualDepthFsw,
                    targetStopDepthFsw,
                    currentGasInUse,
                    allAvailableGases,
                    settings,
                    initialAmbientPressureFsw
            );
            currentTissueState = ascentResult.tissueStateAfterAscent;
            currentGasInUse = ascentResult.gasUsedForAscent;
            currentActualDepthFsw = ascentResult.newCurrentDepthFsw; // Should be targetStopDepthFsw
            
            // If ascent took us to surface (e.g. very shallow deco that cleared during ascent calc), break.
            if (currentActualDepthFsw <= 0) {
                break;
            }

            // 3. Perform decompression at currentActualDepthFsw (which is the targetStopDepthFsw)
            CalculatedStopDetails stopDetails = performDecoAtStop(
                    currentTissueState,
                    currentActualDepthFsw,
                    currentGasInUse,
                    allAvailableGases,
                    settings,
                    initialAmbientPressureFsw,
                    overallFirstStopDepthFsw // Crucial for GF interpolation during stop time calculation
            );

            currentTissueState = stopDetails.tissueStateAtEndOfProcessing;
            if (stopDetails.decoStopEntry != null) {
                decoPlan.add(stopDetails.decoStopEntry);
                currentGasInUse = stopDetails.decoStopEntry.getGas(); // Update current gas if stop used a different one
                
                // Check for excessively long stop from performDecoAtStop's internal check
                // performDecoAtStop logs a warning if maxSingleStopMinutes is hit and still not safe.
                // Here, we can decide to halt further planning if a stop is excessively long.
                // For now, we rely on performDecoAtStop's warning and continue building the plan.
                // A more robust error handling might throw an exception from performDecoAtStop or here.
                if (stopDetails.decoStopEntry.getDurationMinutes() >= 240) { // Example threshold from performDecoAtStop warning
                     System.err.println("CRITICAL: Deco stop at " + stopDetails.decoStopEntry.getDepthFsw() + 
                                       "ft is " + stopDetails.decoStopEntry.getDurationMinutes() + " min. Planning might be unreliable or stop.");
                    // Consider breaking the main loop or throwing an exception if policy is to stop on very long stops.
                    // throw new IllegalStateException("Decompression stop at " + stopDetails.decoStopEntry.getDepthFsw() + "ft exceeds maximum allowed time.");
                }
            } else if (stopDetails.decoStopEntry == null && currentActualDepthFsw > 0) {
                // This means performDecoAtStop determined 0 minutes were needed at this depth
                // to proceed to the next shallower stop. This is plausible if tissues cleared rapidly
                // during ascent or if the target stop was very conservative initially.
                // No stop is added to the plan, loop continues to determine next action from this depth/state.
            }
        }

        if (iteration >= maxMainLoopIterations) {
            System.err.println("Error: Maximum main deco loop iterations reached. Deco calculation aborted.");
            // Consider clearing decoPlan or throwing an exception.
        }

        // Note: The time for the final ascent from the last stop (or currentActualDepthFsw if loop broke early)
        // to the surface is not explicitly added as a DecoStop here, but it's part of the Total Time to Surface (TTS).
        // The NDL calculation includes this. For deco plans, TTS calculation would add this final leg.

        return decoPlan;
    }

} 