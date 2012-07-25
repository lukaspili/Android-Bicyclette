package com.siu.bicyclette.map;

import android.graphics.drawable.Drawable;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.siu.android.andgapisutils.util.LocationUtils;
import com.siu.android.andgapisutils.util.MarkerUtils;
import com.siu.android.bicyclette.Station;
import com.siu.bicyclette.Application;
import com.siu.bicyclette.R;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationOverlayItem extends OverlayItem {

    private Station station;
    private Drawable drawable;

    public StationOverlayItem(Station station) {
        super(LocationUtils.getGeoPoint(station.getLatitude(), station.getLongitude()), null, null);
        this.station = station;
        this.drawable = station.getMarker().mutate();
        setMarker(MarkerUtils.boundCenterBottom(drawable));
    }

    public Station getStation() {
        return station;
    }

    public Drawable getDrawable() {
        return drawable;
    }
}
