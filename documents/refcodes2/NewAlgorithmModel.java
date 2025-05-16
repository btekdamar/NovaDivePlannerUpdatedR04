package com.example.newnovadiveplanner.Algorithm;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.newnovadiveplanner.Core.DecoStop;
import com.example.newnovadiveplanner.Core.Dive;
import com.example.newnovadiveplanner.Core.Gas;
import com.example.newnovadiveplanner.Core.Segment;
import com.example.newnovadiveplanner.Core.Settings;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class NewAlgorithmModel implements Parcelable {
    public static final Creator<NewAlgorithmModel> CREATOR = new Creator<NewAlgorithmModel>() {
        @Override
        public NewAlgorithmModel createFromParcel(Parcel in) {
            return new NewAlgorithmModel(in);
        }

        @Override
        public NewAlgorithmModel[] newArray(int size) {
            return new NewAlgorithmModel[size];
        }
    };
    private final double[] tiN2 = {5.77, 7.22, 11.5, 18.0, 26.7, 39.0, 55.3, 78.4, 111, 157, 211,
            270, 345, 440, 563, 719, 916};
    private final double[] tiSN2 = {86.6, 86.6, 86.6, 86.6, 86.6, 86.6, 86.6, 86.6, 111, 157, 211,
            270, 345, 440, 563, 719, 916};
    private final double[] tiHe = {2.18, 2.71, 4.36, 6.81, 10.1, 14.7, 20.9, 29.6, 42.0, 59.5, 79.6,
            102, 130, 166, 213, 272, 346};
    private final double[] tiSHe = {32.7, 32.7, 32.7, 32.7, 32.7, 32.7, 32.7, 32.7, 42.0, 59.5, 79.6,
            102, 130, 166, 213, 272, 346};
    private final double[] aiN2 = {41.0, 38.1, 32.6, 28.1, 24.6, 20.2, 16.4, 14.4, 13.0, 11.0, 10.0,
            9.1, 8.2, 7.5, 6.8, 6.1, 5.6};
    private final double[] aiHe = {56.7, 52.7, 45.0, 38.8, 34.1, 30.0, 26.7, 23.8, 21.2, 19.4, 18.1,
            17.4, 16.9, 16.9, 16.9, 16.8, 16.7};
    private final double[] biN2 = {0.505, 0.558, 0.651, 0.722, 0.783, 0.813, 0.843, 0.869, 0.891,
            0.909, 0.922, 0.932, 0.94, 0.948, 0.954, 0.96, 0.965};
    private final double[] biHe = {0.425, 0.477, 0.575, 0.653, 0.722, 0.758, 0.796, 0.828, 0.855,
            0.876, 0.89, 0.9, 0.907, 0.912, 0.917, 0.922, 0.927};
    private final List<NewSingleTissue> n2Tissues;
    private final List<NewSingleTissue> heTissues;
    private final List<Gas> gasList;
    private List<NewDivePoint> divePoints;
    private List<DecoStop> decoStops;
    private Settings settings;
    private final O2Toxicity o2Toxicity;
    private double globalTimer = 0;
    private double currentDepth = 0;
    private final double meterToFeet;
    private final int pressureDivider;
    private Gas bestGas;

    public NewAlgorithmModel(Settings settings, List<Gas> gasList) {
        this.settings = settings;
        this.o2Toxicity = new O2Toxicity();
        this.n2Tissues = new ArrayList<>();
        this.heTissues = new ArrayList<>();

        for (int i = 0; i < 17; i++) {
            this.n2Tissues.add(new NewSingleTissue(NewSingleTissue.NITROGEN, this.tiN2[i], this.tiSN2[i],
                    this.aiN2[i], this.biN2[i], settings.getPamb()));
            this.heTissues.add(new NewSingleTissue(NewSingleTissue.HELIUM, this.tiHe[i], this.tiSHe[i],
                    this.aiHe[i], this.biHe[i], settings.getPamb()));
        }

        this.divePoints = new ArrayList<>();
        this.decoStops = new ArrayList<>();
        this.gasList = new ArrayList<>();
        for (Gas gas : gasList) {
            this.gasList.add(new Gas(gas));
        }
        if (settings.getUnit() == Settings.METRIC) {
            this.meterToFeet = 3.2808399;
            this.pressureDivider = 10;
        } else {
            this.meterToFeet = 1;
            this.pressureDivider = 33;
        }
    }

    public NewAlgorithmModel(@NonNull Parcel in) {
        n2Tissues = in.createTypedArrayList(NewSingleTissue.CREATOR);
        heTissues = in.createTypedArrayList(NewSingleTissue.CREATOR);
        gasList = in.createTypedArrayList(Gas.CREATOR);
        divePoints = in.createTypedArrayList(NewDivePoint.CREATOR);
        decoStops = in.createTypedArrayList(DecoStop.CREATOR);

        settings = in.readParcelable(Settings.class.getClassLoader());
        o2Toxicity = in.readParcelable(O2Toxicity.class.getClassLoader());

        globalTimer = in.readDouble();
        currentDepth = in.readDouble();
        meterToFeet = in.readDouble();
        pressureDivider = in.readInt();

        bestGas = in.readParcelable(Gas.class.getClassLoader());
    }

    public NewAlgorithmModel(NewAlgorithmModel newAlgorithmModel) {
        this.settings = new Settings(newAlgorithmModel.settings);
        this.o2Toxicity = newAlgorithmModel.o2Toxicity;
        this.n2Tissues = new ArrayList<>();
        for (NewSingleTissue t : newAlgorithmModel.n2Tissues) {
            this.n2Tissues.add(new NewSingleTissue(t));
        }

        this.heTissues = new ArrayList<>();
        for (NewSingleTissue t : newAlgorithmModel.heTissues) {
            this.heTissues.add(new NewSingleTissue(t));
        }
        this.divePoints = new ArrayList<>();
        this.decoStops = new ArrayList<>();
        this.gasList = new ArrayList<>();
        for (Gas gas : newAlgorithmModel.gasList) {
            this.gasList.add(new Gas(gas));
        }
        if (settings.getUnit() == Settings.METRIC) {
            this.meterToFeet = 3.2808399;
            this.pressureDivider = 10;
        } else {
            this.meterToFeet = 1;
            this.pressureDivider = 33;
        }
        this.globalTimer = 0;
        this.currentDepth = 0;
        this.bestGas = null;
    }

    public void goToSegment(@NonNull Dive lastDive) {
        decoStops = new ArrayList<>();
        boolean isDecoDive = false;
        for (int i = lastDive.getSegmentList().size() - 1; i >= 0; i--) {
            isDecoDive = false;
            Segment segment = lastDive.getSegmentList().get(i);
            double segmentDepth = segment.getDepth() * meterToFeet;
            double segmentAscentRate = segment.getAscentRate() * meterToFeet;
            double segmentDescentRate = segment.getDescentRate() * meterToFeet;
            double prevDepth;
            if (i == lastDive.getSegmentList().size() - 1) {
                prevDepth = 0;
            } else {
                prevDepth = lastDive.getSegmentList().get(i + 1).getDepth();
            }

            if (segmentDepth > prevDepth) {
                double descSecond = getAscOrDescTimeInSec(segmentDepth - prevDepth, segmentDescentRate);
                double staySecond = segment.getTime().getSeconds() - descSecond;
                isDecoDive = runAlgorithm(prevDepth, segment, descSecond);

                if (staySecond > 0) {
                    isDecoDive = runAlgorithm(segmentDepth, segment, staySecond);
                }
            } else if (segmentDepth < prevDepth) {
                double ascSecond = getAscOrDescTimeInSec(prevDepth - segmentDepth, segmentAscentRate);
                double staySecond = segment.getTime().getSeconds() - ascSecond;

                isDecoDive = runAlgorithm(prevDepth, segment, ascSecond);

                if (staySecond > 0) {
                    isDecoDive = runAlgorithm(segmentDepth, segment, staySecond);
                }
            } else if (segmentDepth == prevDepth) {
                double staySecond = segment.getTime().getSeconds();
                isDecoDive = runAlgorithm(segmentDepth, segment, staySecond);
            }
        }

        if (isDecoDive) {
            goToSurface(lastDive.getSegmentList().get(0).getGas(), false);
        }

    }

    private boolean runAlgorithm(double prevDepth, @NonNull Segment segment, double time) {
        boolean isDecoDive = false;

        double localCurrentDepth;
        double previousDepth;
        double actualDepth;

        double segmentDepth = segment.getDepth() * meterToFeet;

        Gas currentGas = null;
        for (Gas gas : gasList) {
            if (gas.getId() == segment.getGas().getId())
                currentGas = gas;
        }

        if (settings.getWater() == Settings.FRESH) {
            localCurrentDepth = (segmentDepth / 34) * 33;
            previousDepth = (prevDepth / 34) * 33;
        } else {
            localCurrentDepth = segmentDepth;
            previousDepth = prevDepth;
        }

        double depthIteration = (localCurrentDepth - previousDepth) / time;
        double depthIterated = previousDepth;

        for (int timer = 0; timer < time; timer++) {
            for (int i = 0; i < 17; i++) {
                assert currentGas != null;
                n2Tissues.get(i).computeTension(currentGas, depthIterated);
                heTissues.get(i).computeTension(currentGas, depthIterated);

                double tensionX = n2Tissues.get(i).getTension() + heTissues.get(i).getTension();

                double aTx = ((n2Tissues.get(i).getTension() * n2Tissues.get(i).getaValue()) +
                        (heTissues.get(i).getTension() * heTissues.get(i).getaValue())) / tensionX;
                double bTx = ((n2Tissues.get(i).getTension() * n2Tissues.get(i).getbValue()) +
                        (heTissues.get(i).getTension() * heTissues.get(i).getbValue())) / tensionX;

                double piN2Surface = getPiValueWithSpeed(currentGas.getFiN2(), tiN2[i],
                        n2Tissues.get(i).getTension(), localCurrentDepth, 0, 30);
                double piHeSurface = getPiValueWithSpeed(currentGas.getFiHe(), tiHe[i],
                        heTissues.get(i).getTension(), localCurrentDepth, 0, 30);
                double tensionXSurface = piN2Surface + piHeSurface;
                double aTxSurface = ((aiHe[i] * piHeSurface) + (aiN2[i] * piN2Surface)) / tensionXSurface;
                double bTxSurface = ((biHe[i] * piHeSurface) + (biN2[i] * piN2Surface)) / tensionXSurface;

                double mValue = settings.getPamb() + (settings.getHighGF() *
                        (aTxSurface + (settings.getPamb() * ((1 / bTxSurface) - 1))));
                if (tensionXSurface > mValue) {
                    isDecoDive = true;
                }
            }
            updateGasConsumption(currentGas, depthIterated - depthIteration, depthIterated, 1);
            o2Toxicity.runAlgorithmO2Tox(currentGas.getFiO2(), depthIterated);
            if (settings.getWater() == Settings.FRESH) {
                actualDepth = Math.ceil((depthIterated * 34) / 33);
            } else {
                actualDepth = depthIterated;
            }

            if (!isDecoDive && globalTimer != 0 && globalTimer % 60 == 0 && divePoints.isEmpty()) {
                double noDecoValue = getNoDecoTime(depthIterated, currentGas);
                double gfValue = calculateActualCurrentGF(actualDepth);
                int tts = calculateTTS(n2Tissues, heTissues, 0, currentGas, actualDepth);
                NewDivePoint divePoint = new NewDivePoint(actualDepth, Duration.ofMinutes(1),
                        0, gfValue, Duration.ofSeconds((long) noDecoValue), currentGas, (int) Math.round(currentGas.getConsumption()), o2Toxicity.getO2ToxPercent(), tts);
                divePoints.add(divePoint);
            } else if (!isDecoDive && globalTimer != 0 && globalTimer % 60 == 0) {
                double noDecoValue = getNoDecoTime(depthIterated, currentGas);
                double gfValue = calculateActualCurrentGF(actualDepth);
                int tts = calculateTTS(n2Tissues, heTissues, 0, currentGas, actualDepth);
                NewDivePoint divePoint = new NewDivePoint(actualDepth,
                        divePoints.get(divePoints.size() - 1).getTime().plus(Duration.ofMinutes(1)),
                        0, gfValue, Duration.ofSeconds((long) noDecoValue), currentGas, (int) Math.round(currentGas.getConsumption()), o2Toxicity.getO2ToxPercent(), tts);
                divePoints.add(divePoint);
            } else if (isDecoDive && globalTimer != 0 && globalTimer % 60 == 0 && divePoints.isEmpty()) {
                double dsValue = getDSValue(depthIterated, currentGas);
                double gfValue = calculateActualCurrentGF(actualDepth);
                int tts = calculateTTS(n2Tissues, heTissues, (int) dsValue, currentGas, actualDepth);
                NewDivePoint divePoint = new NewDivePoint(actualDepth, Duration.ofMinutes(1),
                        dsValue, gfValue, Duration.ZERO, currentGas, (int) Math.round(currentGas.getConsumption()), o2Toxicity.getO2ToxPercent(), tts);
                divePoints.add(divePoint);
            } else if (isDecoDive && globalTimer != 0 && globalTimer % 60 == 0) {
                double dsValue = getDSValue(depthIterated, currentGas);
                double gfValue = calculateActualCurrentGF(actualDepth);
                int tts = calculateTTS(n2Tissues, heTissues, (int) dsValue, currentGas, actualDepth);
                NewDivePoint divePoint = new NewDivePoint(actualDepth,
                        divePoints.get(divePoints.size() - 1).getTime().plus(Duration.ofMinutes(1)),
                        dsValue, gfValue, Duration.ZERO, currentGas, (int) Math.round(currentGas.getConsumption()), o2Toxicity.getO2ToxPercent(), tts);
                divePoints.add(divePoint);
            }
            globalTimer++;
            depthIterated += depthIteration;
        }

        if (settings.getWater() == Settings.FRESH) {
            actualDepth = Math.ceil((depthIterated * 34) / 33);
        } else {
            actualDepth = depthIterated;
        }
        assert currentGas != null;
        updateGasConsumption(currentGas, actualDepth, actualDepth, 1);
        o2Toxicity.runAlgorithmO2Tox(currentGas.getFiO2(), depthIterated);
        if (!isDecoDive && globalTimer != 0 && globalTimer % 60 == 0 && divePoints.isEmpty()) {
            double noDecoValue = getNoDecoTime(depthIterated, currentGas);
            double gfValue = calculateActualCurrentGF(actualDepth);
            int tts = calculateTTS(n2Tissues, heTissues, 0, currentGas, actualDepth);
            NewDivePoint divePoint = new NewDivePoint(actualDepth, Duration.ofMinutes(1),
                    0, gfValue, Duration.ofSeconds((long) noDecoValue), currentGas, (int) Math.round(currentGas.getConsumption()), o2Toxicity.getO2ToxPercent(), tts);
            divePoints.add(divePoint);
        } else if (!isDecoDive && globalTimer != 0 && globalTimer % 60 == 0) {
            double noDecoValue = getNoDecoTime(depthIterated, currentGas);
            double gfValue = calculateActualCurrentGF(actualDepth);
            int tts = calculateTTS(n2Tissues, heTissues, 0, currentGas, actualDepth);
            NewDivePoint divePoint = new NewDivePoint(actualDepth,
                    divePoints.get(divePoints.size() - 1).getTime().plus(Duration.ofMinutes(1)),
                    0, gfValue, Duration.ofSeconds((long) noDecoValue), currentGas, (int) Math.round(currentGas.getConsumption()), o2Toxicity.getO2ToxPercent(), tts);
            divePoints.add(divePoint);
        } else if (isDecoDive && globalTimer != 0 && globalTimer % 60 == 0 && divePoints.isEmpty()) {
            double dsValue = getDSValue(depthIterated, currentGas);
            double gfValue = calculateActualCurrentGF(actualDepth);
            int tts = calculateTTS(n2Tissues, heTissues, (int) dsValue, currentGas, actualDepth);
            NewDivePoint divePoint = new NewDivePoint(actualDepth, Duration.ofMinutes(1),
                    dsValue, gfValue, Duration.ZERO, currentGas, (int) Math.round(currentGas.getConsumption()), o2Toxicity.getO2ToxPercent(), tts);
            divePoints.add(divePoint);
        } else if (isDecoDive && globalTimer != 0 && globalTimer % 60 == 0) {
            double dsValue = getDSValue(depthIterated, currentGas);
            double gfValue = calculateActualCurrentGF(actualDepth);
            int tts = calculateTTS(n2Tissues, heTissues, (int) dsValue, currentGas, actualDepth);
            NewDivePoint divePoint = new NewDivePoint(actualDepth,
                    divePoints.get(divePoints.size() - 1).getTime().plus(Duration.ofMinutes(1)),
                    dsValue, gfValue, Duration.ZERO, currentGas, (int) Math.round(currentGas.getConsumption()), o2Toxicity.getO2ToxPercent(), tts);
            divePoints.add(divePoint);
        }

        currentDepth = actualDepth;

        return isDecoDive;
    }

    private double getPiValueWithSpeed(double gasValue, double tauValue, double tissue,
                                       double initDepth, double finalDepth, double speed) {
        return (gasValue * (finalDepth + settings.getPamb() + (speed * tauValue))) +
                ((tissue - (gasValue * (initDepth + settings.getPamb() + (speed * tauValue)))) *
                        Math.exp((-(initDepth - finalDepth) / speed) / tauValue));
    }

    private double getAscOrDescTimeInSec(double len, double speed) {
        return ((len * 60) / speed);
    }

    public void writeTensions() {
        for (int i = 0; i < 17; i++) {
            System.out.println(i + ". n2Tissue: " + n2Tissues.get(i).getTension() +
                    " heTissue: " + heTissues.get(i).getTension());
        }
    }

    private double getNoDecoTime(double depth, Gas gas) {
        List<NewSingleTissue> localN2Tissues = new ArrayList<>();
        List<NewSingleTissue> localHeTissues = new ArrayList<>();
        for (int i = 0; i < 17; i++) {
            localN2Tissues.add(new NewSingleTissue(this.n2Tissues.get(i)));
            localHeTissues.add(new NewSingleTissue(this.heTissues.get(i)));
        }
        boolean isDecoDive = false;
        int timer;
        for (timer = 0; timer < (199 * 60) && !isDecoDive; timer++) {
            for (int i = 0; i < 17; i++) {
                localN2Tissues.get(i).computeTension(gas, depth);
                localHeTissues.get(i).computeTension(gas, depth);

                double piN2Surface = getPiValueWithSpeed(gas.getFiN2(), tiN2[i],
                        localN2Tissues.get(i).getTension(), depth, 0, 30);
                double piHeSurface = getPiValueWithSpeed(gas.getFiHe(), tiHe[i],
                        localHeTissues.get(i).getTension(), depth, 0, 30);
                double tensionXSurface = piN2Surface + piHeSurface;
                double aTxSurface = ((aiHe[i] * piHeSurface) + (aiN2[i] * piN2Surface)) / tensionXSurface;
                double bTxSurface = ((biHe[i] * piHeSurface) + (biN2[i] * piN2Surface)) / tensionXSurface;

                double mValue = settings.getPamb() + (settings.getHighGF() *
                        (aTxSurface + (settings.getPamb() * ((1 / bTxSurface) - 1))));
                if (tensionXSurface > mValue) {
                    isDecoDive = true;
                    break;
                }
            }
        }

        return timer;
    }

    public double getDSValue(double currentDepth, Gas gas) {
        int dsiValuePredictMax = (int) currentDepth;
        int dsiValuePredictMin = 0;
        double actualDS = 0;
        int dsi = (dsiValuePredictMax + dsiValuePredictMin) / 2;

        while ((dsiValuePredictMax - dsiValuePredictMin) > 1) {
            double DSValue;
            double prevDSValue = 0;
            for (int i = 0; i < 17; i++) {
                double piN2DSi = getPiValueWithSpeed(gas.getFiN2(), tiN2[i],
                        n2Tissues.get(i).getTension(), currentDepth, dsi, 30);
                double piHeDSi = getPiValueWithSpeed(gas.getFiHe(), tiHe[i],
                        heTissues.get(i).getTension(), currentDepth, dsi, 30);

                double tensionXDs = piN2DSi + piHeDSi;

                double aTxDs = ((aiHe[i] * piHeDSi) + (aiN2[i] * piN2DSi)) / tensionXDs;
                double bTxDs = ((biHe[i] * piHeDSi) + (biN2[i] * piN2DSi)) / tensionXDs;

                DSValue = (tensionXDs - this.settings.getPamb() - (this.settings.getLowGF() *
                        (aTxDs + (this.settings.getPamb() * ((1 / bTxDs) - 1))))) /
                        (1 + (this.settings.getLowGF() * ((1 / bTxDs) - 1)));
                if (prevDSValue > DSValue && DSValue > 0)
                    break;

                prevDSValue = DSValue;

                if (Math.abs(dsi - DSValue) <= 1) {
                    actualDS = Math.max(dsi, DSValue);
                }
            }
            if (prevDSValue > dsi && prevDSValue < dsiValuePredictMax) {
                dsiValuePredictMin = dsi;
            } else {
                dsiValuePredictMax = dsi;
            }
            dsi = (dsiValuePredictMax + dsiValuePredictMin) / 2;
        }

        actualDS = (int) (Math.ceil(actualDS / 10.0) * 10);
        return actualDS;
    }

    public void terminateDive(@NonNull Dive lastDive, Duration surfaceTime) {
        if (decoStops.isEmpty()) {
            Gas segmentGas = lastDive.getSegmentList().get(0).getGas();
            for (int i = 0; i < 17; i++) {
                n2Tissues.get(i).setTension(getPiValueWithSpeed(segmentGas.getFiN2(), tiN2[i],
                        n2Tissues.get(i).getTension(), currentDepth, 0, 30));
                heTissues.get(i).setTension(getPiValueWithSpeed(segmentGas.getFiHe(), tiHe[i],
                        heTissues.get(i).getTension(), currentDepth, 0, 30));
            }
        } else {
            goToSurface(bestGas, true);
            for (int i = 0; i < 17; i++) {
                n2Tissues.get(i).setTension(getPiValueWithSpeed(bestGas.getFiN2(), tiN2[i],
                        n2Tissues.get(i).getTension(), currentDepth, 0, 30));
                heTissues.get(i).setTension(getPiValueWithSpeed(bestGas.getFiHe(), tiHe[i],
                        heTissues.get(i).getTension(), currentDepth, 0, 30));
            }
        }

        List<NewDivePoint> tempList = new ArrayList<>(this.divePoints);
        lastDive.setDivePoints(tempList);

        this.divePoints = new ArrayList<>();

        writeTensions();

        for (int timer = 0; timer < surfaceTime.getSeconds(); timer++) {
            for (int i = 0; i < 17; i++) {
                n2Tissues.get(i).computeTensionsSurface();
                heTissues.get(i).computeTensionsSurface();
            }
        }

        writeTensions();
    }

    private void goToSurface(Gas activeGas, boolean isEndDive) {
        int decoDepth = (int) divePoints.get(divePoints.size() - 1).getCeiling();
        int nextDecoDepth = decoDepth - 10;
        int minDecoDepth = settings.getLastStopDepth();
        double actualDecoDepth;
        double actualNextDecoDepth;

        if (settings.getUnit() == Settings.METRIC) {
            actualDecoDepth = ((double) (decoDepth * 3) / 10) * meterToFeet;
        } else {
            actualDecoDepth = decoDepth;
        }

        if (settings.getWater() == Settings.FRESH) {
            actualDecoDepth = (actualDecoDepth / 34) * 33;
        }
        List<NewSingleTissue> localN2Tissues = new ArrayList<>();
        List<NewSingleTissue> localHeTissues = new ArrayList<>();
        for (int i = 0; i < 17; i++) {
            localN2Tissues.add(new NewSingleTissue(this.n2Tissues.get(i)));
            localHeTissues.add(new NewSingleTissue(this.heTissues.get(i)));
            localN2Tissues.get(i).setTension(getPiValueWithSpeed(activeGas.getFiN2(), tiN2[i],
                    localN2Tissues.get(i).getTension(), currentDepth, actualDecoDepth, 30));
            localHeTissues.get(i).setTension(getPiValueWithSpeed(activeGas.getFiHe(), tiHe[i],
                    localHeTissues.get(i).getTension(), currentDepth, actualDecoDepth, 30));
        }
        int timer;
        while (decoDepth >= minDecoDepth) {
            if (settings.getUnit() == Settings.METRIC) {
                actualDecoDepth = ((double) (decoDepth * 3) / 10) * meterToFeet;
                actualNextDecoDepth = ((double) (nextDecoDepth * 3) / 10) * meterToFeet;
            } else {
                actualDecoDepth = decoDepth;
                actualNextDecoDepth = nextDecoDepth;
            }
            if (settings.getWater() == Settings.FRESH) {
                actualDecoDepth = (actualDecoDepth / 34) * 33;
                actualNextDecoDepth = (actualNextDecoDepth / 34) * 33;
            }

            setBestGas(actualDecoDepth, activeGas);

            double gfValue = settings.getLowGF() + (settings.getHighGF() - settings.getLowGF()) *
                    ((actualDecoDepth - actualNextDecoDepth) / actualDecoDepth);
            int decoTime = 0;
            for (int i = 0; i < 17; i++) {
                double minTimer = 2;
                double maxTimer = 30000;
                double midTimer;
                double tolerance = 0.00001;

                double ptxDecoStop = 1;
                double miValue = 0;

                double basePressureN2 = bestGas.getFiN2() * (actualDecoDepth + settings.getPamb());
                double basePressureHe = bestGas.getFiHe() * (actualDecoDepth + settings.getPamb());
                double adjustedDepth = actualDecoDepth + (settings.getPamb() - 10);

                while (Math.abs(ptxDecoStop - miValue) > tolerance && minTimer < maxTimer) {
                    midTimer = (minTimer + maxTimer) / 2.0;

                    double expN2 = Math.exp((-1.0 * midTimer) / (60 * tiN2[i]));
                    double expHe = Math.exp((-1.0 * midTimer) / (60 * tiHe[i]));

                    double piN2DecoStop = basePressureN2 +
                            (localN2Tissues.get(i).getTension() - basePressureN2) * expN2;
                    double piHeDecoStop = basePressureHe +
                            (localHeTissues.get(i).getTension() - basePressureHe) * expHe;

                    double piN2NextStop = getPiValueWithSpeed(bestGas.getFiN2(), tiN2[i],
                            piN2DecoStop, actualDecoDepth, actualNextDecoDepth, 30);
                    double piHeNextStop = getPiValueWithSpeed(bestGas.getFiHe(), tiHe[i],
                            piHeDecoStop, actualDecoDepth, actualNextDecoDepth, 30);

                    ptxDecoStop = piN2NextStop + piHeNextStop;

                    double aiTx = ((piN2NextStop * aiN2[i]) + (piHeNextStop * aiHe[i])) / ptxDecoStop;
                    double biTx = ((piN2NextStop * biN2[i]) + (piHeNextStop * biHe[i])) / ptxDecoStop;

                    miValue = adjustedDepth + (gfValue * (aiTx + adjustedDepth * ((1.0 / biTx) - 1)));

                    if (ptxDecoStop > miValue) {
                        minTimer = midTimer;
                    } else {
                        maxTimer = midTimer;
                    }
                }

                int finalTimer = (int) Math.round((minTimer + maxTimer) / 2.0);

                if (finalTimer > decoTime) {
                    decoTime = finalTimer;
                }
            }

            int depthForUnit = decoDepth;
            if (settings.getUnit() == Settings.METRIC) {
                depthForUnit = (decoDepth * 3) / 10;
            }
            if (decoTime != 1) {
                if (decoTime % 60 != 0) {
                    decoTime = ((decoTime / 60) + 1) * 60;
                }
                DecoStop decoStop = new DecoStop(depthForUnit, Duration.ofSeconds(decoTime), bestGas);
                decoStops.add(decoStop);
                Duration pointTime = divePoints.get(divePoints.size() - 1).getTime();
                for (timer = 0; timer < decoTime; timer++) {
                    for (int i = 0; i < 17; i++) {
                        localN2Tissues.get(i).computeTension(bestGas, actualDecoDepth);
                        localHeTissues.get(i).computeTension(bestGas, actualDecoDepth);
                    }
                    updateGasConsumption(bestGas, actualDecoDepth, actualDecoDepth, 1);
                    o2Toxicity.runAlgorithmO2Tox(bestGas.getFiO2(), actualDecoDepth);
                    globalTimer++;
                    if (globalTimer % 60 == 0) {
                        double gfLocal = settings.getLowGF() + (settings.getHighGF() - settings.getLowGF()) *
                                ((actualDecoDepth - actualNextDecoDepth) / actualDecoDepth);
                        pointTime = pointTime.plus(Duration.ofMinutes(1));
                        int ttsValue = calculateTTS(localN2Tissues, localHeTissues, decoDepth, bestGas, actualDecoDepth);
                        NewDivePoint divePoint = new NewDivePoint(actualDecoDepth, pointTime,
                                actualNextDecoDepth, gfLocal, Duration.ZERO, bestGas, (int) Math.round(bestGas.getConsumption()), o2Toxicity.getO2ToxPercent(), ttsValue);
                        divePoints.add(divePoint);
                    }
                }

                for (int i = 0; i < 17; i++) {
                    localN2Tissues.get(i).setTension(getPiValueWithSpeed(bestGas.getFiN2(), tiN2[i],
                            localN2Tissues.get(i).getTension(), actualDecoDepth, actualNextDecoDepth, 30));
                    localHeTissues.get(i).setTension(getPiValueWithSpeed(bestGas.getFiHe(), tiHe[i],
                            localHeTissues.get(i).getTension(), actualDecoDepth, actualNextDecoDepth, 30));
                }
                double ascTime = getAscOrDescTimeInSec(actualDecoDepth - actualNextDecoDepth, 30);
                updateGasConsumption(bestGas, actualDecoDepth, actualNextDecoDepth, ascTime);
            }
            decoDepth -= 10;
            nextDecoDepth = decoDepth - 10;
        }
        if (isEndDive) {
            currentDepth = decoDepth + 10;
            for (int i = 0; i < 17; i++) {
                n2Tissues.add(new NewSingleTissue(localN2Tissues.get(i)));
                heTissues.add(new NewSingleTissue(localHeTissues.get(i)));
            }
        }
    }

    private double calculateActualCurrentGF(double depth) {
        double maxGFi = 0;
        for (int i = 0; i < 17; i++) {
            double gfi = tissueGF(n2Tissues.get(i), heTissues.get(i), depth);
            if (i == 0) {
                maxGFi = gfi;
            } else if (gfi > maxGFi) {
                maxGFi = gfi;
            }
        }
        return maxGFi;
    }

    private int calculateTTS(List<NewSingleTissue> n2Tissues, List<NewSingleTissue> heTissues, int dsValue, Gas activeGas, double depth) {
        double ttsValue = 0;
        int decoDepth = dsValue;
        int nextDecoDepth;
        if (decoDepth == 0) {
            nextDecoDepth = 0;
        } else {
            nextDecoDepth = decoDepth - 10;
        }
        int minDecoDepth = settings.getLastStopDepth();
        double actualDecoDepth;
        double actualNextDecoDepth;

        if (settings.getUnit() == Settings.METRIC) {
            actualDecoDepth = ((double) (decoDepth * 3) / 10) * meterToFeet;
        } else {
            actualDecoDepth = decoDepth;
        }

        ttsValue += ((depth - actualDecoDepth) * (1 / 30.0));

        if (settings.getWater() == Settings.FRESH) {
            actualDecoDepth = (actualDecoDepth / 34) * 33;
        }
        List<NewSingleTissue> localN2Tissues = new ArrayList<>();
        List<NewSingleTissue> localHeTissues = new ArrayList<>();
        for (int i = 0; i < 17; i++) {
            localN2Tissues.add(new NewSingleTissue(n2Tissues.get(i)));
            localHeTissues.add(new NewSingleTissue(heTissues.get(i)));
            localN2Tissues.get(i).setTension(getPiValueWithSpeed(activeGas.getFiN2(), tiN2[i],
                    localN2Tissues.get(i).getTension(), depth, actualDecoDepth, 30));
            localHeTissues.get(i).setTension(getPiValueWithSpeed(activeGas.getFiHe(), tiHe[i],
                    localHeTissues.get(i).getTension(), depth, actualDecoDepth, 30));
        }

        while (decoDepth >= minDecoDepth) {
            if (settings.getUnit() == Settings.METRIC) {
                actualDecoDepth = ((double) (decoDepth * 3) / 10) * meterToFeet;
                actualNextDecoDepth = ((double) (nextDecoDepth * 3) / 10) * meterToFeet;
            } else {
                actualDecoDepth = decoDepth;
                actualNextDecoDepth = nextDecoDepth;
            }
            if (settings.getWater() == Settings.FRESH) {
                actualDecoDepth = (actualDecoDepth / 34) * 33;
                actualNextDecoDepth = (actualNextDecoDepth / 34) * 33;
            }

            setBestGas(actualDecoDepth, activeGas);

            double gfValue = settings.getLowGF() + (settings.getHighGF() - settings.getLowGF()) *
                    ((actualDecoDepth - actualNextDecoDepth) / actualDecoDepth);
            int decoTime = 0;
            for (int i = 0; i < 17; i++) {
                double minTimer = 1;
                double maxTimer = 30000;
                double midTimer;
                double tolerance = 0.00001;

                double ptxDecoStop = 1;
                double miValue = 0;

                double basePressureN2 = bestGas.getFiN2() * (actualDecoDepth + settings.getPamb());
                double basePressureHe = bestGas.getFiHe() * (actualDecoDepth + settings.getPamb());
                double adjustedDepth = actualDecoDepth + (settings.getPamb() - 10);

                while (Math.abs(ptxDecoStop - miValue) > tolerance && minTimer < maxTimer) {
                    midTimer = (minTimer + maxTimer) / 2.0;

                    double expN2 = Math.exp((-1.0 * midTimer) / (60 * tiN2[i]));
                    double expHe = Math.exp((-1.0 * midTimer) / (60 * tiHe[i]));

                    double piN2DecoStop = basePressureN2 +
                            (localN2Tissues.get(i).getTension() - basePressureN2) * expN2;
                    double piHeDecoStop = basePressureHe +
                            (localHeTissues.get(i).getTension() - basePressureHe) * expHe;

                    double piN2NextStop = getPiValueWithSpeed(bestGas.getFiN2(), tiN2[i],
                            piN2DecoStop, actualDecoDepth, actualNextDecoDepth, 30);
                    double piHeNextStop = getPiValueWithSpeed(bestGas.getFiHe(), tiHe[i],
                            piHeDecoStop, actualDecoDepth, actualNextDecoDepth, 30);

                    ptxDecoStop = piN2NextStop + piHeNextStop;

                    double aiTx = ((piN2NextStop * aiN2[i]) + (piHeNextStop * aiHe[i])) / ptxDecoStop;
                    double biTx = ((piN2NextStop * biN2[i]) + (piHeNextStop * biHe[i])) / ptxDecoStop;

                    miValue = adjustedDepth + (gfValue * (aiTx + adjustedDepth * ((1.0 / biTx) - 1)));

                    if (ptxDecoStop > miValue) {
                        minTimer = midTimer;
                    } else {
                        maxTimer = midTimer;
                    }
                }

                int finalTimer = (int) Math.round((minTimer + maxTimer) / 2.0);

                if (finalTimer > decoTime) {
                    decoTime = finalTimer;
                }
            }
            ttsValue += (decoTime / 60.0);
            for (int i = 0; i < 17; i++) {
                localN2Tissues.get(i).computeTensionDirect(bestGas, actualDecoDepth, decoTime);
                localHeTissues.get(i).computeTensionDirect(bestGas, actualDecoDepth, decoTime);
            }

            for (int i = 0; i < 17; i++) {
                localN2Tissues.get(i).setTension(getPiValueWithSpeed(bestGas.getFiN2(), tiN2[i],
                        localN2Tissues.get(i).getTension(), actualDecoDepth, actualNextDecoDepth, 30));
                localHeTissues.get(i).setTension(getPiValueWithSpeed(bestGas.getFiHe(), tiHe[i],
                        localHeTissues.get(i).getTension(), actualDecoDepth, actualNextDecoDepth, 30));
            }
            decoDepth -= 10;
            nextDecoDepth = decoDepth - 10;

            ttsValue += (10 * (1 / 30.0));
        }

        return (int) Math.ceil(ttsValue);
    }

    private double tissueGF(NewSingleTissue n2Tissue, NewSingleTissue heTissue, double depth) {
        double pi = n2Tissue.getTension() + heTissue.getTension();
        double ai = ((n2Tissue.getTension() * n2Tissue.getaValue()) + (heTissue.getTension() * heTissue.getaValue())) / pi;
        double bi = ((n2Tissue.getTension() * n2Tissue.getbValue()) + (heTissue.getTension() * heTissue.getbValue())) / pi;
        double mi = ai + ((depth + settings.getPamb()) / bi);

        return (pi - depth - settings.getPamb()) / (mi - depth - settings.getPamb());
    }

    private void setBestGas(double decoDepth, @NonNull Gas activeGas) {
        double po2BestMix = (1 - (activeGas.getFiN2() + activeGas.getFiHe())) * (settings.getPamb() + decoDepth);
        bestGas = activeGas;
        for (Gas gas : gasList) {
            if (gas.isActive()) {
                double po2Current = gas.getFiO2() * (settings.getPamb() + decoDepth);
                double modCurrent = ((gas.getMaxPO2() * 33) / gas.getFiO2()) - settings.getPamb();

                int modifiedMod = (int) Math.ceil(modCurrent);

                if (decoDepth <= modifiedMod && po2Current > po2BestMix) {
                    bestGas = gas;
                    po2BestMix = bestGas.getFiO2() * (settings.getPamb() + decoDepth);
                }
            }
        }
    }

    private void updateGasConsumption(Gas currentGas, double prevDepth, double currentDepth, double elapsedTime) {
        for (Gas gas : gasList) {
            if (gas == currentGas) {
                double consumption = gas.getConsumption();
                if (prevDepth == currentDepth) {
                    consumption += ((currentDepth / pressureDivider) + 1) * (elapsedTime / 60) * settings.getDiveSAC();
                } else if (currentDepth > prevDepth) {
                    consumption += ((((currentDepth + prevDepth) / 2) / pressureDivider) + 1) * (elapsedTime / 60) * settings.getDiveSAC();
                } else {
                    consumption += ((((prevDepth - currentDepth) / 2) / pressureDivider) + 1) * (elapsedTime / 60) * settings.getDiveSAC();
                }
                gas.setConsumption(consumption);
                break;
            }
        }
    }

    public List<NewDivePoint> getDivePoints() {
        return divePoints;
    }

    public void setDivePoints(List<NewDivePoint> divePoints) {
        this.divePoints = divePoints;
    }

    public List<DecoStop> getDecoStops() {
        return decoStops;
    }

    public double getGlobalTimer() {
        return globalTimer;
    }



    public void setGlobalTimer(double globalTimer) {
        this.globalTimer = globalTimer;
    }

    public O2Toxicity getO2Toxicity() {
        return o2Toxicity;
    }

    public List<Gas> getGasList() {
        return gasList;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedList(n2Tissues);
        dest.writeTypedList(heTissues);
        dest.writeTypedList(gasList);
        dest.writeTypedList(divePoints);
        dest.writeTypedList(decoStops);

        dest.writeParcelable(settings, flags);
        dest.writeParcelable(o2Toxicity, flags);

        dest.writeDouble(globalTimer);
        dest.writeDouble(currentDepth);
        dest.writeDouble(meterToFeet);
        dest.writeInt(pressureDivider);

        dest.writeParcelable(bestGas, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
