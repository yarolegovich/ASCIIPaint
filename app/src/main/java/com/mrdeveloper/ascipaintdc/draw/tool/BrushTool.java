package com.mrdeveloper.ascipaintdc.draw.tool;

import android.support.annotation.NonNull;

import com.mrdeveloper.ascipaintdc.draw.ASCIICanvas;

/**
 * Created by yarolegovich on 01-May-17.
 */

public class BrushTool extends Tool {
    public BrushTool(@NonNull ASCIICanvas canvas) {
        super(canvas);
    }

    @Override
    public void onDrawStart(float x, float y) {

    }

    @Override
    public void onDrawProgress(float x, float y, float pressure) {

    }

    @Override
    public void onDrawEnd() {

    }
}
