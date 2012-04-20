package com.siu.bicyclette.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationStatus implements Parcelable {

    public static final Parcelable.Creator<StationStatus> CREATOR = new Parcelable.Creator<StationStatus>() {
        @Override
        public StationStatus createFromParcel(Parcel parcel) {
            return new StationStatus(parcel);
        }

        @Override
        public StationStatus[] newArray(int i) {
            return new StationStatus[i];
        }
    };

    private int id;
    private int places;
    private int bikes;

    public StationStatus() {
    }

    public StationStatus(Parcel parcel) {
        id = parcel.readInt();
        places = parcel.readInt();
        bikes = parcel.readInt();
    }

    public StationStatus(int id, int places, int bikes) {
        this.id = id;
        this.places = places;
        this.bikes = bikes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(places);
        parcel.writeInt(bikes);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlaces() {
        return places;
    }

    public void setPlaces(int places) {
        this.places = places;
    }

    public int getBikes() {
        return bikes;
    }

    public void setBikes(int bikes) {
        this.bikes = bikes;
    }
}
