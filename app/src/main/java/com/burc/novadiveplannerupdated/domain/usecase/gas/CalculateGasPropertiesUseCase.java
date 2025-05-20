package com.burc.novadiveplannerupdated.domain.usecase.gas;

import com.burc.novadiveplannerupdated.domain.entity.DiveSettings;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.model.AlarmSettings;
import com.burc.novadiveplannerupdated.domain.model.GasProperties;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;
import com.burc.novadiveplannerupdated.domain.service.GasCalculationService;

import javax.inject.Inject;

public class CalculateGasPropertiesUseCase {

    private final GenerateGasNameUseCase generateGasNameUseCase;
    private final GasCalculationService gasCalculationService;

    @Inject
    public CalculateGasPropertiesUseCase(
            GenerateGasNameUseCase generateGasNameUseCase,
            GasCalculationService gasCalculationService) {
        this.generateGasNameUseCase = generateGasNameUseCase;
        this.gasCalculationService = gasCalculationService;
    }

    public GasProperties execute(
            Gas gas,
            DiveSettings settings) {

        if (gas == null) {
            return new GasProperties("--", null, null, null, null);
        }
        if (settings == null) {
            // Or throw, or use DomainDefaults to construct a default DiveSettings
            // For now, let's assume settings are always provided or handled upstream.
            // Alternatively, return a GasProperties indicating missing settings.
            return new GasProperties("Error: Settings missing", null, null, null, null);
        }

        UnitSystem unitSystem = settings.getUnitSystem();
        AlarmSettings alarmSettings = settings.getAlarmSettings();
        boolean isOxygenNarcotic = alarmSettings.isOxygenNarcoticEnabled();
        boolean isEndAlarmEnabled = alarmSettings.isEndAlarmEnabled();
        // Assuming endAlarmThreshold is in feet from AlarmSettings, needs conversion if unitSystem is Metric
        // For now, assuming the threshold is passed correctly based on the unit system or is universal
        // This might need refinement based on how AlarmSettings stores/provides thresholds
        int endAlarmThreshold = (int) alarmSettings.getEndAlarmThresholdFt();
        boolean isWobAlarmEnabled = alarmSettings.isWobAlarmEnabled();
        int wobAlarmThreshold = (int) alarmSettings.getWobAlarmThresholdFt();

        String calculatedGasName = generateGasNameUseCase.execute(gas);

        Integer modRaw = gasCalculationService.calculateMod(gas, unitSystem);
        Double mod = null;
        if (gas.getFo2() <= 0 || gas.getPo2Max() == null || gas.getPo2Max() <= 0) {
            mod = null;
        } else if (modRaw != null) {
            mod = modRaw.doubleValue();
        }

        Integer htRaw = gasCalculationService.calculateHypoxicThreshold(gas, unitSystem);
        Double ht = (htRaw != null) ? htRaw.doubleValue() : null;

        Double endLimit = null;
        if (isEndAlarmEnabled) {
            Integer endLimitRaw = gasCalculationService.calculateEndAlarmDepth(gas, unitSystem, isOxygenNarcotic, endAlarmThreshold);
            endLimit = (endLimitRaw != null) ? endLimitRaw.doubleValue() : null;
        }

        Double wobLimit = null;
        if (isWobAlarmEnabled) {
            Integer wobLimitRaw = gasCalculationService.calculateWobAlarmDepth(gas, unitSystem, wobAlarmThreshold);
            wobLimit = (wobLimitRaw != null) ? wobLimitRaw.doubleValue() : null;
        }

        return new GasProperties(calculatedGasName, mod, ht, endLimit, wobLimit);
    }
} 