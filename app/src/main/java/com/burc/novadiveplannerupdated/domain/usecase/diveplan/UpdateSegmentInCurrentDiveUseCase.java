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

/**
 * Use case responsible for updating an existing DiveSegment in the current dive of the active DivePlan.
 */
public class UpdateSegmentInCurrentDiveUseCase {

    private final ActiveDivePlanRepository activeDivePlanRepository;

    @Inject
    public UpdateSegmentInCurrentDiveUseCase(ActiveDivePlanRepository activeDivePlanRepository) {
        this.activeDivePlanRepository = activeDivePlanRepository;
    }

    /**
     * Executes the use case to update an existing segment.
     *
     * @param updatedSegment The {@link DiveSegment} containing the updated information.
     *                       The segmentNumber within updatedSegment must match the segment to be updated.
     * @return A Completable that completes when the operation is successful,
     *         or emits an error if the active plan, current dive, or segment cannot be found.
     */
    public Completable execute(DiveSegment updatedSegment) {
        return activeDivePlanRepository.getActiveDivePlan()
                .firstOrError()
                .flatMapCompletable(currentDivePlan -> {
                    if (currentDivePlan == null || currentDivePlan.getDives().isEmpty()) {
                        return Completable.error(new NoSuchElementException("Active DivePlan or Dives not found."));
                    }

                    Dive currentActiveDive = currentDivePlan.getDives().get(currentDivePlan.getDives().size() - 1);
                    if (currentActiveDive == null) {
                        return Completable.error(new NoSuchElementException("Current active Dive not found in plan."));
                    }

                    List<DiveSegment> segments = new ArrayList<>(currentActiveDive.getSegments());
                    int segmentIndexToUpdate = -1;
                    for (int i = 0; i < segments.size(); i++) {
                        if (segments.get(i).getSegmentNumber() == updatedSegment.getSegmentNumber()) {
                            segmentIndexToUpdate = i;
                            break;
                        }
                    }

                    if (segmentIndexToUpdate == -1) {
                        return Completable.error(new IllegalArgumentException("Segment with number " + updatedSegment.getSegmentNumber() + " not found."));
                    }

                    // As per rules, only the last segment should be editable.
                    // This check can be enhanced or made more explicit based on exact requirements.
                    if (updatedSegment.getSegmentNumber() != segments.get(segments.size() -1).getSegmentNumber()){
                         return Completable.error(new IllegalStateException("Only the last segment can be edited."));
                    }


                    segments.set(segmentIndexToUpdate, updatedSegment);

                    Dive updatedDive = new Dive.Builder(currentActiveDive.getDiveNumber())
                            .surfaceIntervalBeforeDiveInSeconds(currentActiveDive.getSurfaceIntervalBeforeDiveInSeconds())
                            .initialTissueStateForThisDive(currentActiveDive.getInitialTissueStateForThisDive())
                            .segments(segments)
                            .build();

                    List<Dive> updatedDivesList = new ArrayList<>(currentDivePlan.getDives());
                    updatedDivesList.set(updatedDivesList.size() - 1, updatedDive);

                    DivePlan updatedDivePlan = new DivePlan.Builder(currentDivePlan.getId(), currentDivePlan.getSettings())
                            .planTitle(currentDivePlan.getPlanTitle())
                            .dives(updatedDivesList)
                            .build();

                    activeDivePlanRepository.setActiveDivePlan(updatedDivePlan);
                    return Completable.complete();
                });
    }
} 