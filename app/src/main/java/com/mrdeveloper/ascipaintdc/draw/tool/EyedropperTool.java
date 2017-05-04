package com.mrdeveloper.ascipaintdc.draw.tool;

import android.support.annotation.NonNull;

import com.mrdeveloper.ascipaintdc.draw.ASCIICanvas;

/**
 * Created by yarolegovich on 01-May-17.
 */

public class EyedropperTool extends Tool {

    private Listener listener;

    public EyedropperTool(@NonNull ASCIICanvas canvas) {
        super(canvas);
    }

    @Override
    public void onDrawStart(float x, float y) {
        pickColorAt(x, y);
    }

    @Override
    public void onDrawProgress(float x, float y, float pressure) {
        pickColorAt(x, y);
    }

    @Override
    public void onDrawEnd() {

    }

    private void pickColorAt(float x, float y) {
        if (listener != null) {
            if (canvas.isOnField(x, y)) {
                int index = canvas.toIndex(canvas.toRow(y), canvas.toColumn(x));
                int color = canvas.getColor(index);
                if (color != ASCIICanvas.NO_COLOR) {
                    listener.onColorPicked(color);
                }
            }
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onColorPicked(int color);
    }
}
