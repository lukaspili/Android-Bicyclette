package com.siu.bicyclette.map;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.google.android.maps.MapView;
import com.siu.android.andgapisutils.map.*;
import com.siu.android.andgapisutils.map.EnhancedMapView;
import com.siu.bicyclette.R;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class RoundedMapView extends LinearLayout {

    private EnhancedMapView mapView;
    private Bitmap windowFrame;

    /**
     * Creates a Google Map View with rounded corners
     * Constructor when created in XML
     *
     * @param context
     * @param attrs
     */
    public RoundedMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Creates a Google Map View with rounded corners
     * Contructor when created in code
     *
     * @param context
     */
    public RoundedMapView(Context context) {
        super(context);
        init();
    }

    private void init() {
        mapView = new EnhancedMapView(getContext(), getResources().getString(R.string.googlemap_key));
        mapView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        addView(mapView);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas); // Call super first (this draws the map) we then draw on top of it

        if (windowFrame == null) {
            createWindowFrame(); // Lazy creation of the window frame, this is needed as we don't know the width & height of the screen until draw time
        }

        canvas.drawBitmap(windowFrame, 0, 0, null);
    }

    protected void createWindowFrame() {
        windowFrame = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888); // Create a new image we will draw over the map
        Canvas osCanvas = new Canvas(windowFrame); // Create a   canvas to draw onto the new image

        RectF outerRectangle = new RectF(0, 0, getWidth(), getHeight());
        RectF innerRectangle = new RectF(0, 0, getWidth(), getHeight());

        float cornerRadius = getWidth() / 18f; // The angle of your corners

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG); // Anti alias allows for smooth corners
        paint.setColor(Color.BLACK); // This is the color of your activity background
        osCanvas.drawRect(outerRectangle, paint);

        paint.setColor(Color.RED); // An obvious color to help debugging
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)); // A out B http://en.wikipedia.org/wiki/File:Alpha_compositing.svg
        osCanvas.drawRoundRect(innerRectangle, cornerRadius, cornerRadius, paint);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        windowFrame = null; // If the layout changes null our frame so it can be recreated with the new width and height
    }

    public EnhancedMapView getMapView() {
        return mapView;
    }
}
