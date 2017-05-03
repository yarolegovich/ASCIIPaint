package com.yarolegovich.ascipaintdc.view;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.FloatRange;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.TextView;

import com.yarolegovich.ascipaintdc.R;
import com.yarolegovich.ascipaintdc.draw.ASCIICanvas;
import com.yarolegovich.ascipaintdc.draw.ASCIIImage;
import com.yarolegovich.ascipaintdc.draw.tool.PencilTool;
import com.yarolegovich.ascipaintdc.draw.tool.Tool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yarolegovich on 01-May-17.
 */

public class ASCIIPaintView extends TextView {

    private SpannableStringBuilder drawBuffer;
    private SpannableStringBuilder temporaryBuffer;
    private SpannableStringBuilder currentBuffer;

    private final float symbolWidth;
    private final float symbolHeight;
    private int columns;
    private int rows;

    private Tool currentTool;

    private ASCIICanvas canvas;

    public ASCIIPaintView(Context context) {
        super(context);
    }

    public ASCIIPaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ASCIIPaintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ASCIIPaintView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    {
        drawBuffer = new SpannableStringBuilder();
        temporaryBuffer = new SpannableStringBuilder();
        currentBuffer = drawBuffer;
        canvas = new InternalASCIICanvas();

        currentTool = new PencilTool(canvas);
        currentTool.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));

        setSingleLine(false);
        setTypeface(Typeface.MONOSPACE);
        setGravity(Gravity.CENTER);

        setTextSize(10);
        symbolWidth = getPaint().measureText("O");
        symbolHeight = getPaint().getTextSize();
        setLineSpacing(symbolHeight, 0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        columns = w / (int) symbolWidth;
        rows = h / (int) symbolHeight;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                drawBuffer.append(' ');
            }
            drawBuffer.append('\n');
        }

        setText(drawBuffer.toString());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                canvas.onDrawGestureStart();
                currentTool.onDrawStart(event.getX(), event.getY());
            case MotionEvent.ACTION_MOVE:
                currentTool.onDrawProgress(event.getX(), event.getY(), event.getPressure());
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                currentTool.onDrawEnd();
                canvas.onDrawGestureEnd();
                break;
        }
        return true;
    }

    public ASCIICanvas getCanvas() {
        return canvas;
    }

    public void setCanvas(ASCIICanvas canvas) {
        this.canvas = canvas;
    }

    public Tool getCurrentTool() {
        return currentTool;
    }

    public void setCurrentTool(Tool currentTool) {
        this.currentTool = currentTool;
    }

    private class InternalASCIICanvas implements ASCIICanvas {

        @Override
        public void drawToBuffer(char c, int row, int column, int color) {
            drawHorizontalToBuffer(String.valueOf(c), row, column, color);
        }

        @Override
        public void drawToBuffer(char c, int index, int color) {
            drawHorizontalToBuffer(String.valueOf(c), index, color);
        }

        @Override
        public void drawHorizontalToBuffer(CharSequence line, int row, int column, int color) {
            drawHorizontalToBuffer(line, toIndex(row, column), color);
        }

        @Override
        public void drawHorizontalToBuffer(CharSequence line, int start, int color) {
            int end = start + line.length();
            removeColorSpansAt(start, end - 1);
            currentBuffer.replace(start, end, line);
            currentBuffer.setSpan(
                    new ForegroundColorSpan(color), start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        @Override
        public void setTemporaryBuffer(boolean enabled) {
            currentBuffer = enabled ? temporaryBuffer : drawBuffer;
            temporaryBuffer.clear();
            if (currentBuffer == temporaryBuffer) {
                temporaryBuffer.insert(0, drawBuffer);
            }
        }

        @Override
        public void clearTempBufferChanges() {
            temporaryBuffer.replace(0, temporaryBuffer.length(), drawBuffer);
        }

        @Override
        public boolean isTempBufferEnabled() {
            return currentBuffer == temporaryBuffer;
        }

        @Override
        public int getColor(int index) {
            if (drawBuffer.charAt(index) == getEmptyChar()) {
                return NO_COLOR;
            }
            ForegroundColorSpan[] spans = drawBuffer.getSpans(index, index + 1, ForegroundColorSpan.class);
            return spans.length == 0 ? NO_COLOR : spans[0].getForegroundColor();
        }

        @Override
        public char getChar(int index) {
            return drawBuffer.charAt(index);
        }

        @Override
        public void drawToScreen() {
            setText(currentBuffer);
        }

        @Override
        public int toColumn(@FloatRange(from = 0) float x) {
            return (int) (Math.ceil(x) / symbolWidth);
        }

        @Override
        public int toRow(@FloatRange(from = 0) float y) {
            return (int) (Math.ceil(y) / symbolHeight);
        }

        @Override
        public int getColumns() {
            return columns;
        }

        @Override
        public int getRows() {
            return rows;
        }

        @Override
        public boolean isOnField(float x, float y) {
            return (x >= 0 && toColumn(x) < columns) && (y >= 0 && toRow(y) < rows);
        }

        @Override
        public char getEmptyChar() {
            return ' ';
        }

        @Override
        public void onDrawGestureStart() {

        }

        @Override
        public void onDrawGestureEnd() {

        }

        @Override
        public int toIndex(int row, int column) {
            return row * (columns + 1) + column;
        }

        @Override
        public ASCIIImage toASCIIImage() {
            int row = 0;
            char[][] data = new char[rows][columns];
            ASCIIImage.ColorRange[][] colors = new ASCIIImage.ColorRange[rows][];
            for (int i = 0; i < drawBuffer.length(); i += (columns + 1)) {
                drawBuffer.getChars(i, i + columns - 1, data[row], 0);
                colors[row] = collectColorRangesOn(row);
                row++;
            }
            ASCIIImage image = new ASCIIImage(data);
            image.setColors(colors);
            return image;
        }

        private ASCIIImage.ColorRange[] collectColorRangesOn(int row) {
            List<ASCIIImage.ColorRange> ranges = new ArrayList<>();
            int rowStartIndex = row * (columns + 1);
            ForegroundColorSpan[] spans = drawBuffer.getSpans(
                    rowStartIndex, rowStartIndex + columns - 1,
                    ForegroundColorSpan.class);
            for (ForegroundColorSpan span : spans) {
                int rangeStart = drawBuffer.getSpanStart(span);
                int rangeEnd = drawBuffer.getSpanEnd(span);
                int columnStart = rangeStart - (rangeStart / (columns + 1)) * (columns + 1);
                int columnEnd = columnStart + (rangeEnd - rangeStart);
                ranges.add(new ASCIIImage.ColorRange(
                        span.getForegroundColor(), columnStart,
                        columnEnd));
            }
            return ranges.toArray(new ASCIIImage.ColorRange[ranges.size()]);
        }

        private void removeColorSpansAt(int start, int end) {
            ForegroundColorSpan[] spans = currentBuffer.getSpans(start, end, ForegroundColorSpan.class);
            for (ForegroundColorSpan span : spans) {
                int spanStart = currentBuffer.getSpanStart(span);
                int spanEnd = currentBuffer.getSpanEnd(span);
                currentBuffer.removeSpan(span);
                boolean reused = false;
                if (spanStart < start) {
                    currentBuffer.setSpan(span, spanStart, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    reused = true;
                }
                if (end + 1 < spanEnd) {
                    span = reused ? new ForegroundColorSpan(span.getForegroundColor()) : span;
                    currentBuffer.setSpan(span, end + 1, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }

}
