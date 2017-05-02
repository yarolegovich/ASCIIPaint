package com.yarolegovich.ascipaintdc.draw;

/**
 * Created by yarolegovich on 01-May-17.
 */

public interface ASCIICanvas {

    int NO_COLOR = -1;

    void drawToBuffer(char c, int row, int column, int color);

    void drawToBuffer(char c, int index, int color);

    void drawHorizontalToBuffer(CharSequence line, int row, int column, int color);

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

    char getSymbol(int index);

    int getColumns();

    int getRows();

    boolean isOnField(float x, float y);

    char getEmptyChar();

}
