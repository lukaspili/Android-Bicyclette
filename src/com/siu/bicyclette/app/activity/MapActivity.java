package com.siu.bicyclette.app.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.maps.MapView;
import com.siu.bicyclette.R;
import com.siu.bicyclette.Station;
import com.siu.bicyclette.app.map.RoundedMapView;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class MapActivity extends com.google.android.maps.MapActivity {

    private MapView mapView;
    private ImageView jaugeBackgroundRepeat;
    private ImageView jaugeRepeat;
    private TextView jaugeLeftText;
    private TextView jaugeRightText;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.map_activity);

        mapView = ((RoundedMapView) findViewById(R.id.map)).getMapView();
        jaugeBackgroundRepeat = (ImageView) findViewById(R.id.map_top_jauge_background_repeat);
        jaugeRepeat = (ImageView) findViewById(R.id.map_top_jauge_repeat);
        jaugeLeftText = (TextView) findViewById(R.id.map_top_jauge_left_text);
        jaugeRightText = (TextView) findViewById(R.id.map_top_jauge_right_text);

        initMap();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        initStation();
    }

    private void initMap() {
        mapView.setClickable(true);
        mapView.getController().setZoom(14);
    }

    private void initStation() {

        Station station = new Station();
        station.setTotal(20);
        station.setAvailable(5);
        station.setFree(15);

        jaugeLeftText.setText(String.valueOf(station.getAvailable()));
        jaugeRightText.setText(String.valueOf(station.getFree()));
        jaugeRepeat.getLayoutParams().width = (jaugeBackgroundRepeat.getWidth() / station.getTotal()) * station.getAvailable();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
