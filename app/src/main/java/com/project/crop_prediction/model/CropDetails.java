package com.project.crop_prediction.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class CropDetails implements Parcelable {

    public String typ;
    public String techniquesUsed;
    public String varieties;
    public String temp;
    public String rainfall;
    public String soil;
    public String[] majorProducers;
    public String highestProducer;

    public CropDetails() {
        this.typ = null;
        this.techniquesUsed = null;
        this.varieties = null;
        this.temp = null;
        this.rainfall = null;
        this.soil = null;
        this.majorProducers = null;
        this.highestProducer = null;
    }

    public CropDetails(String typ, String techniquesUsed, String varieties, String temp, String rainfall, String soil, String[] majorProducers) {
        this.typ = typ;
        this.techniquesUsed = techniquesUsed;
        this.varieties = varieties;
        this.temp = temp;
        this.rainfall = rainfall;
        this.soil = soil;
        this.majorProducers = majorProducers;
        this.highestProducer = majorProducers[0];
    }

    protected CropDetails(Parcel in) {
        typ = in.readString();
        techniquesUsed = in.readString();
        varieties = in.readString();
        temp = in.readString();
        rainfall = in.readString();
        soil = in.readString();
        majorProducers = in.createStringArray();
        highestProducer = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(typ);
        dest.writeString(techniquesUsed);
        dest.writeString(varieties);
        dest.writeString(temp);
        dest.writeString(rainfall);
        dest.writeString(soil);
        dest.writeStringArray(majorProducers);
        dest.writeString(highestProducer);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CropDetails> CREATOR = new Creator<CropDetails>() {
        @Override
        public CropDetails createFromParcel(Parcel in) {
            return new CropDetails(in);
        }

        @Override
        public CropDetails[] newArray(int size) {
            return new CropDetails[size];
        }
    };

    public ArrayList<InfoCell> getInfoList() {
        ArrayList<InfoCell> infos = new ArrayList<>();

        infos.add(new InfoCell((typ == null) ? "N/A" : typ, "Type"));
        infos.add(new InfoCell((techniquesUsed == null) ? "N/A" : techniquesUsed, "Techniques Used"));
        infos.add(new InfoCell((varieties == null) ? "N/A" : varieties, "Varieties"));
        infos.add(new InfoCell((temp == null) ? "N/A" : temp, "Temp"));
        infos.add(new InfoCell((rainfall == null) ? "N/A" : rainfall, "Rainfall"));
        infos.add(new InfoCell((soil == null) ? "N/A" : soil, "Soil"));
        infos.add(new InfoCell((highestProducer == null) ? "N/A" : highestProducer, "Highest Producer"));


        String str = "";
        if(majorProducers != null)
            for (int i = 0; i < majorProducers.length; i ++) {
                str += majorProducers[i] + ((i != majorProducers.length - 1) ? ",\n" : "");
            }
        else
            str = "N/A";

        infos.add(new InfoCell(str, "Major Producers"));

        return infos;
    }

}
