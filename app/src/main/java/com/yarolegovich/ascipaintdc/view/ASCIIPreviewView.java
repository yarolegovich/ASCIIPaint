package com.yarolegovich.ascipaintdc.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.TextView;

import com.yarolegovich.ascipaintdc.draw.ASCIIImage;

/**
 * Created by yarolegovich on 02-May-17.
 */

public class ASCIIPreviewView extends TextView {

    private ASCIIImage imageForPreview;
    private boolean shouldCalculateSize;

    public ASCIIPreviewView(Context context) {
        super(context);
    }

    public ASCIIPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ASCIIPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ASCIIPreviewView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    {
        setTypeface(Typeface.MONOSPACE);
        setGravity(Gravity.CENTER);
        setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (shouldCalculateSize && imageForPreview != null) {
            calculateSize();
            showImage();
            shouldCalculateSize = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return !hasOnClickListeners() || super.onTouchEvent(event);
    }

    private void calculateSize() {
        char[] row = imageForPreview.getRawRow(0);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        TextPaint paint = getPaint();
        float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, getTextSize(), dm);
        boolean previewFits = false;
        do {
            float width = paint.measureText(row, 0, row.length);
            float height = paint.getTextSize() * imageForPreview.getRowCount();
            if (width > getWidth() || height > getHeight()) {
                size--;
                if (size == 0) {
                    break;
                }
                setTextSize(size);
            } else {
                previewFits = true;
            }
        } while (!previewFits);
        setLineSpacing(paint.getTextSize(), 0);
    }

    public void preview(ASCIIImage image) {
        imageForPreview = image;
        if (getWidth() == 0 || getHeight() == 0) {
            shouldCalculateSize = true;
        } else {
            calculateSize();
            showImage();
        }
    }

    private void showImage() {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < imageForPreview.getRowCount(); i++) {
            buffer.append(imageForPreview.getRawRow(i)).append('\n');
        }
        setText(buffer);
    }
}
