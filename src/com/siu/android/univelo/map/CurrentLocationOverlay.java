package com.siu.android.univelo.map;

import android.location.Location;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.siu.android.andgapisutils.util.LocationUtils;
import com.siu.android.univelo.Application;
import com.siu.android.univelo.R;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class CurrentLocationOverlay extends ItemizedOverlay<OverlayItem> {

    private OverlayItem item;

    public CurrentLocationOverlay() {
        super(boundCenter(Application.getContext().getResources().getDrawable(R.drawable.current_position)));
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return item;
    }

    @Override
    public int size() {
        return (null == item) ? 0 : 1;
    }

    public void setCurrentLocation(Location location) {
        item = new OverlayItem(LocationUtils.getGeoPoint(location), null, null);
        populate();
    }
}
