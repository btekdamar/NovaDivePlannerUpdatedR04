package com.burc.novadiveplannerupdated.domain.usecase.diveplan;

import com.burc.novadiveplannerupdated.domain.entity.Dive;
import com.burc.novadiveplannerupdated.domain.entity.DivePlan;
import com.burc.novadiveplannerupdated.domain.entity.DiveSettings;
import com.burc.novadiveplannerupdated.domain.entity.TissueState;
import com.burc.novadiveplannerupdated.domain.usecase.settings.GetSettingsUseCase;

import java.util.Collections;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

/**
 * Use case responsible for creating a new, empty DivePlan
 * every time the application starts or when a new plan is requested.
 * This DivePlan is not persisted locally.
 */
public class CreateNewDivePlanUseCase {

    private final GetSettingsUseCase getSettingsUseCase;

    @Inject
    public CreateNewDivePlanUseCase(GetSettingsUseCase getSettingsUseCase) {
        this.getSettingsUseCase = getSettingsUseCase;
    }

    /**
     * Executes the use case to create a new DivePlan.
     *
     * @return A Single that emits the newly created {@link DivePlan}.
     */
    public Single<DivePlan> execute() {
        return getSettingsUseCase.execute()
                .firstOrError() // GetSettingsUseCase returns Flowable, take the first emission or error
                .flatMap(diveSettings -> {
                    // 1. Determine initialAmbientPressureFsw from DiveSettings
                    double initialAmbientPressureFsw = diveSettings.getAltitudeLevel().getInitialAmbientPressureFsw();

                    // 2. Create initial TissueState
                    TissueState initialTissueState = new TissueState(initialAmbientPressureFsw);

                    // 3. Create the first Dive (empty segments)
                    Dive.Builder diveBuilder = new Dive.Builder(1) // diveNumber = 1
                            .surfaceIntervalBeforeDiveInSeconds(0)
                            .initialTissueStateForThisDive(initialTissueState)
                            .segments(Collections.emptyList()); // Empty segment list
                    Dive firstDive = diveBuilder.build();

                    // 4. Create the DivePlan
                    DivePlan.Builder divePlanBuilder = new DivePlan.Builder(diveSettings) // Plan ID is auto-generated
                            .planTitle("New Dive Plan") // Default title
                            .addDive(firstDive);
                    DivePlan newDivePlan = divePlanBuilder.build();

                    return Single.just(newDivePlan);
                });
    }
} 