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
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.TextView;

import com.yarolegovich.ascipaintdc.R;
import com.yarolegovich.ascipaintdc.draw.ASCIICanvas;
import com.yarolegovich.ascipaintdc.draw.tool.PencilTool;
import com.yarolegovich.ascipaintdc.draw.tool.Tool;

/**
 * Created by yarolegovich on 01-May-17.
 */

public class ASCIIPaintView extends TextView {

    private SpannableStringBuilder drawBuffer;
    private SpannableStringBuilder temporaryBuffer;
    private boolean isTemporaryEnabled;

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
            drawToBuffer(c, toIndex(row, column), color);
        }

        @Override
        public void drawToBuffer(char c, int index, int color) {
            if (isTemporaryEnabled) {
                drawToBuffer(temporaryBuffer, c, index, color);
            } else {
                drawToBuffer(drawBuffer, c, index, color);
            }
        }

        @Override
        public void drawHorizontalToBuffer(CharSequence line, int row, int column, int color) {
            if (isTemporaryEnabled) {
                drawHorizontalToBuffer(temporaryBuffer, line, row, column, color);
            } else {
                drawHorizontalToBuffer(drawBuffer, line, row, column, color);
            }
        }

        @Override
        public void setTemporaryBuffer(boolean enabled) {
            isTemporaryEnabled = enabled;
            temporaryBuffer.clear();
            if (isTemporaryEnabled) {
                temporaryBuffer.insert(0, drawBuffer);
            }
        }

        @Override
        public void clearTempBufferChanges() {
            temporaryBuffer.replace(0, temporaryBuffer.length(), drawBuffer);
        }

        @Override
        public boolean isTempBufferEnabled() {
            return isTemporaryEnabled;
        }

        private void drawToBuffer(SpannableStringBuilder buffer, char c, int index, int color) {
            removeColorSpansAt(buffer, index, index);
            buffer.replace(index, index + 1, String.valueOf(c));
            if (c != getEmptyChar() && color != NO_COLOR) {
                buffer.setSpan(
                        new ForegroundColorSpan(color), index, index + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        private void drawHorizontalToBuffer(
                SpannableStringBuilder buffer,
                CharSequence line, int row, int column, int color) {
            int start = toIndex(row, column);
            int end = start + line.length();
            removeColorSpansAt(buffer, start, end - 1);
            buffer.replace(start, end, line);
            buffer.setSpan(
                    new ForegroundColorSpan(color), start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        public char getSymbol(int index) {
            return drawBuffer.charAt(index);
        }

        @Override
        public void drawToScreen() {
            if (isTemporaryEnabled) {
                setText(temporaryBuffer);
            } else {
                setText(drawBuffer);
            }
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

        private void removeColorSpansAt(SpannableStringBuilder buffer, int start, int end) {
            ForegroundColorSpan[] spans = buffer.getSpans(start, end, ForegroundColorSpan.class);
            for (ForegroundColorSpan span : spans) {
                int spanStart = buffer.getSpanStart(span);
                int spanEnd = buffer.getSpanEnd(span);
                buffer.removeSpan(span);
                boolean reused = false;
                if (spanStart < start) {
                    reused = true;
                    buffer.setSpan(new ForegroundColorSpan(span.getForegroundColor()),
                            spanStart, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (end + 1 < spanEnd) {
                    ForegroundColorSpan s = reused ?
                            new ForegroundColorSpan(span.getForegroundColor()) :
                            span;
                    buffer.setSpan(s, end + 1, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }

}
