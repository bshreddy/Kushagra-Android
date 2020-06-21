package com.project.crop_prediction.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.project.crop_prediction.R;

import java.util.ArrayList;

public class CropDetails implements Parcelable {

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

    public ArrayList<InfoCell> getInfoList(Context context) {
        ArrayList<InfoCell> infos = new ArrayList<>();

        infos.add(new InfoCell((typ == null) ? context.getString(R.string.not_available) : typ,
                context.getString(R.string.info_subtitle_type)));
        infos.add(new InfoCell((techniquesUsed == null) ? context.getString(R.string.not_available) : techniquesUsed,
                context.getString(R.string.info_subtitle_technique_used)));
        infos.add(new InfoCell((varieties == null) ? context.getString(R.string.not_available) : varieties,
                context.getString(R.string.info_subtitle_varieties)));
        infos.add(new InfoCell((temp == null) ? context.getString(R.string.not_available) : temp,
                context.getString(R.string.info_subtitle_temp)));
        infos.add(new InfoCell((rainfall == null) ? context.getString(R.string.not_available) : rainfall,
                context.getString(R.string.info_subtitle_rainfall)));
        infos.add(new InfoCell((soil == null) ? context.getString(R.string.not_available) : soil,
                context.getString(R.string.info_subtitle_soil)));
        infos.add(new InfoCell((highestProducer == null) ? context.getString(R.string.not_available) : highestProducer,
                context.getString(R.string.info_subtitle_highest_producer)));


        String str = "";
        if (majorProducers != null)
            for (int i = 0; i < majorProducers.length; i++) {
                str += majorProducers[i] + ((i != majorProducers.length - 1) ? ",\n" : "");
            }
        else
            str = "N/A";

        infos.add(new InfoCell(str, context.getString(R.string.info_subtitle_major_producers)));

        return infos;
    }

}
