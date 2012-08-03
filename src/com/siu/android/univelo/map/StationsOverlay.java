package com.siu.android.univelo.map;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.siu.android.bicyclette.Station;
import com.siu.android.univelo.Application;
import com.siu.android.univelo.R;
import com.siu.android.univelo.activity.MapActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationsOverlay extends ItemizedOverlay<StationOverlayItem> {

    private static final int ALPHA_OPAQUE = 255;
    private static final int ALPHA_TRANSPARENT = 70;

    private MapActivity activity;
    private List<StationOverlayItem> overlayItems = new ArrayList<StationOverlayItem>();

//    private int markerLabelSize;
//    private int markerHeight;

    public StationsOverlay(MapActivity activity) {
        super(null);

        // bug fix, see this : http://stackoverflow.com/questions/3755921/problem-with-crash-with-itemizedoverlay
        populate();

//        markerHeight = ((BitmapDrawable) Application.getContext().getResources().getDrawable(R.drawable.pin)).getBitmap().getHeight();
//        markerLabelSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, Application.getContext().getResources().getDisplayMetrics());

        this.activity = activity;
    }

    @Override
    protected StationOverlayItem createItem(int i) {
        return overlayItems.get(i);
    }

    @Override
    public int size() {
        return overlayItems.size();
    }

    public void addOverlayItem(StationOverlayItem overlayItem) {
        overlayItems.add(overlayItem);
        populate();
    }

    public void addStations(List<Station> stations) {
        Resources resources = Application.getContext().getResources();

        for (Station station : stations) {
            String id = "pin_" + activity.getInfoTypeStatus(station);

            Drawable drawable;
            try {
                drawable = resources.getDrawable(resources.getIdentifier(id, "drawable", activity.getPackageName()));
            } catch (Resources.NotFoundException e) {
                Log.e(getClass().getName(), "Marker pin not found with id : " + id);
                drawable = resources.getDrawable(R.drawable.pin);
            }

            overlayItems.add(new StationOverlayItem(station, drawable));
        }

        populate();
    }

    public void updateMarkers() {
        Resources resources = Application.getContext().getResources();

        for (StationOverlayItem item : overlayItems) {
            Drawable drawable = resources.getDrawable(resources.getIdentifier("pin_" + activity.getInfoTypeStatus(item.getStation()), "drawable", activity.getPackageName()));
            item.setDrawable(drawable);
        }
    }

    public void clearOverlayItems() {
        overlayItems.clear();
        populate();
    }

    @Override
    public boolean onTap(GeoPoint geoPoint, MapView mapView) {
        try {
            if (super.onTap(geoPoint, mapView)) {
                return true;
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Another strange error", e);
        }

        activity.hideCurrentStationIfShown();
        setNoStationSelectedAlpha();

        return super.onTap(geoPoint, mapView);
    }

    @Override
    protected boolean onTap(int i) {
        Station station = getItem(i).getStation();
        activity.showCurrentStation(station);
        setStationSelectedAlpha(station);

        return true;
    }

    public void setStationSelectedAlpha(Station station) {
        for (StationOverlayItem item : overlayItems) {
            if (item.getStation().getId().equals(station.getId())) {
                item.getDrawable().mutate().setAlpha(ALPHA_OPAQUE);
            } else {
                item.getDrawable().mutate().setAlpha(ALPHA_TRANSPARENT);
            }
        }
    }

    public void setStationSelectedAlphaAndFocus(Station station) {
        for (StationOverlayItem item : overlayItems) {
            if (item.getStation().getId().equals(station.getId())) {
                item.getDrawable().mutate().setAlpha(ALPHA_OPAQUE);

                // ugly fix exception are thrown sometimes but screw it
                try {
                    setFocus(item);
                } catch (Exception e) {
                    Log.e(getClass().getName(), "Error focus selected item, ignore", e);
                }
            } else {
                item.getDrawable().mutate().setAlpha(ALPHA_TRANSPARENT);
            }
        }
    }

    public void setNoStationSelectedAlpha() {
        for (StationOverlayItem item : overlayItems) {
            item.getDrawable().mutate().setAlpha(ALPHA_OPAQUE);
        }
    }

//    @Override
//    public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
//        super.draw(canvas, mapView, false);
//
//
//        // go through all OverlayItems and draw title for each of them
//        for (StationOverlayItem item : overlayItems) {
//
//            String label = String.valueOf(activity.getInfoTypeStatus(item.getStation()));
//
//            /* Converts latitude & longitude of this overlay item to coordinates on screen.
//             * As we have called boundCenterBottom() in constructor, so these coordinates
//             * will be of the bottom center position of the displayed marker.
//             */
//            GeoPoint point = item.getPoint();
//            Point markerBottomCenterCoords = new Point();
//            mapView.getProjection().toPixels(point, markerBottomCenterCoords);
//
//
//            /* Find the width and height of the title*/
//            TextPaint paintText = new TextPaint();
//
//            Rect rect = new Rect();
//            paintText.setTextSize(markerLabelSize);
//            paintText.getTextBounds(label, 0, label.length(), rect);
//
//            int variation = markerHeight / 6 * 5;
//            rect.offsetTo(markerBottomCenterCoords.x - rect.width() / 2, markerBottomCenterCoords.y - variation);
//
//            paintText.setTextAlign(Paint.Align.CENTER);
//            paintText.setAntiAlias(true);
//            paintText.setTypeface(Typeface.DEFAULT_BOLD);
//            paintText.setARGB(255, 255, 255, 255);
//
//            canvas.drawText(label, rect.left + rect.width() / 2, rect.bottom, paintText);
//        }
//    }


    public List<StationOverlayItem> getOverlayItems() {
        return overlayItems;
    }
}
