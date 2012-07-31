package com.siu.bicyclette.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import com.siu.android.andutils.adapter.SimpleAdapter;
import com.siu.android.bicyclette.Station;
import com.siu.bicyclette.R;
import com.siu.bicyclette.activity.FavoritesDialog;
import com.siu.bicyclette.activity.MapActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationAdapter extends SimpleAdapter<Station, StationViewHolder> {

    private FavoritesDialog dialog;
    private List<Long> ids = new ArrayList<Long>();

    public StationAdapter(FavoritesDialog dialog, int rowLayoutId, List<Station> stations) {
        super(dialog.getContext(), rowLayoutId, stations);
        this.dialog = dialog;
    }

    @Override
    protected void configure(final StationViewHolder viewHolder, final Station station) {
        // show or remove delete
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder.right.getLayoutParams();
        if (ids.contains(station.getId())) {
            viewHolder.delete.setVisibility(View.VISIBLE);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            params.addRule(RelativeLayout.LEFT_OF, R.id.station_row_delete);

            viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.getActivity().getFavoritesStations().remove(station.getId());
                    remove(station);
                    ids.remove(station.getId());
                    notifyDataSetChanged();
                }
            });
        } else {
            viewHolder.delete.setVisibility(View.INVISIBLE);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.LEFT_OF, 0);
        }

        viewHolder.getRow().setFocusable(true);
        viewHolder.getRow().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ids.contains(station.getId())) {
                    ids.remove(station.getId());
                } else {
                    ids.add(station.getId());
                }

                notifyDataSetChanged();
            }
        });

        viewHolder.title.setText(station.getName());
        viewHolder.leftText.setText(String.valueOf(station.getAvailable()));
        viewHolder.rightText.setText(String.valueOf(station.getFree()));

        viewHolder.jaugeBackground.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewHolder.jaugeBackground.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                double width = (new Double(viewHolder.jaugeBackground.getWidth()) / new Double(station.getAvailable() + station.getFree())) * station.getAvailable();
                viewHolder.jauge.getLayoutParams().width = (int) width;
            }
        });
    }

    @Override
    protected StationViewHolder createViewHolder() {
        return new StationViewHolder();
    }

    public List<Long> getIds() {
        return ids;
    }
}
