package com.mrdeveloper.ascipaintdc.draw.action;

import android.util.SparseArray;

import com.mrdeveloper.ascipaintdc.draw.ASCIICanvas;

import java.util.Collections;

/**
 * Created by yarolegovich on 04-May-17.
 */

public class UndoableGesture implements DrawAction {

    private SparseArray<Replacement> changes;

    public UndoableGesture() {
        changes = new SparseArray<>();
    }

    @Override
    public void undoOn(ASCIICanvas canvas) {
        for (int i = 0; i < changes.size(); i++) {
            int index = changes.keyAt(i);
            Replacement change = changes.valueAt(i);
            canvas.drawToBuffer(change.oldSymbol, index, change.oldColor);
        }
        canvas.drawToScreen();
    }

    @Override
    public void doOn(ASCIICanvas canvas) {
        for (int i = 0; i < changes.size(); i++) {
            int index = changes.keyAt(i);
            Replacement change = changes.valueAt(i);
            canvas.drawToBuffer(change.newSymbol, index, change.newColor);
        }
        canvas.drawToScreen();
    }

    @Override
    public void addChange(ASCIICanvas canvas, int index, char symbol, int color) {
        if (canvas.isTempBufferEnabled()) {
            return;
        }
        Replacement replacement = changes.get(index);
        if (replacement == null) {
            replacement = new Replacement();
            replacement.set(
                    symbol, canvas.getChar(index),
                    color, canvas.getColor(index));
            changes.put(index, replacement);
        } else {
            if (replacement.newSymbol != symbol || replacement.newColor != color) {
                replacement.set(
                        symbol, canvas.getChar(index),
                        color, canvas.getColor(index));
            }
        }
    }


    public ChangeIterator getChanges() {
        return new ReplacementIterator();
    }

    @Override
    public boolean hasChanges() {
        return changes.size() > 0;
    }

    public static class Replacement {

        private char newSymbol;
        private char oldSymbol;
        private int newColor;
        private int oldColor;

        void set(char newSymbol, char oldSymbol, int newColor, int oldColor) {
            this.newSymbol = newSymbol;
            this.oldSymbol = oldSymbol;
            this.newColor = newColor;
            this.oldColor = oldColor;
        }
    }

    private class ReplacementIterator implements ChangeIterator {

        private int current;


        @Override
        public int getIndex() {
            return changes.keyAt(current);
        }

        @Override
        public char getSymbol() {
            return changes.valueAt(current).newSymbol;
        }

        @Override
        public int getColor() {
            return changes.valueAt(current).newColor;
        }

        @Override
        public boolean hasNext() {
            return current < changes.size() - 1;
        }

        @Override
        public void next() {
            current++;
        }
    }

}
