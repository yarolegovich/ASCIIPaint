package com.mrdeveloper.asciipaint.draw.tool;

import android.support.annotation.NonNull;

import com.mrdeveloper.asciipaint.draw.ASCIICanvas;
import com.mrdeveloper.asciipaint.draw.model.ASCIIImage;
import com.mrdeveloper.asciipaint.util.Logger;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by MrDeveloper on 02-May-17.
 */

public class PresetImageTool extends Tool {

    private ASCIIImage image;

    private int previousRow;
    private int previousCol;

    private int leftOffset, rightOffset;
    private int topOffset, bottomOffset;

    private StringBuilder buffer;

    public PresetImageTool(@NonNull ASCIICanvas canvas, ASCIIImage image) {
        super(canvas);
        this.image = image;
        this.buffer = new StringBuilder();
        this.bottomOffset = (image.getRowCount() - 1) / 2;
        this.topOffset = bottomOffset + (image.getRowCount() % 2 == 0 ? 1 : 0);
        this.rightOffset = (image.getRawRow(0).length - 1) / 2;
        this.leftOffset = rightOffset+ (image.getRawRow(0).length % 2 == 0 ? 1 : 0);
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
        drawToBufferInternal(row, col);
        canvas.drawToScreen();
        previousRow = row;
        previousCol = col;
    }

    private void drawToBufferInternal(int row, int col) {
        Logger.d("draw image at row=%d, col=%d", row, col);

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
                buffer.delete(0, buffer.length());
                image.getRow(currentImageRow++, imageColumnOffset, columnsToDraw, buffer);
                canvas.drawHorizontalToBuffer(buffer, canvasRow, canvasColumnStart, color);
            }
        }
        buffer.delete(0, buffer.length());
    }

    private void drawColoredImage(
            int canvasRowStart, int canvasRowEnd, int canvasColumnStart,
            int srcRowOffset, int srcColumnOffset,
            int columnsToDraw) {
        Logger.d("canvasRow start=%d, end=%d", canvasRowStart, canvasRowEnd);
        Logger.d("imageRowOffset=%d, columnOffset=%d", srcRowOffset, srcColumnOffset);
        Logger.d("columnsToDraw=%d", columnsToDraw);
        int imageRow = srcRowOffset;
        ASCIIImage.ColorRange[][] colors = image.getColors();
        RangeComparator rangeComparator = new RangeComparator();
        for (int canvasRow = canvasRowStart; canvasRow <= canvasRowEnd; canvasRow++) {
            int rowDrawProgress = srcColumnOffset;
            ASCIIImage.ColorRange[] rowColors = colors[imageRow];
            Arrays.sort(rowColors, rangeComparator);
            for (ASCIIImage.ColorRange range : rowColors) {
                if (rowDrawProgress > range.getRangeEnd()) {
                    //May happen if part of the image is outside the screen, so we just skip
                    //the color range
                    continue;
                }
                if (rowDrawProgress < range.getRangeStart()) {
                    //If a row segment doesn't fall into any range, I still want to draw it with
                    //NO_COLOR to erase any previous drawing on the canvas
                    rowDrawProgress = drawPartOfTheRow(
                            canvasRow, canvasColumnStart, imageRow, rowDrawProgress,
                            range.getRangeStart(), ASCIICanvas.NO_COLOR,
                            columnsToDraw);
                }
                rowDrawProgress = drawPartOfTheRow(
                        canvasRow, canvasColumnStart, imageRow, rowDrawProgress,
                        range.getRangeEnd(), range.getColor(),
                        columnsToDraw);

                if (rowDrawProgress >= columnsToDraw) {
                    break;
                }
            }
            if (rowDrawProgress < columnsToDraw) {
                //See the comment above
                drawPartOfTheRow(
                        canvasRow, canvasColumnStart, imageRow, rowDrawProgress,
                        columnsToDraw, ASCIICanvas.NO_COLOR,
                        columnsToDraw);
            }
            imageRow++;
        }
    }

    private int drawPartOfTheRow(
            int canvasRow, int canvasColumnStart, int imageRow,
            int drawProgress, int endPoint, int color,
            int maxLength) {
        int length = Math.min(endPoint, maxLength) - drawProgress;
        buffer.delete(0, buffer.length());
        image.getRow(imageRow, drawProgress, length, buffer);
        canvas.drawHorizontalToBuffer(
                buffer, canvasRow, canvasColumnStart + drawProgress,
                color);
        return drawProgress + length;
    }

    @Override
    public void onDrawEnd() {

    }

    public void drawToBufferAtLastLocation() {
        drawToBufferInternal(previousRow, previousCol);
    }


    public void drawToBufferAt(int row, int col) {
        drawToBufferInternal(row, col);
    }

    private static class RangeComparator implements Comparator<ASCIIImage.ColorRange> {

        @Override
        public int compare(ASCIIImage.ColorRange o1, ASCIIImage.ColorRange o2) {
            return o1.getRangeEnd() - o2.getRangeEnd();
        }
    }

}
