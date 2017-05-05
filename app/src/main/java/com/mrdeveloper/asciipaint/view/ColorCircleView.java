package com.mrdeveloper.asciipaint.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.mrdeveloper.asciipaint.R;

/**
 * Created by MrDeveloper on 01-May-17.
 */

public class ColorCircleView extends View {

    private static final int DEFAULT_COLOR = Color.BLACK;

    private Paint paint;
    private float radius;
    private float centerX, centerY;

    private int borderColor;
    private int circleColor;

    public ColorCircleView(Context context) {
        super(context);
        init(null);
    }

    public ColorCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ColorCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ColorCircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        borderColor = ContextCompat.getColor(getContext(), R.color.primaryText);

        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ColorCircleView);
            circleColor = ta.getColor(R.styleable.ColorCircleView_color, DEFAULT_COLOR);
            ta.recycle();
        } else {
            circleColor = DEFAULT_COLOR;
        }

        float borderWidth = getResources().getDisplayMetrics().density;
        paint.setStrokeWidth(borderWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        radius = Math.min(w, h) * 0.45f;
        centerX = w * 0.5f;
        centerY = h * 0.5f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(circleColor);
        canvas.drawCircle(centerX, centerY, radius, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(borderColor);
        canvas.drawCircle(centerX, centerY, radius, paint);
    }

    public void setColor(@ColorInt int color) {
        this.circleColor = color;
        invalidate();
    }

    @ColorInt
    public int getColor() {
        return circleColor;
    }
}
