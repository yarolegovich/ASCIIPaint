package com.yarolegovich.ascipaintdc.draw.tool;

import android.support.annotation.NonNull;

import com.yarolegovich.ascipaintdc.draw.ASCIICanvas;

/**
 * Created by yarolegovich on 01-May-17.
 */

public class EraserTool extends PencilTool {

    public EraserTool(@NonNull ASCIICanvas canvas) {
        super(canvas);
        setSymbol(canvas.getEmptyChar());
    }

    @Override
    public void setSymbol(char symbol) {
        if (symbol == canvas.getEmptyChar()) {
            super.setSymbol(symbol);
        }
    }
}
