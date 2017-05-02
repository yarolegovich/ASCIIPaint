package com.yarolegovich.ascipaintdc.draw;

import android.util.Log;
import android.util.SparseArray;

/**
 * Created by yarolegovich on 02-May-17.
 */

public class DrawAction {

    private SparseArray<Replacement> changes;

    public DrawAction() {
        changes = new SparseArray<>();
    }

    public void undo(ASCIICanvas canvas) {
        for (int i = 0; i < changes.size(); i++) {
            int index = changes.keyAt(i);
            Replacement change = changes.valueAt(i);
            canvas.drawToBuffer(change.oldSymbol, index, change.oldColor);
        }
        canvas.drawToScreen();
    }

    public void redo(ASCIICanvas canvas) {
        for (int i = 0; i < changes.size(); i++) {
            int index = changes.keyAt(i);
            Replacement change = changes.valueAt(i);
            canvas.drawToBuffer(change.newSymbol, index, change.newColor);
        }
        canvas.drawToScreen();
    }

    public void onChange(ASCIICanvas canvas, int index, char symbol, int color) {
        if (canvas.isTempBufferEnabled()) {
            return;
        }
        Replacement replacement = changes.get(index);
        if (replacement == null) {
            replacement = new Replacement();
            replacement.set(
                    symbol, canvas.getSymbol(index),
                    color, canvas.getColor(index));
            changes.put(index, replacement);
        } else {
            if (replacement.newSymbol != symbol || replacement.newColor != color) {
                replacement.set(
                        symbol, canvas.getSymbol(index),
                        color, canvas.getColor(index));
            }
        }
    }

    public boolean hasChanges() {
        return changes.size() > 0;
    }

    private static class Replacement {

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
}
