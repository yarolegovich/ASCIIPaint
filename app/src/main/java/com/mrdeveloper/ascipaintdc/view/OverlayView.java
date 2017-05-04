package com.mrdeveloper.ascipaintdc.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yarolegovich on 04-May-17.
 */

public class OverlayView extends View {

    private static final int DEFAULT_ANIM_DURATION = 200;
    private static final int DEFAULT_OVERLAY_COLOR = Color.parseColor("#64000000");

    private int animDuration;
    private int overlayColor;
    private Activity activity;

    private int statusBarColor;

    public OverlayView(Context context) {
        super(context);
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    {
        activity = (Activity) getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            statusBarColor = activity.getWindow().getStatusBarColor();
        }

        setClickable(true);
        animDuration = DEFAULT_ANIM_DURATION;
        overlayColor = DEFAULT_OVERLAY_COLOR;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(overlayColor);
    }

    public void hide() {
        animate().cancel();
        setAlpha(1f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(statusBarColor);
        }
        animate().alpha(0)
                .setDuration(animDuration)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        setVisibility(View.GONE);
                    }
                })
                .start();
    }

    public void show() {
        setVisibility(View.VISIBLE);
        animate().cancel();
        setAlpha(0f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(Color.BLACK);
        }
        animate().alpha(1f)
                .setDuration(animDuration)
                .start();
    }

    public void setOverlayColor(int overlayColor) {
        this.overlayColor = overlayColor;
    }

    public void setAnimationDuration(int animDuration) {
        this.animDuration = animDuration;
    }
}
