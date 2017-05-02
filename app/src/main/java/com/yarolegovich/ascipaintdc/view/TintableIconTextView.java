package com.yarolegovich.ascipaintdc.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.widget.TextView;

import com.yarolegovich.ascipaintdc.R;

/**
 * Created by yarolegovich on 01-May-17.
 */

public class TintableIconTextView extends TextView {

    private static final int NO_TINT = -1;

    private int tintColor;

    public TintableIconTextView(Context context) {
        super(context);
    }

    public TintableIconTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TintableIconTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TintableIconTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.TintableIconTextView);
        tintColor = ta.getColor(R.styleable.TintableIconTextView_iconTint, NO_TINT);
        if (tintColor != NO_TINT) {
            Drawable[] drawables = getCompoundDrawables();
            for (int i = 0; i < drawables.length; i++) {
                if (drawables[i] != null) {
                    drawables[i] = drawables[i].mutate();
                    DrawableCompat.setTint(drawables[i], tintColor);
                }
            }
            setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
        }
        ta.recycle();
    }

    public void setTintColor(int tintColor) {
        this.tintColor = tintColor;
    }

    public void setTopDrawable(@DrawableRes int res) {
        Drawable[] drawables = getCompoundDrawables();
        Drawable newTop = ContextCompat.getDrawable(getContext(), res);
        int h = newTop.getIntrinsicHeight();
        int w = newTop.getIntrinsicWidth();
        newTop.setBounds( 0, 0, w, h );
        if (tintColor != NO_TINT) {
            newTop = newTop.mutate();
            DrawableCompat.setTint(newTop, tintColor);
        }
        setCompoundDrawables(drawables[0], newTop, drawables[2], drawables[3]);
    }
}
