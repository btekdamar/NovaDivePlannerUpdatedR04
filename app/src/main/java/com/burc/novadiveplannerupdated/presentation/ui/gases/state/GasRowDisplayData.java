package com.burc.novadiveplannerupdated.presentation.ui.gases.state;

import java.util.Objects;

public final class GasRowDisplayData {
    private final int slotNumber;
    private final boolean isEnabled;
    private final String userDefinedGasName;
    private final String calculatedStandardGasName;
    private final String fo2Text;
    private final String fHeText;
    private final String ppo2MaxText;
    private final String modText;
    private final String htText;
    private final String endLimitText;
    private final String wobLimitText;
    private final String gasTypeShortText;
    private final String tankCapacityText;
    private final String reservePercentageText;

    public GasRowDisplayData(
            int slotNumber,
            boolean isEnabled,
            String userDefinedGasName,
            String calculatedStandardGasName,
            String fo2Text,
            String fHeText,
            String ppo2MaxText,
            String modText,
            String htText,
            String endLimitText,
            String wobLimitText,
            String gasTypeShortText,
            String tankCapacityText,
            String reservePercentageText
    ) {
        this.slotNumber = slotNumber;
        this.isEnabled = isEnabled;
        this.userDefinedGasName = userDefinedGasName;
        this.calculatedStandardGasName = calculatedStandardGasName;
        this.fo2Text = fo2Text;
        this.fHeText = fHeText;
        this.ppo2MaxText = ppo2MaxText;
        this.modText = modText;
        this.htText = htText;
        this.endLimitText = endLimitText;
        this.wobLimitText = wobLimitText;
        this.gasTypeShortText = gasTypeShortText;
        this.tankCapacityText = tankCapacityText;
        this.reservePercentageText = reservePercentageText;
    }

    public int getSlotNumber() { return slotNumber; }
    public boolean isEnabled() { return isEnabled; }
    public String getUserDefinedGasName() { return userDefinedGasName; }
    public String getCalculatedStandardGasName() { return calculatedStandardGasName; }
    public String getFo2Text() { return fo2Text; }
    public String getFHeText() { return fHeText; }
    public String getPpo2MaxText() { return ppo2MaxText; }
    public String getModText() { return modText; }
    public String getHtText() { return htText; }
    public String getEndLimitText() { return endLimitText; }
    public String getWobLimitText() { return wobLimitText; }
    public String getGasTypeShortText() { return gasTypeShortText; }
    public String getTankCapacityText() { return tankCapacityText; }
    public String getReservePercentageText() { return reservePercentageText; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GasRowDisplayData that = (GasRowDisplayData) o;
        return slotNumber == that.slotNumber &&
                isEnabled == that.isEnabled &&
                Objects.equals(userDefinedGasName, that.userDefinedGasName) &&
                Objects.equals(calculatedStandardGasName, that.calculatedStandardGasName) &&
                Objects.equals(fo2Text, that.fo2Text) &&
                Objects.equals(fHeText, that.fHeText) &&
                Objects.equals(ppo2MaxText, that.ppo2MaxText) &&
                Objects.equals(modText, that.modText) &&
                Objects.equals(htText, that.htText) &&
                Objects.equals(endLimitText, that.endLimitText) &&
                Objects.equals(wobLimitText, that.wobLimitText) &&
                Objects.equals(gasTypeShortText, that.gasTypeShortText) &&
                Objects.equals(tankCapacityText, that.tankCapacityText) &&
                Objects.equals(reservePercentageText, that.reservePercentageText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slotNumber, isEnabled, userDefinedGasName, calculatedStandardGasName, fo2Text, fHeText, ppo2MaxText, modText, htText, endLimitText, wobLimitText, gasTypeShortText, tankCapacityText, reservePercentageText);
    }

    @Override
    public String toString() {
        return "GasRowDisplayData{" +
                "slotNumber=" + slotNumber +
                ", isEnabled=" + isEnabled +
                ", userDefinedGasName='" + userDefinedGasName + '\'' +
                ", calculatedStandardGasName='" + calculatedStandardGasName + '\'' +
                ", fo2Text='" + fo2Text + '\'' +
                ", fHeText='" + fHeText + '\'' +
                ", ppo2MaxText='" + ppo2MaxText + '\'' +
                ", modText='" + modText + '\'' +
                ", htText='" + htText + '\'' +
                ", endLimitText='" + endLimitText + '\'' +
                ", wobLimitText='" + wobLimitText + '\'' +
                ", gasTypeShortText='" + gasTypeShortText + '\'' +
                ", tankCapacityText='" + tankCapacityText + '\'' +
                ", reservePercentageText='" + reservePercentageText + '\'' +
                '}';
    }
} 