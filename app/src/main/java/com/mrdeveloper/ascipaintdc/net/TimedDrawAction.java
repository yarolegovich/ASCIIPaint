package com.mrdeveloper.ascipaintdc.net;

/**
 * Created by yarolegovich on 04-May-17.
 */

public class TimedDrawAction {

    private int row, column;
    private char symbol;
    private int color;
    private long timestamp;

    public void setRow(int row) {
        this.row = row;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
