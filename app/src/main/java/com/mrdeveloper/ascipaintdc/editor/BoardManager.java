package com.mrdeveloper.ascipaintdc.editor;

import com.mrdeveloper.ascipaintdc.draw.ASCIICanvas;
import com.mrdeveloper.ascipaintdc.draw.action.DrawAction;
import com.mrdeveloper.ascipaintdc.draw.model.DrawBoard;
import com.mrdeveloper.ascipaintdc.net.PublicBoardManager;
import com.mrdeveloper.ascipaintdc.net.Subscription;
import com.mrdeveloper.ascipaintdc.view.ASCIIPaintView;

/**
 * Created by yarolegovich on 04-May-17.
 */

public class BoardManager implements ASCIIPaintView.BufferStateListener {

    private PublicBoardManager publicBoardManager;
    private Subscription<DrawAction> changesSubscription;

    private ASCIICanvas canvas;
    private DrawBoard board;

    public BoardManager(DrawBoard board) {
        this.board = board;
        this.canvas = board.getCanvas();
    }

    @Override
    public void onBufferReady() {
        if (board.isPublic()) {
            publicBoardManager = new PublicBoardManager();
            changesSubscription = publicBoardManager.subscribeForDrawActions(board, new OnChange());
        } else {

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
    }

    private class OnChange implements PublicBoardManager.Callback<DrawAction> {

        @Override
        public void onResult(DrawAction result) {
            result.doOn(canvas);
        }

        @Override
        public void onError(Throwable e) {

        }
    }
}
