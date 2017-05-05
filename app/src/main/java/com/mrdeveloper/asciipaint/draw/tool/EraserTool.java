package com.mrdeveloper.asciipaint.draw.tool;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.mrdeveloper.asciipaint.draw.ASCIICanvas;

/**
 * Created by MrDeveloper on 01-May-17.
 */

public class EraserTool extends PencilTool {

    public EraserTool(@NonNull ASCIICanvas canvas) {
        super(canvas);
        super.setColor(ASCIICanvas.NO_COLOR);
        super.setSymbol(canvas.getEmptyChar());
    }

    @Override
    public void setSymbol(char symbol) {
    }

    @Override
    public void setColor(@ColorInt int color) {
    }
}
