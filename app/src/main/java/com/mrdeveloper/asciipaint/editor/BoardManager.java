package com.mrdeveloper.asciipaint.editor;

import android.content.Context;
import android.os.Bundle;

import com.mrdeveloper.asciipaint.data.LocalBoardManager;
import com.mrdeveloper.asciipaint.draw.ASCIICanvas;
import com.mrdeveloper.asciipaint.draw.action.DrawAction;
import com.mrdeveloper.asciipaint.draw.model.ASCIIImage;
import com.mrdeveloper.asciipaint.draw.model.DrawBoard;
import com.mrdeveloper.asciipaint.draw.tool.PresetImageTool;
import com.mrdeveloper.asciipaint.net.PublicBoardManager;
import com.mrdeveloper.asciipaint.util.Callback;
import com.mrdeveloper.asciipaint.util.Logger;
import com.mrdeveloper.asciipaint.util.Subscription;
import com.mrdeveloper.asciipaint.view.ASCIIPaintView;

/**
 * Created by MrDeveloper on 04-May-17.
 */

public class BoardManager implements ASCIIPaintView.BufferStateListener {

    private static final String EXTRA_SAVED_IMAGE = "saved_image";

    private LocalBoardManager localBoardManager;
    private Subscription<ASCIIImage> imageSubscription;

    private PublicBoardManager publicBoardManager;
    private Subscription<DrawAction> changesSubscription;

    private ASCIICanvas canvas;
    private DrawBoard board;
    private Bundle savedState;

    private Context context;

    public BoardManager(Context context, DrawBoard board, Bundle savedState) {
        this.context = context;
        this.board = board;
        this.canvas = board.getCanvas();
        this.savedState = savedState;
    }

    @Override
    public void onBufferReady() {
        if (board.isPublic()) {
            publicBoardManager = new PublicBoardManager();
            if (savedState == null) {
                changesSubscription = publicBoardManager.subscribeForDrawActions(board, new OnChange());
            }
        } else {
            localBoardManager = new LocalBoardManager(context);
            if (savedState == null) {
                imageSubscription = localBoardManager.loadImageForBoard(board, new OnImageLoaded());
            }
        }
        if (savedState != null) {
            ASCIIImage image = savedState.getParcelable(EXTRA_SAVED_IMAGE);
            drawImageOnCanvas(image);
        }
    }

    public void onDrawAction(int index, char symbol, int color) {
        if (publicBoardManager != null) {
            publicBoardManager.submitDrawAction(board, index, symbol, color);
        }
    }

    public void exitTheBoard() {
        if (changesSubscription != null) {
            changesSubscription.cancel();
        }
        if (imageSubscription != null) {
            imageSubscription.cancel();
        }
        if (localBoardManager != null) {
            localBoardManager.saveTheBoard(context, board);
        }
    }

    public void drawImageOnCanvas(ASCIIImage image) {
        PresetImageTool tool = new PresetImageTool(canvas, image);
        tool.drawToBufferAt(canvas.getRows() / 2, canvas.getColumns() / 2);
        canvas.drawToScreen();
    }

    public void saveStateTo(Bundle outState) {
        outState.putParcelable(EXTRA_SAVED_IMAGE, canvas.toASCIIImage());
    }

    public void notifyError(Throwable e) {
        Logger.e(e);
    }

    private class OnImageLoaded implements Callback<ASCIIImage> {

        @Override
        public void onResult(ASCIIImage result) {
            drawImageOnCanvas(result);
        }

        @Override
        public void onError(Throwable e) { /* NOP */ }
    }

    private class OnChange implements Callback<DrawAction> {

        @Override
        public void onResult(DrawAction result) {
            result.doOn(canvas);
        }

        @Override
        public void onError(Throwable e) {
            notifyError(e);
        }
    }
}
