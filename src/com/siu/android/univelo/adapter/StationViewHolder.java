package com.siu.android.univelo.adapter;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.siu.android.andutils.adapter.SimpleViewHolder;
import com.siu.android.univelo.R;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationViewHolder extends SimpleViewHolder {

    TextView title;
    TextView leftText;
    TextView rightText;
    ImageView jauge;
    ImageView jaugeBackground;
    ImageButton delete;

    @Override
    public void init() {
        title = (TextView) row.findViewById(R.id.station_row_title);
        leftText = (TextView) row.findViewById(R.id.station_row_left_text);
        rightText = (TextView) row.findViewById(R.id.station_row_right_text);
        jauge = (ImageView) row.findViewById(R.id.station_row_jauge_repeat);
        jaugeBackground = (ImageView) row.findViewById(R.id.station_row_jauge_background_repeat);
        delete = (ImageButton) row.findViewById(R.id.station_row_delete);
    }

    public View getRow() {
        return row;
    }
}
