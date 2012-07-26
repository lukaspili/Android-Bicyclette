package com.siu.bicyclette.map;

import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextPaint;
import android.util.TypedValue;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.siu.android.bicyclette.Station;
import com.siu.bicyclette.Application;
import com.siu.bicyclette.R;
import com.siu.bicyclette.activity.MapActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationsOverlay extends ItemizedOverlay<StationOverlayItem> {

    private static final int ALPHA_SELECTED = 255;
    private static final int ALPHA_UNSELECTED = 100;

    private MapActivity activity;
    private List<StationOverlayItem> overlayItems = new ArrayList<StationOverlayItem>();

    private int markerLabelSize;
    private int markerHeight;

    public StationsOverlay(MapActivity activity) {
        super(null);

        // bug fix, see this : http://stackoverflow.com/questions/3755921/problem-with-crash-with-itemizedoverlay
        populate();

        markerHeight = ((BitmapDrawable) Application.getContext().getResources().getDrawable(R.drawable.pin)).getBitmap().getHeight();
        markerLabelSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, Application.getContext().getResources().getDisplayMetrics());

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
        for (Station station : stations) {
            overlayItems.add(new StationOverlayItem(station));
        }

        populate();
    }

    public void clearOverlayItems() {
        overlayItems.clear();
        populate();
    }

    @Override
    protected boolean onTap(int i) {
        StationOverlayItem selectedItem = getItem(i);
        activity.showCurrentStation(selectedItem.getStation());

        for (StationOverlayItem item : overlayItems) {
            if (item.equals(selectedItem)) {
                item.getDrawable().setAlpha(ALPHA_SELECTED);
            } else {
                item.getDrawable().setAlpha(ALPHA_UNSELECTED);
            }
        }

        return true;
    }

    @Override
    public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, false);


        // go through all OverlayItems and draw title for each of them
        for (StationOverlayItem item : overlayItems) {

            String label = String.valueOf(activity.getInfoTypeStatus(item.getStation()));

            /* Converts latitude & longitude of this overlay item to coordinates on screen.
             * As we have called boundCenterBottom() in constructor, so these coordinates
             * will be of the bottom center position of the displayed marker.
             */
            GeoPoint point = item.getPoint();
            Point markerBottomCenterCoords = new Point();
            mapView.getProjection().toPixels(point, markerBottomCenterCoords);


            /* Find the width and height of the title*/
            TextPaint paintText = new TextPaint();

            Rect rect = new Rect();
            paintText.setTextSize(markerLabelSize);
            paintText.getTextBounds(label, 0, label.length(), rect);

            int variation = markerHeight / 6 * 5;
            rect.offsetTo(markerBottomCenterCoords.x - rect.width() / 2, markerBottomCenterCoords.y - variation);

            paintText.setTextAlign(Paint.Align.CENTER);
            paintText.setAntiAlias(true);
            paintText.setTypeface(Typeface.DEFAULT_BOLD);
            paintText.setARGB(255, 255, 255, 255);

            canvas.drawText(label, rect.left + rect.width() / 2, rect.bottom, paintText);
        }
    }


    public List<StationOverlayItem> getOverlayItems() {
        return overlayItems;
    }
}
