package com.yarolegovich.ascipaintdc.draw.tool;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.yarolegovich.ascipaintdc.draw.ASCIICanvas;

/**
 * Created by yarolegovich on 01-May-17.
 */

public class PencilTool extends Tool {

    private int prevRowStart;
    private int prevColumnStart;

    private StringBuilder batch;

    private int extraLeftOffset;

    public PencilTool(@NonNull ASCIICanvas canvas) {
        super(canvas);
        batch = new StringBuilder();
    }

    @Override
    public void onDrawStart(float x, float y) {
        if (canvas.isOnField(x, y)) {
            canvas.drawToBuffer(symbol, canvas.toRow(y), canvas.toColumn(x), color);
        }
    }

    @Override
    public void onDrawProgress(float x, float y, float pressure) {
        if (canvas.isOnField(x, y)) {
            int leftOffset = (size - 1) / 2;
            int rightOffset = leftOffset + extraLeftOffset;

            int row = canvas.toRow(y);
            int column = canvas.toColumn(x);
            drawRectToBuffer(Math.max(row - leftOffset, 0),
                    Math.max(column - leftOffset, 0),
                    Math.min(row + rightOffset, canvas.getRows() - 1),
                    Math.min(column + rightOffset, canvas.getColumns() - 1));

            canvas.drawToScreen();
        }
    }

    private void drawRectToBuffer(int rowStart, int columnStart, int rowEnd, int columnEnd) {
        if (rowStart == prevRowStart && columnStart == prevColumnStart) {
            return;
        }
        prevRowStart = rowStart;
        prevColumnStart = columnStart;

        int lineSize = (columnEnd - columnStart) + 1;
        if (lineSize != batch.length()) {
            initBatch(lineSize);
        }
        for (int row = rowStart; row <= rowEnd; row++) {
            canvas.drawHorizontalToBuffer(batch, row, columnStart, color);
        }
    }

    @Override
    public void onDrawEnd() {

    }

    @Override
    public void setSize(@IntRange(from = 1, to = 5) int size) {
        super.setSize(size);
        initBatch(size);
        extraLeftOffset = size % 2 == 0 ? 1 : 0;
        prevRowStart = prevColumnStart = -1;
    }

    @Override
    public void setSymbol(char symbol) {
        super.setSymbol(symbol);
        initBatch(size);
    }

    private void initBatch(int size) {
        batch.delete(0, batch.length());
        for (int i = 0; i < size; i++) {
            batch.append(symbol);
        }
    }
}
