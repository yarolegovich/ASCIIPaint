package com.yarolegovich.ascipaintdc.draw.tool;

import android.support.annotation.NonNull;

import com.yarolegovich.ascipaintdc.draw.ASCIICanvas;
import com.yarolegovich.ascipaintdc.draw.PresetImage;

/**
 * Created by yarolegovich on 02-May-17.
 */

public class PresetImageTool extends Tool {

    private PresetImage image;

    private int previousRow;
    private int previousCol;

    private int leftOffset, rightOffset;
    private int topOffset, bottomOffset;

    public PresetImageTool(@NonNull ASCIICanvas canvas, PresetImage image) {
        super(canvas);
        this.image = image;
        topOffset = (image.getRowCount() - 1) / 2;
        bottomOffset = topOffset + (image.getRowCount() % 2 == 0 ? 1 : 0);
        leftOffset = (image.getRawRow(0).length - 1) / 2;
        rightOffset = leftOffset + (image.getRawRow(0).length % 2 == 0 ? 1 : 0);
    }

    @Override
    public void onDrawStart(float x, float y) {
        canvas.clearTempBufferChanges();
        putImageAt(x, y);
    }

    @Override
    public void onDrawProgress(float x, float y, float pressure) {
        canvas.clearTempBufferChanges();
        putImageAt(x, y);
    }

    private void putImageAt(float x, float y) {
        int row = canvas.toRow(y);
        int col = canvas.toColumn(x);
        if (row == previousRow && col == previousCol) {
            return;
        }
        drawToBufferAt(row, col);
        canvas.drawToScreen();
        previousRow = row;
        previousCol = col;
    }

    private void drawToBufferAt(int row, int col) {
        int columnStart = col - leftOffset;
        int columnEnd = Math.min(col + rightOffset, canvas.getColumns() - 1);
        int offset = columnStart >= 0 ? 0 : Math.abs(columnStart);
        columnStart = Math.max(0, columnStart);
        int length = columnEnd - columnStart + 1;

        int rowStart = row - topOffset;
        int rowEnd = Math.min(row + bottomOffset, canvas.getRows() - 1);
        int index = rowStart >= 0 ? 0 : Math.abs(rowStart);
        rowStart = Math.max(0, rowStart);

        for (int rowToDraw = rowStart; rowToDraw <= rowEnd; rowToDraw++) {
            CharSequence seq = image.getRow(index++, offset, length);
            canvas.drawHorizontalToBuffer(seq, rowToDraw, columnStart, color);
        }
    }

    @Override
    public void onDrawEnd() {

    }

    public void drawAtLastLocation() {
        drawToBufferAt(previousRow, previousCol);
    }
}
