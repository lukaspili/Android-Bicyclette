package com.siu.android.univelo.adapter;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import com.siu.android.andutils.adapter.SimpleAdapter;
import com.siu.android.bicyclette.Station;
import com.siu.android.univelo.activity.FavoritesDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationAdapter extends SimpleAdapter<Station, StationViewHolder> {

    private FavoritesDialog dialog;
//    private List<Long> ids = new ArrayList<Long>();

    public StationAdapter(FavoritesDialog dialog, int rowLayoutId, List<Station> stations) {
        super(dialog.getContext(), rowLayoutId, stations);
        this.dialog = dialog;
    }

    @Override
    protected void configure(final StationViewHolder viewHolder, final Station station) {
//        final int width = viewHolder.delete.getWidth();
//        Log.d(getClass().getName(), "Width = " + width);

//        if (ids.contains(station.getId())) {
//            viewHolder.delete.setVisibility(View.INVISIBLE);
//            ((RelativeLayout.LayoutParams) viewHolder.bar.getLayoutParams()).rightMargin = 0;
//        } else {
//            viewHolder.delete.setVisibility(View.VISIBLE);
//            ((RelativeLayout.LayoutParams) viewHolder.bar.getLayoutParams()).rightMargin = width;
//        }

        viewHolder.getRow().setFocusable(true);
        viewHolder.getRow().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.getActivity().showStationFromDialog(station);
                dialog.dismiss();

//                viewHolder.jaugeBackground.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                    @Override
//                    public void onGlobalLayout() {
//                        viewHolder.jaugeBackground.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                        updateJauge(viewHolder, station);
//                    }
//                });
//
//                if (ids.contains(station.getId())) {
//                    ids.remove(station.getId());
//                    ((RelativeLayout.LayoutParams) viewHolder.bar.getLayoutParams()).rightMargin = 0;
//                } else {
//                    ids.add(station.getId());
//                    ((RelativeLayout.LayoutParams) viewHolder.bar.getLayoutParams()).rightMargin = width;
//                }
//
//                dialog.refreshFavs(station);
            }
        });

        viewHolder.delete.setVisibility(View.INVISIBLE);
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.getActivity().getFavoritesStations().remove(station.getId());
                viewHolder.getRow().setVisibility(View.GONE);
            }
        });

        viewHolder.getRow().setOnTouchListener(new View.OnTouchListener() {
            private boolean moved;
            private boolean animating;
            private int movement = 0;
            private int initialx = 0;
            private int currentx = 0;

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    movement = 0;
                    initialx = (int) event.getX();
                    currentx = (int) event.getX();
                }

                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    currentx = (int) event.getX();
                    movement = currentx - initialx;

                    // reccord the movement action to test if next is movement stop and not a click
                    moved = true;
                }

                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    movement = 0;
                    initialx = 0;
                    currentx = 0;
                }

                if (!animating) {
                    if (movement < 0 && viewHolder.delete.getVisibility() == View.INVISIBLE) {
                        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 255);
                        alphaAnimation.setDuration(1000);
                        alphaAnimation.setFillAfter(true);
                        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                animating = true;
                                viewHolder.delete.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                animating = false;
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });

                        viewHolder.delete.startAnimation(alphaAnimation);

                    } else if (movement > 0 && viewHolder.delete.getVisibility() == View.VISIBLE) {
                        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
                        alphaAnimation.setDuration(1000);
                        alphaAnimation.setFillAfter(true);
                        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                animating = true;
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                viewHolder.delete.setVisibility(View.INVISIBLE);
                                animating = false;
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });

                        viewHolder.delete.startAnimation(alphaAnimation);
                    }
                }

                Log.d(getClass().getName(), "Movement = " + movement);

                if (moved && movement == 0) {
                    moved = false;
                    return true;
                }

                return false;
            }

        });

        viewHolder.title.setText(station.getName());
        viewHolder.leftText.setText(String.valueOf(station.getAvailable()));
        viewHolder.rightText.setText(String.valueOf(station.getFree()));

        viewHolder.jaugeBackground.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewHolder.jaugeBackground.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                updateJauge(viewHolder, station);
            }
        });
    }

    @Override
    protected StationViewHolder createViewHolder() {
        return new StationViewHolder();
    }

//    private void showOrHideDelete(final StationViewHolder viewHolder, final Station station) {
//
////        Log.d(getClass().getName(), "Jauge initial = " + viewHolder.jaugeBackground.getWidth());
//
//        final int width = viewHolder.delete.getWidth();
//        Log.d(getClass().getName(), "Width = " + width);
//
//        if (ids.contains(station.getId())) {
//
//            viewHolder.delete.setVisibility(View.VISIBLE);
//            ((RelativeLayout.LayoutParams) viewHolder.right.getLayoutParams()).rightMargin = width;
//            updateJauge(viewHolder, station);
//
//
//            viewHolder.delete.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    dialog.getActivity().getFavoritesStations().remove(station.getId());
//                    remove(station);
//                    ids.remove(station.getId());
//                    notifyDataSetChanged();
//                }
//            });
//        } else {
//            viewHolder.delete.setVisibility(View.INVISIBLE);
//            ((RelativeLayout.LayoutParams) viewHolder.right.getLayoutParams()).rightMargin = 0;
//        }
//
//        viewHolder.jaugeBackground.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                viewHolder.jaugeBackground.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                updateJauge(viewHolder, station);
//            }
//        });
//
////        viewHolder.getRow().invalidate();
////
//////        Log.d(getClass().getName(), "Width = " + width);
//////        Log.d(getClass().getName(), "Jauge final = " + viewHolder.jaugeBackground.getWidth());
////
////        updateJauge(viewHolder, station);
//    }

    public void updateJauge(StationViewHolder viewHolder, Station station) {
        double width = (new Double(viewHolder.jaugeBackground.getWidth()) / new Double(station.getAvailable() + station.getFree())) * station.getAvailable();
        viewHolder.jauge.getLayoutParams().width = (int) width;
    }
}
