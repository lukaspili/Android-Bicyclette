package com.siu.bicyclette.app.toast;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.siu.bicyclette.R;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class AppToast extends Toast {

    public AppToast(Context context, int message) {
        this(context, context.getString(message));
    }

    public AppToast(Context context, String message) {
        super(context);

        View layout = LayoutInflater.from(context).inflate(R.layout.app_toast_layout, null);
        ((TextView) layout.findViewById(android.R.id.text1)).setText(message);

        setGravity(Gravity.CENTER, 0, 0);
        setDuration(Toast.LENGTH_LONG);

        setView(layout);
    }
}
