package com.burc.novadiveplannerupdated.domain.usecase.diveplan;

import com.burc.novadiveplannerupdated.domain.entity.Dive;
import com.burc.novadiveplannerupdated.domain.entity.DivePlan;
import com.burc.novadiveplannerupdated.domain.entity.DiveSegment;
import com.burc.novadiveplannerupdated.domain.repository.ActiveDivePlanRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * Use case responsible for adding a new DiveSegment to the current dive in the active DivePlan.
 */
public class AddSegmentToCurrentDiveUseCase {

    private final ActiveDivePlanRepository activeDivePlanRepository;

    @Inject
    public AddSegmentToCurrentDiveUseCase(ActiveDivePlanRepository activeDivePlanRepository) {
        this.activeDivePlanRepository = activeDivePlanRepository;
    }

    /**
     * Executes the use case to add a new segment.
     *
     * @param newSegment The {@link DiveSegment} to add.
     *                   The segmentNumber within newSegment should be correctly set by the caller
     *                   (e.g., last segment number + 1).
     * @return A Completable that completes when the operation is successful,
     *         or emits an error if the active plan or current dive cannot be found,
     *         or if the segment is invalid.
     */
    public Completable execute(DiveSegment newSegment) {
        return activeDivePlanRepository.getActiveDivePlan()
                .firstOrError() // Get the current active plan once
                .flatMapCompletable(currentDivePlan -> {
                    if (currentDivePlan == null || currentDivePlan.getDives().isEmpty()) {
                        return Completable.error(new NoSuchElementException("Active DivePlan or Dives not found."));
                    }

                    // Assuming we are always working with the last dive in the plan for adding segments.
                    // Or, if multiple dives are selectable, a mechanism to identify the "current" dive is needed.
                    // For now, let's assume the last dive is the target.
                    Dive currentActiveDive = currentDivePlan.getDives().get(currentDivePlan.getDives().size() - 1);
                    if (currentActiveDive == null) {
                        return Completable.error(new NoSuchElementException("Current active Dive not found in plan."));
                    }

                    List<DiveSegment> updatedSegments = new ArrayList<>(currentActiveDive.getSegments());
                    updatedSegments.add(newSegment);

                    Dive updatedDive = new Dive.Builder(currentActiveDive.getDiveNumber())
                            .surfaceIntervalBeforeDiveInSeconds(currentActiveDive.getSurfaceIntervalBeforeDiveInSeconds())
                            .initialTissueStateForThisDive(currentActiveDive.getInitialTissueStateForThisDive()) // Should be a copy
                            .segments(updatedSegments)
                            .build();

                    List<Dive> updatedDivesList = new ArrayList<>(currentDivePlan.getDives());
                    updatedDivesList.set(updatedDivesList.size() - 1, updatedDive); // Replace the last dive

                    DivePlan updatedDivePlan = new DivePlan.Builder(currentDivePlan.getId(), currentDivePlan.getSettings())
                            .planTitle(currentDivePlan.getPlanTitle())
                            .dives(updatedDivesList)
                            .build();

                    activeDivePlanRepository.setActiveDivePlan(updatedDivePlan);
                    return Completable.complete();
                });
    }
} 