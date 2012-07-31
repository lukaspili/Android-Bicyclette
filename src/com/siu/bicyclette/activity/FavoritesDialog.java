package com.siu.bicyclette.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.siu.android.bicyclette.Station;
import com.siu.bicyclette.R;
import com.siu.bicyclette.adapter.StationAdapter;
import com.siu.bicyclette.task.GetFavoritesTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class FavoritesDialog extends Dialog {

    private ListView list;
    private TextView message;
    private StationAdapter stationAdapter;
    private List<Station> stations = new ArrayList<Station>();
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

        list = (ListView) findViewById(R.id.list);
        list.setItemsCanFocus(true);
        message = (TextView) findViewById(R.id.favorites_dialog_message);

        stationAdapter = new StationAdapter(this, R.layout.station_row, stations);
        list.setAdapter(stationAdapter);
    }

    public void start(Set<Long> ids) {
        message.setText(getContext().getString(R.string.favorites_dialog_loading));
        message.setVisibility(View.VISIBLE);
        list.setVisibility(View.GONE);

        new GetFavoritesTask(this, ids).execute();
    }

    public void onGetFavoritesTaskFinished(List<Station> receivedStations) {
        stations.clear();

        if (null == receivedStations || receivedStations.isEmpty()) {
            message.setText(getContext().getString(R.string.favorites_dialog_empty));
        } else {
            stations.addAll(receivedStations);
            list.setVisibility(View.VISIBLE);
            message.setVisibility(View.GONE);
        }

        stationAdapter.notifyDataSetChanged();
    }

    public MapActivity getActivity() {
        return activity;
    }
}
