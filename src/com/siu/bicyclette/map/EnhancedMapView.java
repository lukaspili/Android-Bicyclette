package com.siu.bicyclette.map;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class EnhancedMapView extends MapView {

    public interface OnChangeListener {
        public void onChange(EnhancedMapView view, GeoPoint newCenter, GeoPoint oldCenter, int newZoom, int oldZoom);
    }

    private EnhancedMapView mThis;
    private long mEventsTimeout = 250L;
    private boolean mIsTouched = false;
    private GeoPoint mLastCenterPosition;
    private int mLastZoomLevel;
    private Timer mChangeDelayTimer = new Timer();
    private EnhancedMapView.OnChangeListener mChangeListener = null;

    public EnhancedMapView(Context context, String apiKey) {
        super(context, apiKey);
        init();
    }

    public EnhancedMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EnhancedMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mThis = this;
        mLastCenterPosition = this.getMapCenter();
        mLastZoomLevel = this.getZoomLevel();
    }

    public void setOnChangeListener(EnhancedMapView.OnChangeListener l) {
        mChangeListener = l;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        mIsTouched = (ev.getAction() != MotionEvent.ACTION_UP);

        try {
            return super.onTouchEvent(ev);
        } catch (Exception e) {
            Log.wtf(getClass().getName(), "onTouchEvent exception ?!", e);
            return false;
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        // Check for change
        if (isSpanChange() || isZoomChange()) {
            // If computeScroll called before timer counts down we should drop it and
            // start counter over again
            resetMapChangeTimer();
        }
    }

    private void resetMapChangeTimer() {
        mChangeDelayTimer.cancel();
        mChangeDelayTimer = new Timer();
        mChangeDelayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mChangeListener != null) mChangeListener.onChange(mThis, getMapCenter(), mLastCenterPosition, getZoomLevel(), mLastZoomLevel);
                mLastCenterPosition = getMapCenter();
                mLastZoomLevel = getZoomLevel();
            }
        }, mEventsTimeout);
    }

    private boolean isSpanChange() {
        return !mIsTouched && !getMapCenter().equals(mLastCenterPosition);
    }

    private boolean isZoomChange() {
        return (getZoomLevel() != mLastZoomLevel);
    }
}
