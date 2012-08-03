package com.siu.android.univelo.map;

import android.graphics.drawable.Drawable;
import com.google.android.maps.OverlayItem;
import com.siu.android.andgapisutils.util.LocationUtils;
import com.siu.android.andgapisutils.util.MarkerUtils;
import com.siu.android.bicyclette.Station;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationOverlayItem extends OverlayItem {

    private Station station;
    private Drawable drawable;

    public StationOverlayItem(Station station, Drawable drawable) {
        super(LocationUtils.getGeoPoint(station.getLatitude(), station.getLongitude()), null, null);
        this.station = station;
        setDrawable(drawable);
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
        setMarker(MarkerUtils.boundCenterBottom(drawable));
    }

    public Station getStation() {
        return station;
    }

    public Drawable getDrawable() {
        return drawable;
    }
}
