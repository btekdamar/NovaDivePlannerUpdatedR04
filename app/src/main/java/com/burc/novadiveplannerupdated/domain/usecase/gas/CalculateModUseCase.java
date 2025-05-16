package com.burc.novadiveplannerupdated.domain.usecase.gas;

import com.burc.novadiveplannerupdated.domain.common.DomainDefaults;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.model.UnitSystem;

import javax.inject.Inject;

public class CalculateModUseCase {

    // private static final double DEPTH_FACTOR_METRIC = 10.0; // meters per ATA
    // private static final double DEPTH_FACTOR_IMPERIAL = 33.0; // feet per ATA

    @Inject
    public CalculateModUseCase() {
    }

    public Integer execute(Gas gas, UnitSystem unitSystem) {
        if (gas == null || gas.getFo2() <= 0 || gas.getPo2Max() == null || gas.getPo2Max() <= 0) {
            return null; // MOD cannot be calculated
        }

        double fo2 = gas.getFo2();
        double po2Max = gas.getPo2Max();

        // P_amb_at_MOD (ATA) = PPO2Max / FO2
        // Depth_in_atm_gauge = P_amb_at_MOD - 1
        // MOD = Depth_in_atm_gauge * ConversionFactor
        double modCalculation = ((po2Max / fo2) - DomainDefaults.SURFACE_PRESSURE_ATA);

        if (modCalculation < 0) {
            // This can happen if po2Max < fo2 (e.g. PPO2Max = 0.21, FO2 = 1.0 for pure O2)
            // In such cases, the gas is breathable at the surface, and the MOD relative to diving is effectively 0.
            return 0;
        }

        double result;
        if (unitSystem == UnitSystem.METRIC) {
            result = modCalculation * DomainDefaults.DEPTH_CONSTANT_METRIC;
        } else {
            result = modCalculation * DomainDefaults.DEPTH_CONSTANT_IMPERIAL;
        }
        return (int) Math.floor(result);
    }
} 