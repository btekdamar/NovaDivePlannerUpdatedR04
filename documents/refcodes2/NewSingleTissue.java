package com.example.newnovadiveplanner.Algorithm;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.newnovadiveplanner.Core.Gas;

public class NewSingleTissue implements Parcelable {
    public static final Creator<NewSingleTissue> CREATOR = new Creator<NewSingleTissue>() {
        @Override
        public NewSingleTissue createFromParcel(Parcel in) {
            return new NewSingleTissue(in);
        }

        @Override
        public NewSingleTissue[] newArray(int size) {
            return new NewSingleTissue[size];
        }
    };
    protected static final double FN2inAIR = 0.79;
    protected static final int NITROGEN = 0;
    protected static final int HELIUM = 1;
    private int type;
    private double tension;
    private double tau;
    private double surfaceTau;
    private double aValue;
    private double bValue;
    private int pAmb;

    public NewSingleTissue(int type, double tension, double tau, double surfaceTau, double aValue, double bValue, int pAmb) {
        this.type = type;
        this.tension = tension;
        this.tau = tau;
        this.surfaceTau = surfaceTau;
        this.aValue = aValue;
        this.bValue = bValue;
        this.pAmb = pAmb;
    }

    public NewSingleTissue(int type, double tau, double surfaceTau, double aValue, double bValue, int pAmb) {
        this.type = type;
        if (type == NITROGEN) {
            this.tension = pAmb * FN2inAIR;
        } else {
            this.tension = 0;
        }
        this.tau = tau;
        this.surfaceTau = surfaceTau;
        this.aValue = aValue;
        this.bValue = bValue;
        this.pAmb = pAmb;
    }

    public NewSingleTissue(NewSingleTissue newSingleTissue) {
        this.type = newSingleTissue.type;
        this.tension = newSingleTissue.tension;
        this.tau = newSingleTissue.tau;
        this.surfaceTau = newSingleTissue.surfaceTau;
        this.aValue = newSingleTissue.aValue;
        this.bValue = newSingleTissue.bValue;
        this.pAmb = newSingleTissue.pAmb;
    }

    protected NewSingleTissue(Parcel in) {
        this.type = in.readInt();
        this.tension = in.readDouble();
        this.tau = in.readDouble();
        this.surfaceTau = in.readDouble();
        this.aValue = in.readDouble();
        this.bValue = in.readDouble();
        this.pAmb = in.readInt();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getTension() {
        return tension;
    }

    public void setTension(double tension) {
        this.tension = tension;
    }

    public double getTau() {
        return tau;
    }

    public void setTau(double tau) {
        this.tau = tau;
    }

    public double getSurfaceTau() {
        return surfaceTau;
    }

    public void setSurfaceTau(double surfaceTau) {
        this.surfaceTau = surfaceTau;
    }

    public double getaValue() {
        return aValue;
    }

    public void setaValue(double aValue) {
        this.aValue = aValue;
    }

    public double getbValue() {
        return bValue;
    }

    public void setbValue(double bValue) {
        this.bValue = bValue;
    }

    public int getpAmb() {
        return pAmb;
    }

    public void setpAmb(int pAmb) {
        this.pAmb = pAmb;
    }

    protected void computeTension(Gas gas, double depth) {
        double gasFraction;
        if (this.type == NITROGEN) {
            gasFraction = gas.getFiN2();
        } else {
            gasFraction = gas.getFiHe();
        }

        this.tension = gasFraction * (depth + this.pAmb) + ((this.tension - gasFraction * (depth + this.pAmb)) * Math.exp(-1 / (60 * this.tau)));
    }

    protected void computeTensionDirect(Gas gas, double depth, long time) {
        double gasFraction;
        if (this.type == NITROGEN) {
            gasFraction = gas.getFiN2();
        } else {
            gasFraction = gas.getFiHe();
        }

        this.tension = gasFraction * (depth + this.pAmb) + ((this.tension - gasFraction * (depth + this.pAmb)) * Math.exp((-1 * time) / (60 * this.tau)));
    }

    protected void computeTensionsSurface() {
        if (this.type == NITROGEN) {
            double tau = surfaceTau;
            if (this.tension < this.pAmb * FN2inAIR) {
                tau = this.tau;
            }
            this.tension = (FN2inAIR * this.pAmb) + ((this.tension - (FN2inAIR * this.pAmb)) * Math.exp(-1 / (60 * tau)));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(this.type);
        parcel.writeDouble(this.tension);
        parcel.writeDouble(this.tau);
        parcel.writeDouble(this.surfaceTau);
        parcel.writeDouble(this.aValue);
        parcel.writeDouble(this.bValue);
        parcel.writeInt(this.pAmb);
    }
}
