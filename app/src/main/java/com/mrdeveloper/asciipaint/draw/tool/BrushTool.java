package com.mrdeveloper.asciipaint.draw.tool;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.mrdeveloper.asciipaint.draw.ASCIICanvas;

/**
 * Created by MrDeveloper on 01-May-17.
 */

public class BrushTool extends Tool {

    private static final float MAX_PRESSURE_MODIFIER = 0.15f;

    private static final int CENTER_OFFSET = 0;

    private float[][] brushPattern;

    public BrushTool(@NonNull ASCIICanvas canvas) {
        super(canvas);
        updatePattern();
    }

    @Override
    public void onDrawStart(float x, float y) {
        onDrawProgress(x, y, 0f);
    }

    @Override
    public void onDrawProgress(float x, float y, float pressure) {
        int row = canvas.toRow(y);
        int col = canvas.toColumn(x);

        int startRow = row - CENTER_OFFSET;
        int startColumn = col - CENTER_OFFSET;

        int patternRowOffset = startRow >= 0 ? 0 : Math.abs(startRow);
        int patternColumnOffset = startColumn >= 0 ? 0 : Math.abs(startColumn);

        startRow = Math.max(0, startRow);
        startColumn = Math.max(0, startColumn);

        int rowsToDraw = Math.min(
                canvas.getRows() - Math.max(0, startRow),
                brushPattern.length);
        int columnsToDraw = Math.min(
                canvas.getColumns() - Math.max(0, startColumn),
                brushPattern[0].length);

        drawToBuffer(startRow, startColumn,
                patternRowOffset, patternColumnOffset,
                rowsToDraw, columnsToDraw,
                MAX_PRESSURE_MODIFIER * pressure);

        canvas.drawToScreen();
    }

    private void drawToBuffer(
            int startCanvasRow, int startCanvasColumn,
            int patternRowOffset, int patternColumnOffset,
            int rowsToDraw, int columnsToDraw,
            float pressureModifier) {
        for (int rowProgress = 0; rowProgress < rowsToDraw; rowProgress++) {
            for (int colProgress = 0; colProgress < columnsToDraw; colProgress++) {
                float cellWeight = brushPattern
                        [patternRowOffset + rowProgress]
                        [patternColumnOffset + colProgress];
                if (cellWeight == 0) {
                    continue;
                }
                cellWeight += pressureModifier;
                if (cellWeight >= 1f || Math.random() < cellWeight) {
                    canvas.drawToBuffer(symbol, startCanvasRow + rowProgress,
                            startCanvasColumn + colProgress,
                            color);
                }
            }
        }
    }

    @Override
    public void onDrawEnd() {

    }

    @Override
    public void setSize(@IntRange(from = TOOL_MIN_SIZE, to = TOOL_MAX_SIZE) int size) {
        super.setSize(size);
        updatePattern();
    }

    private void updatePattern() {
        brushPattern = brushPatters[size - 1];
    }

    private final float[][][] brushPatters = {
            {
                    {0, 0, 0, 0, 0},
                    {0, 0, 0.2f, 0, 0},
                    {0, 0.2f, 1f, 0.2f, 0},
                    {0, 0, 0.2f, 0, 0},
                    {0, 0, 0, 0, 0}
            },
            {
                    {0, 0, 0, 0, 0},
                    {0, 0.2f, 0.8f, 0.2f, 0},
                    {0, 0.8f, 1f, 0.8f, 0},
                    {0, 0.2f, 0.8f, 0.2f, 0},
                    {0, 0, 0, 0, 0}
            },
            {
                    {0, 0, 0.2f, 0, 0},
                    {0, 0.8f, 1f, 0.8f, 0},
                    {0.2f, 1f, 1f, 1f, 0.2f},
                    {0, 0.8f, 1f, 0.8f, 0},
                    {0, 0, 0.2f, 0, 0}
            },
            {
                    {0, 0.2f, 0.8f, 0.2f, 0},
                    {0.2f, 1f, 1f, 1f, 0.2f},
                    {0.8f, 1f, 1f, 1f, 0.8f},
                    {0.2f, 1f, 1f, 1f, 0.2f},
                    {0, 0.2f, 0.8f, 0.2f, 0}
            },
            {
                    {0, 0.8f, 1f, 0.8f, 0},
                    {0.8f, 1f, 1f, 1f, 0.2f},
                    {1f, 1f, 1f, 1f, 1f},
                    {0.8f, 1f, 1f, 1f, 0.2f},
                    {0, 0.8f, 1f, 0.8f, 0}
            }
    };
}
