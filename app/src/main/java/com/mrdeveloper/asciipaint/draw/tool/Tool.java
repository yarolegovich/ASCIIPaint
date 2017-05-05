package com.mrdeveloper.asciipaint.draw.tool;

import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.mrdeveloper.asciipaint.draw.ASCIICanvas;

/**
 * Created by MrDeveloper on 01-May-17.
 */

public abstract class Tool {

    public static final int TOOL_MAX_SIZE = 5;
    public static final int TOOL_MIN_SIZE = 1;

    protected ASCIICanvas canvas;

    protected int size;
    protected int color;
    protected char symbol;

    public Tool(@NonNull ASCIICanvas canvas) {
        this.canvas = canvas;
        this.size = TOOL_MIN_SIZE;
    }

    public abstract void onDrawStart(float x, float y);

    public abstract void onDrawProgress(float x, float y, float pressure);

    public abstract void onDrawEnd();

    public void setSize(@IntRange(from = TOOL_MIN_SIZE, to = TOOL_MAX_SIZE) int size) {
        this.size = size;
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

}
