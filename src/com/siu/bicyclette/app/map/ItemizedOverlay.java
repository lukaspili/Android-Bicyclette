package com.siu.bicyclette.app.map;

import android.graphics.drawable.Drawable;
import com.google.android.maps.OverlayItem;

import java.util.ArrayList;

public class ItemizedOverlay extends com.google.android.maps.ItemizedOverlay<OverlayItem> {

    protected ArrayList<OverlayItem> overlayItems = new ArrayList<OverlayItem>();

    public ItemizedOverlay(Drawable defaultMarker) {
        super(boundCenterBottom(defaultMarker));

        // bug fix, see this : http://stackoverflow.com/questions/3755921/problem-with-crash-with-itemizedoverlay
        populate();
    }

    public void addOverlay(OverlayItem item) {
        overlayItems.add(item);
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return overlayItems.get(i);
    }

    @Override
    public int size() {
        return overlayItems.size();
    }

    public ArrayList<OverlayItem> getOverlayItems() {
        return overlayItems;
    }
}
