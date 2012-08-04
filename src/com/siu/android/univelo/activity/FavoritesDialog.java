package com.siu.android.univelo.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import com.siu.android.bicyclette.Station;
import com.siu.android.univelo.R;
import com.siu.android.univelo.adapter.StationAdapter;
import com.siu.android.univelo.task.GetStationsByIdsTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class FavoritesDialog extends Dialog {

    private ListView favsList;
    private ListView alertsList;
    private TextView messageAlerts;
    private TextView messageFavs;
    private StationAdapter favsAdapter;
    private StationAdapter alertsAdapter;
    private List<Station> favsStations = new ArrayList<Station>();
    private List<Station> alertsStations = new ArrayList<Station>();
    private MapActivity activity;

    public FavoritesDialog(MapActivity activity) {
        super(activity);
        this.activity = activity;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites_dialog);

        favsList = (ListView) findViewById(R.id.favorites_dialog_list_fav);
        favsList.setItemsCanFocus(true);
        favsList.setAnimationCacheEnabled(false);
        favsList.setDrawingCacheEnabled(false);
        favsAdapter = new StationAdapter(this, R.layout.station_row, favsStations);
        favsList.setAdapter(favsAdapter);

        alertsList = (ListView) findViewById(R.id.favorites_dialog_list_alert);
        alertsList.setItemsCanFocus(true);
        alertsAdapter = new StationAdapter(this, R.layout.station_row, alertsStations);
        alertsList.setAdapter(alertsAdapter);

        messageFavs = (TextView) findViewById(R.id.favorites_dialog_fav_message);
        messageAlerts = (TextView) findViewById(R.id.favorites_dialog_alert_message);
    }

//    public void refreshFavs(Station station) {
//        int start = favsList.getFirstVisiblePosition();
//        for (int i = start, j = favsList.getLastVisiblePosition(); i <= j; i++) {
//            if (station == favsList.getItemAtPosition(i)) {
//                View view = favsList.getChildAt(i - start);
//                favsList.getAdapter().getView(i, view, favsList);
//                break;
//            }
//        }
//    }

    public void start(Set<Long> favs, Set<Long> alerts) {
        messageFavs.setText(getContext().getString(R.string.favorites_dialog_loading_alerts));
        messageFavs.setVisibility(View.VISIBLE);
        favsList.setVisibility(View.GONE);
        new GetStationsByIdsTask(this, favs, GetStationsByIdsTask.Type.FAVORITES).execute();

        messageAlerts.setText(getContext().getString(R.string.favorites_dialog_loading_favs));
        messageAlerts.setVisibility(View.VISIBLE);
        alertsList.setVisibility(View.GONE);
        new GetStationsByIdsTask(this, alerts, GetStationsByIdsTask.Type.ALERTS).execute();
    }

    public void onGetFavoritesTaskFinished(List<Station> receivedStations, GetStationsByIdsTask.Type type) {
        if (type == GetStationsByIdsTask.Type.FAVORITES) {
            favsStations.clear();

            if (null == receivedStations || receivedStations.isEmpty()) {
                messageFavs.setText(getContext().getString(R.string.favorites_dialog_empty_favs));
            } else {
                favsStations.addAll(receivedStations);
                favsList.setVisibility(View.VISIBLE);
                messageFavs.setVisibility(View.GONE);
            }

            favsAdapter.notifyDataSetChanged();
        } else {
            alertsStations.clear();

            if (null == receivedStations || receivedStations.isEmpty()) {
                messageAlerts.setText(getContext().getString(R.string.favorites_dialog_empty_alerts));
            } else {
                alertsStations.addAll(receivedStations);
                alertsList.setVisibility(View.VISIBLE);
                messageAlerts.setVisibility(View.GONE);
            }

            alertsAdapter.notifyDataSetChanged();
        }
    }

    public MapActivity getActivity() {
        return activity;
    }
}
