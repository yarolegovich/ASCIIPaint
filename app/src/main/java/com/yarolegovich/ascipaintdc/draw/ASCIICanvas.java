package com.yarolegovich.ascipaintdc.draw;

import android.graphics.Color;

/**
 * Created by yarolegovich on 01-May-17.
 */

public interface ASCIICanvas {

    int NO_COLOR = Color.TRANSPARENT;

    void drawToBuffer(char c, int row, int column, int color);

    void drawToBuffer(char c, int index, int color);

    void drawHorizontalToBuffer(CharSequence line, int row, int column, int color);

    void drawHorizontalToBuffer(CharSequence line, int index, int color);

    void setTemporaryBuffer(boolean enabled);

    void clearTempBufferChanges();

    boolean isTempBufferEnabled();

    void drawToScreen();

    void onDrawGestureStart();

    void onDrawGestureEnd();

    int toColumn(float x);

    int toRow(float y);

    int toIndex(int row, int col);

    int getColor(int index);

    char getChar(int index);

    int getColumns();

    int getRows();

    boolean isOnField(float x, float y);

    char getEmptyChar();

    ASCIIImage toASCIIImage();

}
