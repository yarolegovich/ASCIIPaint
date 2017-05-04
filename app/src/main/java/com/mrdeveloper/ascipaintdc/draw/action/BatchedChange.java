package com.mrdeveloper.ascipaintdc.draw.action;

import com.mrdeveloper.ascipaintdc.draw.ASCIICanvas;
import com.mrdeveloper.ascipaintdc.draw.model.DrawBoard;

/**
 * Created by yarolegovich on 04-May-17.
 */

public class BatchedChange implements DrawAction {

    private int current;

    private int[] indices;
    private char[] symbols;
    private int[] colors;

    public BatchedChange(int batchSize) {
        indices = new int[batchSize];
        symbols = new char[batchSize];
        colors = new int[batchSize];
    }

    @Override
    public void doOn(ASCIICanvas canvas) {
        for (int i = 0; i < current; i++) {
            canvas.drawToBuffer(symbols[i], indices[i], colors[i]);
        }
        canvas.drawToScreen();
    }

    @Override
    public void addChange(ASCIICanvas canvas, int index, char symbol, int color) {
        indices[current] = index;
        symbols[current] = symbol;
        colors[current] = color;
        current++;
    }

    public boolean isBatchFull() {
        return current >= indices.length - 1;
    }

    @Override
    public void undoOn(ASCIICanvas canvas) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasChanges() {
        return current > 0;
    }

    @Override
    public ChangeIterator getChanges() {
        throw new UnsupportedOperationException();
    }
}
