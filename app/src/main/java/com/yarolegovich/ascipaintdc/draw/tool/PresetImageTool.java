package com.yarolegovich.ascipaintdc.draw.tool;

import android.support.annotation.NonNull;
import android.util.Log;

import com.yarolegovich.ascipaintdc.draw.ASCIICanvas;
import com.yarolegovich.ascipaintdc.draw.ASCIIImage;
import com.yarolegovich.ascipaintdc.util.Logger;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by yarolegovich on 02-May-17.
 */

public class PresetImageTool extends Tool {

    private ASCIIImage image;

    private int previousRow;
    private int previousCol;

    private int leftOffset, rightOffset;
    private int topOffset, bottomOffset;

    public PresetImageTool(@NonNull ASCIICanvas canvas, ASCIIImage image) {
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
        int canvasColumnStart = col - leftOffset;
        int canvasColumnEnd = Math.min(col + rightOffset, canvas.getColumns() - 1);
        int imageColumnOffset = canvasColumnStart >= 0 ? 0 : Math.abs(canvasColumnStart);
        canvasColumnStart = Math.max(0, canvasColumnStart);
        int columnsToDraw = canvasColumnEnd - canvasColumnStart + 1;

        int canvasRowStart = row - topOffset;
        int canvasRowEnd = Math.min(row + bottomOffset, canvas.getRows() - 1);
        int imageRowStart = canvasRowStart >= 0 ? 0 : Math.abs(canvasRowStart);
        canvasRowStart = Math.max(0, canvasRowStart);

        if (image.hasColors()) {
            drawColoredImage(canvasRowStart, canvasRowEnd, canvasColumnStart,
                    imageRowStart, imageColumnOffset,
                    columnsToDraw);
        } else {
            int currentImageRow = imageRowStart;
            for (int canvasRow = canvasRowStart; canvasRow <= canvasRowEnd; canvasRow++) {
                CharSequence seq = image.getRow(currentImageRow++, imageColumnOffset, columnsToDraw);
                canvas.drawHorizontalToBuffer(seq, canvasRow, canvasColumnStart, color);
            }
        }
    }

    private void drawColoredImage(
            int canvasRowStart, int canvasRowEnd, int canvasColumnStart,
            int srcRowOffset, int srcColumnOffset,
            int columnsToDraw) {
        int imageRow = srcRowOffset;
        ASCIIImage.ColorRange[][] colors = image.getColors();
        RangeComparator rangeComparator = new RangeComparator();
        for (int canvasRow = canvasRowStart; canvasRow <= canvasRowEnd; canvasRow++) {
            int rowDrawProgress = srcColumnOffset;
            ASCIIImage.ColorRange[] rowColors = colors[imageRow];
            Arrays.sort(rowColors, rangeComparator);
            for (ASCIIImage.ColorRange range : rowColors) {
                if (rowDrawProgress > range.getRangeEnd()) {
                    continue;
                }
                if (rowDrawProgress < range.getRangeStart()) {
                    rowDrawProgress = drawPartOfTheRow(
                            canvasRow, canvasColumnStart, imageRow, rowDrawProgress,
                            range.getRangeStart(), color);
                }
                rowDrawProgress = drawPartOfTheRow(
                        canvasRow, canvasColumnStart, imageRow, rowDrawProgress,
                        range.getRangeEnd(), range.getColor());

                if (rowDrawProgress >= columnsToDraw) {
                    break;
                }
            }
            if (rowDrawProgress < columnsToDraw) {
                drawPartOfTheRow(
                        canvasRow, canvasColumnStart, imageRow, rowDrawProgress,
                        columnsToDraw, color);
            }
            imageRow++;
        }
    }

    private int drawPartOfTheRow(
            int canvasRow, int canvasColumnStart, int imageRow,
            int drawProgress, int endPoint, int color) {
        int length = endPoint - drawProgress;
        CharSequence seq = image.getRow(imageRow, drawProgress, length);
        canvas.drawHorizontalToBuffer(
                seq, canvasRow, canvasColumnStart + drawProgress,
                color);
        return drawProgress + length;
    }

    @Override
    public void onDrawEnd() {

    }

    public void drawAtLastLocation() {
        drawToBufferAt(previousRow, previousCol);
    }


    private static class RangeComparator implements Comparator<ASCIIImage.ColorRange> {

        @Override
        public int compare(ASCIIImage.ColorRange o1, ASCIIImage.ColorRange o2) {
            return o1.getRangeEnd() - o2.getRangeEnd();
        }
    }

}
