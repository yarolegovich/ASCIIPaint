package com.mrdeveloper.asciipaint.view;

import android.content.Context;
import android.graphics.Point;
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

import com.mrdeveloper.asciipaint.R;
import com.mrdeveloper.asciipaint.draw.ASCIICanvas;
import com.mrdeveloper.asciipaint.draw.model.ASCIIImage;
import com.mrdeveloper.asciipaint.draw.tool.PencilTool;
import com.mrdeveloper.asciipaint.draw.tool.Tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by MrDeveloper on 01-May-17.
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

    private BufferStateListener bufferStateListener;

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

        if (bufferStateListener != null) {
            bufferStateListener.onBufferReady();
        }

        setText(drawBuffer);
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

    public void setBufferStateListener(BufferStateListener bufferStateListener) {
        this.bufferStateListener = bufferStateListener;
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
            if (color != NO_COLOR) {
                currentBuffer.setSpan(
                        new ForegroundColorSpan(color), start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
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
        public int getColor(int i) {
            if (drawBuffer.charAt(i) == getEmptyChar()) {
                return NO_COLOR;
            }
            ForegroundColorSpan[] spans = drawBuffer.getSpans(i, i + 1, ForegroundColorSpan.class);
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
        public int rowFromIndex(int index) {
            return index / (columns + 1);
        }

        @Override
        public int columnFromIndex(int index) {
            return index - rowFromIndex(index) * (columns + 1);
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
            ForegroundColorSpan[] spans = getSortedSpans(drawBuffer, row);
            if (spans.length == 0) {
                return new ASCIIImage.ColorRange[0];
            }
            List<ASCIIImage.ColorRange> ranges = new ArrayList<>();

            int[] spanBounds = new int[2];
            getSpanColumnBounds(drawBuffer, spans[0], spanBounds);
            ASCIIImage.ColorRange currentRange = newRange(spans[0], spanBounds);

            for (int i = 1; i < spans.length; i++) {
                ForegroundColorSpan span = spans[i];
                int spanColor = span.getForegroundColor();
                getSpanColumnBounds(drawBuffer, span, spanBounds);
                if (currentRange.getColor() != spanColor || currentRange.getRangeEnd() < spanBounds[0]) {
                    ranges.add(currentRange);
                    currentRange = newRange(span, spanBounds);
                } else {
                    currentRange.tryExpand(spanBounds[0], spanBounds[1]);
                }
            }
            ranges.add(currentRange);
            return ranges.toArray(new ASCIIImage.ColorRange[ranges.size()]);
        }

        private ASCIIImage.ColorRange newRange(ForegroundColorSpan span, int[] bounds) {
            return new ASCIIImage.ColorRange(span.getForegroundColor(), bounds[0], bounds[1]);
        }

        private void getSpanColumnBounds(
                SpannableStringBuilder buffer,
                ForegroundColorSpan span, int[] outBounds) {
            int rangeStart = buffer.getSpanStart(span);
            int rangeEnd = buffer.getSpanEnd(span);
            int columnStart = columnFromIndex(rangeStart);
            int columnEnd = Math.min(columnStart + (rangeEnd - rangeStart), columns);
            outBounds[0] = columnStart;
            outBounds[1] = columnEnd;
        }

        private ForegroundColorSpan[] getSortedSpans(final SpannableStringBuilder buffer, int row) {
            int rowStart = toIndex(row, 0);
            ForegroundColorSpan[] spans = drawBuffer.getSpans(
                    rowStart, rowStart + columns - 1,
                    ForegroundColorSpan.class);
            Arrays.sort(spans, new Comparator<ForegroundColorSpan>() {
                @Override
                public int compare(ForegroundColorSpan o1, ForegroundColorSpan o2) {
                    return buffer.getSpanEnd(o1) - buffer.getSpanEnd(o2);
                }
            });
            return spans;
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

    public interface BufferStateListener {
        void onBufferReady();
    }

}
