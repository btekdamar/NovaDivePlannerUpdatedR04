package com.burc.novadiveplannerupdated.domain.entity;

import com.burc.novadiveplannerupdated.domain.model.GasType;

import java.util.Objects;

public class Gas {

    private final int slotNumber; // 1-10 arası
    private final boolean isEnabled;
    private final String gasName;
    private final double fo2; // Oksijen oranı (0.07 - 1.0)
    private final double fhe; // Helyum oranı (0.0 - 0.93)
    private final Double po2Max; // OC gazlar için PPO2 üst limiti, CC için null olabilir
    private final GasType gasType; // OPEN_CIRCUIT veya CLOSED_CIRCUIT
    private final double tankCapacity;
    private final double reservePressurePercentage;

    private Gas(Builder builder) {
        this.slotNumber = builder.slotNumber;
        this.isEnabled = builder.isEnabled;
        this.gasName = builder.gasName;
        this.fo2 = builder.fo2;
        this.fhe = builder.fhe;
        this.po2Max = builder.po2Max;
        this.gasType = builder.gasType;
        this.tankCapacity = builder.tankCapacity;
        this.reservePressurePercentage = builder.reservePressurePercentage;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getGasName() {
        return gasName;
    }

    public double getFo2() {
        return fo2;
    }

    public double getFhe() {
        return fhe;
    }

    public Double getPo2Max() {
        return po2Max;
    }

    public GasType getGasType() {
        return gasType;
    }

    public double getTankCapacity() {
        return tankCapacity;
    }

    public double getReservePressurePercentage() {
        return reservePressurePercentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gas gas = (Gas) o;
        return slotNumber == gas.slotNumber &&
                isEnabled == gas.isEnabled &&
                Double.compare(gas.fo2, fo2) == 0 &&
                Double.compare(gas.fhe, fhe) == 0 &&
                Double.compare(gas.tankCapacity, tankCapacity) == 0 &&
                Double.compare(gas.reservePressurePercentage, reservePressurePercentage) == 0 &&
                Objects.equals(gasName, gas.gasName) &&
                Objects.equals(po2Max, gas.po2Max) &&
                gasType == gas.gasType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(slotNumber, isEnabled, gasName, fo2, fhe, po2Max, gasType, tankCapacity, reservePressurePercentage);
    }

    public static class Builder {
        private int slotNumber;
        private boolean isEnabled = false; // Default to false, can be overridden
        private String gasName = "";
        private double fo2 = 0.21; // Default to Air
        private double fhe = 0.0;
        private Double po2Max = 1.4; // Default PPO2 Max for OC
        private GasType gasType = GasType.OPEN_CIRCUIT; // Default to OC
        private double tankCapacity = 0.0;
        private double reservePressurePercentage = 0.0;

        public Builder() {}

        public Builder(Gas copy) {
            this.slotNumber = copy.getSlotNumber();
            this.isEnabled = copy.isEnabled();
            this.gasName = copy.getGasName();
            this.fo2 = copy.getFo2();
            this.fhe = copy.getFhe();
            this.po2Max = copy.getPo2Max();
            this.gasType = copy.getGasType();
            this.tankCapacity = copy.getTankCapacity();
            this.reservePressurePercentage = copy.getReservePressurePercentage();
        }

        public Builder slotNumber(int slotNumber) {
            this.slotNumber = slotNumber;
            return this;
        }

        public Builder isEnabled(boolean isEnabled) {
            this.isEnabled = isEnabled;
            return this;
        }

        public Builder gasName(String gasName) {
            this.gasName = gasName;
            return this;
        }

        public Builder fo2(double fo2) {
            this.fo2 = fo2;
            return this;
        }

        public Builder fhe(double fhe) {
            this.fhe = fhe;
            return this;
        }

        public Builder po2Max(Double po2Max) {
            this.po2Max = po2Max;
            return this;
        }

        public Builder gasType(GasType gasType) {
            this.gasType = gasType;
            return this;
        }

        public Builder tankCapacity(double tankCapacity) {
            this.tankCapacity = tankCapacity;
            return this;
        }

        public Builder reservePressurePercentage(double reservePressurePercentage) {
            this.reservePressurePercentage = reservePressurePercentage;
            return this;
        }

        public Gas build() {
            // Basic validation can be added here if needed
            if (gasName == null) gasName = ""; // Ensure gasName is not null
            return new Gas(this);
        }
    }
} 