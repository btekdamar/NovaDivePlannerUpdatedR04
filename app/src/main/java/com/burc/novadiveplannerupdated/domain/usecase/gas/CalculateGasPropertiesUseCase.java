package com.burc.novadiveplannerupdated.domain.usecase.gas;

import com.burc.novadiveplannerupdated.domain.entity.DiveSettings;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.model.AlarmSettings;
import com.burc.novadiveplannerupdated.domain.model.GasProperties;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;

import javax.inject.Inject;

public class CalculateGasPropertiesUseCase {

    private final GenerateGasNameUseCase generateGasNameUseCase;
    private final CalculateModUseCase calculateModUseCase;
    private final CalculateHypoxicThresholdUseCase calculateHypoxicThresholdUseCase;
    private final CalculateEndAlarmDepthUseCase calculateEndAlarmDepthUseCase;
    private final CalculateWobAlarmDepthUseCase calculateWobAlarmDepthUseCase;

    @Inject
    public CalculateGasPropertiesUseCase(
            GenerateGasNameUseCase generateGasNameUseCase,
            CalculateModUseCase calculateModUseCase,
            CalculateHypoxicThresholdUseCase calculateHypoxicThresholdUseCase,
            CalculateEndAlarmDepthUseCase calculateEndAlarmDepthUseCase,
            CalculateWobAlarmDepthUseCase calculateWobAlarmDepthUseCase) {
        this.generateGasNameUseCase = generateGasNameUseCase;
        this.calculateModUseCase = calculateModUseCase;
        this.calculateHypoxicThresholdUseCase = calculateHypoxicThresholdUseCase;
        this.calculateEndAlarmDepthUseCase = calculateEndAlarmDepthUseCase;
        this.calculateWobAlarmDepthUseCase = calculateWobAlarmDepthUseCase;
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

        Integer modRaw = calculateModUseCase.execute(gas, unitSystem);
        Double mod = (modRaw != null) ? modRaw.doubleValue() : null;
        // Corrected MOD: if modRaw is 0 but gas is breathable, it should be 0.0, not null.
        // If fo2 is 0 or po2max is 0, calculateModUseCase returns 0, which should be null for GasProperties unless it's truly 0 depth.
        // Let's refine this: if modRaw is 0 because it's uncalculable (fo2 or po2max <=0), it's null.
        // If it calculates to 0 depth (e.g. pure O2 with PPO2Max 1.0), it should be 0.0.
        if (gas.getFo2() <= 0 || gas.getPo2Max() == null || gas.getPo2Max() <= 0) {
            mod = null;
        } else {
            mod = modRaw.doubleValue();
        }

        Integer htRaw = calculateHypoxicThresholdUseCase.execute(gas, unitSystem);
        Double ht = (htRaw != null) ? htRaw.doubleValue() : null;

        Double endLimit = null;
        if (isEndAlarmEnabled) {
            Integer endLimitRaw = calculateEndAlarmDepthUseCase.execute(gas, unitSystem, isOxygenNarcotic, endAlarmThreshold);
            endLimit = (endLimitRaw != null) ? endLimitRaw.doubleValue() : null;
        }

        Double wobLimit = null;
        if (isWobAlarmEnabled) {
            Integer wobLimitRaw = calculateWobAlarmDepthUseCase.execute(gas, unitSystem, wobAlarmThreshold);
            wobLimit = (wobLimitRaw != null) ? wobLimitRaw.doubleValue() : null;
        }

        return new GasProperties(calculatedGasName, mod, ht, endLimit, wobLimit);
    }
} 