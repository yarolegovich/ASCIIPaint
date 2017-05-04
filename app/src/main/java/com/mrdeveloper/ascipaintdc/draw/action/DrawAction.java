package com.mrdeveloper.ascipaintdc.draw.action;

import android.util.SparseArray;

import com.mrdeveloper.ascipaintdc.draw.ASCIICanvas;

/**
 * Created by yarolegovich on 02-May-17.
 */

public interface DrawAction {

    void undoOn(ASCIICanvas canvas);

    void doOn(ASCIICanvas canvas);

    void addChange(ASCIICanvas canvas, int index, char symbol, int color);

    boolean hasChanges();

    ChangeIterator getChanges();

    interface ChangeIterator {

        int getIndex();

        char getSymbol();

        int getColor();

        void next();

        boolean hasNext();
    }
}
