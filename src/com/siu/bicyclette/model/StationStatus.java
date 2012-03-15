package com.siu.bicyclette.model;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationStatus {

    private long id;
    private long places;
    private long bikes;

    public StationStatus(long id, long places, long bikes) {
        this.id = id;
        this.places = places;
        this.bikes = bikes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPlaces() {
        return places;
    }

    public void setPlaces(long places) {
        this.places = places;
    }

    public long getBikes() {
        return bikes;
    }

    public void setBikes(long bikes) {
        this.bikes = bikes;
    }
}
