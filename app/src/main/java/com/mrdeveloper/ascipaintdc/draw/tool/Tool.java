package com.mrdeveloper.ascipaintdc.draw.tool;

import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.mrdeveloper.ascipaintdc.draw.ASCIICanvas;

/**
 * Created by yarolegovich on 01-May-17.
 */

public abstract class Tool {

    protected ASCIICanvas canvas;

    protected int size;
    protected int color;
    protected char symbol;

    public Tool(@NonNull ASCIICanvas canvas) {
        this.canvas = canvas;
    }

    public abstract void onDrawStart(float x, float y);

    public abstract void onDrawProgress(float x, float y, float pressure);

    public abstract void onDrawEnd();

    public void setSize(@IntRange(from = 1, to = 5) int size) {
        this.size = size;
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

}
