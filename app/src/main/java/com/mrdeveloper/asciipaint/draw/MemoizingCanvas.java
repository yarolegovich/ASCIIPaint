package com.mrdeveloper.asciipaint.draw;

import com.mrdeveloper.asciipaint.draw.action.UndoableGesture;
import com.mrdeveloper.asciipaint.draw.model.ASCIIImage;
import com.mrdeveloper.asciipaint.draw.action.DrawAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MrDeveloper on 02-May-17.
 */

public class MemoizingCanvas implements ASCIICanvas {

    private static final int STACK_MAX_SIZE = 5;

    private ASCIICanvas wrapped;

    private List<DrawAction> undoStack;
    private List<DrawAction> redoStack;

    private DrawAction currentAction;

    private HistoryChangeListener listener;

    public MemoizingCanvas(ASCIICanvas wrapped) {
        this.wrapped = wrapped;
        this.undoStack = new ArrayList<>();
        this.redoStack = new ArrayList<>();
    }

    public void undo() {
        if (undoStack.isEmpty()) {
            return;
        }
        DrawAction action = undoStack.remove(undoStack.size() - 1);
        action.undoOn(wrapped);
        redoStack.add(action);
        notifyDrawHistoryChanged();
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            return;
        }
        DrawAction action = redoStack.remove(redoStack.size() - 1);
        action.doOn(wrapped);
        undoStack.add(action);
        notifyDrawHistoryChanged();
    }

    @Override
    public void drawToBuffer(char c, int index, int color) {
        memoizeAction(String.valueOf(c), index, color);
        wrapped.drawToBuffer(c, index, color);
    }

    @Override
    public void drawToBuffer(char c, int row, int column, int color) {
        memoizeAction(String.valueOf(c), wrapped.toIndex(row, column), color);
        wrapped.drawToBuffer(c, row, column, color);
    }

    @Override
    public void drawHorizontalToBuffer(CharSequence line, int index, int color) {
        memoizeAction(line, index, color);
        wrapped.drawHorizontalToBuffer(line, index, color);
    }

    @Override
    public void drawHorizontalToBuffer(CharSequence line, int row, int column, int color) {
        memoizeAction(line, wrapped.toIndex(row, column), color);
        wrapped.drawHorizontalToBuffer(line, row, column, color);
    }

    private void memoizeAction(CharSequence line, int index, int color) {
        if (wrapped.isTempBufferEnabled()) {
            return;
        }
        for (int i = 0; i < line.length(); i++) {
            currentAction.addChange(wrapped, index + i, line.charAt(i), color);
            if (listener != null) {
                listener.onDrawAction(index + i, line.charAt(i), color);
            }
        }
    }

    @Override
    public void onDrawGestureStart() {
        currentAction = new UndoableGesture();
        redoStack.clear();
        notifyDrawHistoryChanged();
    }

    @Override
    public void onDrawGestureEnd() {
        if (currentAction.hasChanges()) {
            if (undoStack.size() == STACK_MAX_SIZE) {
                undoStack.remove(0);
            }
            undoStack.add(currentAction);
            notifyDrawHistoryChanged();
        }
        wrapped.onDrawGestureEnd();
    }

    private void notifyDrawHistoryChanged() {
        if (listener != null) {
            listener.onDrawHistoryChanged();
        }
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    @Override
    public void setTemporaryBuffer(boolean enabled) {
        wrapped.setTemporaryBuffer(enabled);
    }

    @Override
    public void clearTempBufferChanges() {
        wrapped.clearTempBufferChanges();
    }

    @Override
    public boolean isTempBufferEnabled() {
        return wrapped.isTempBufferEnabled();
    }

    @Override
    public void drawToScreen() {
        wrapped.drawToScreen();
    }

    @Override
    public int toColumn(float x) {
        return wrapped.toColumn(x);
    }

    @Override
    public int toRow(float y) {
        return wrapped.toRow(y);
    }

    @Override
    public int toIndex(int row, int col) {
        return wrapped.toIndex(row, col);
    }

    @Override
    public int rowFromIndex(int index) {
        return wrapped.rowFromIndex(index);
    }

    @Override
    public int columnFromIndex(int index) {
        return wrapped.columnFromIndex(index);
    }

    @Override
    public int getColor(int index) {
        return wrapped.getColor(index);
    }

    @Override
    public int getColumns() {
        return wrapped.getColumns();
    }

    @Override
    public int getRows() {
        return wrapped.getRows();
    }

    @Override
    public boolean isOnField(float x, float y) {
        return wrapped.isOnField(x, y);
    }

    @Override
    public char getEmptyChar() {
        return wrapped.getEmptyChar();
    }

    @Override
    public char getChar(int index) {
        return wrapped.getChar(index);
    }

    @Override
    public ASCIIImage toASCIIImage() {
        return wrapped.toASCIIImage();
    }

    public void setListener(HistoryChangeListener historyChangeListener) {
        this.listener = historyChangeListener;
    }

    public interface HistoryChangeListener {
        void onDrawHistoryChanged();

        void onDrawAction(int index, char symbol, int color);
    }

}
