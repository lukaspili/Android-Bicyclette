package com.siu.bicyclette.app.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
    private ImageButton availableButton;
    private ImageButton freeButton;
    private ImageButton locateButton;
    private ImageButton favoritesButton;
    private ImageButton alertButton;
    private ImageButton addFavoriteButton;

    private InfoType infoType = InfoType.AVAILABLE;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.map_activity);

        mapView = ((RoundedMapView) findViewById(R.id.map)).getMapView();
        jaugeBackgroundRepeat = (ImageView) findViewById(R.id.map_top_jauge_background_repeat);
        jaugeRepeat = (ImageView) findViewById(R.id.map_top_jauge_repeat);
        jaugeLeftText = (TextView) findViewById(R.id.map_top_jauge_left_text);
        jaugeRightText = (TextView) findViewById(R.id.map_top_jauge_right_text);
        availableButton = (ImageButton) findViewById(R.id.map_bottom_available_button);
        freeButton = (ImageButton) findViewById(R.id.map_bottom_free_button);
        locateButton = (ImageButton) findViewById(R.id.map_bottom_location_button);
        favoritesButton = (ImageButton) findViewById(R.id.map_bottom_favorites_button);
        alertButton = (ImageButton) findViewById(R.id.map_top_alert);
        favoritesButton = (ImageButton) findViewById(R.id.map_top_favorite);

        initMap();
        initButtons();
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

    private void initButtons() {
        availableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (infoType == InfoType.AVAILABLE) {
                    return;
                }

                availableButton.setImageResource(R.drawable.avail_pushed);
                freeButton.setImageResource(R.drawable.free);
                infoType = InfoType.AVAILABLE;
            }
        });

        freeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (infoType == InfoType.FREE) {
                    return;
                }

                availableButton.setImageResource(R.drawable.avail);
                freeButton.setImageResource(R.drawable.free_pushed);
                infoType = InfoType.FREE;
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favoritesButton.setImageResource(R.drawable.star);
            }
        });

        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertButton.setImageResource(R.drawable.alert);
            }
        });
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

    private static enum InfoType {
        AVAILABLE, FREE
    }
}