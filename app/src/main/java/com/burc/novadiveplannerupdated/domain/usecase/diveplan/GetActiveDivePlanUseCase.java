package com.burc.novadiveplannerupdated.domain.usecase.diveplan;

import com.burc.novadiveplannerupdated.domain.entity.DivePlan;
import com.burc.novadiveplannerupdated.domain.repository.ActiveDivePlanRepository;
import javax.inject.Inject;
import io.reactivex.rxjava3.core.Observable;

/**
 * Use case responsible for providing the currently active DivePlan.
 */
public class GetActiveDivePlanUseCase {

    private final ActiveDivePlanRepository activeDivePlanRepository;

    @Inject
    public GetActiveDivePlanUseCase(ActiveDivePlanRepository activeDivePlanRepository) {
        this.activeDivePlanRepository = activeDivePlanRepository;
    }

    /**
     * Executes the use case.
     *
     * @return An Observable that emits the active {@link DivePlan} and subsequent updates to it.
     *         It will filter out null DivePlan emissions from the repository to ensure
     *         downstream consumers always receive a non-null DivePlan.
     *         If the repository's BehaviorSubject is empty or holds null initially,
     *         this Observable will wait until a non-null DivePlan is set.
     */
    public Observable<DivePlan> execute() {
        return activeDivePlanRepository.getActiveDivePlan()
                .filter(divePlan -> divePlan != null); // Null planları filtrele
    }
} 