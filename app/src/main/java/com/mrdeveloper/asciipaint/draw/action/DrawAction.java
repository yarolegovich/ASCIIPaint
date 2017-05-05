package com.mrdeveloper.asciipaint.draw.action;

import com.mrdeveloper.asciipaint.draw.ASCIICanvas;

/**
 * Created by MrDeveloper on 02-May-17.
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
