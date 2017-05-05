package com.mrdeveloper.asciipaint;

import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.View;
import android.widget.CheckBox;

import com.mrdeveloper.asciipaint.adapter.BoardsAdapter;
import com.mrdeveloper.asciipaint.data.LocalBoardManager;
import com.mrdeveloper.asciipaint.draw.model.DrawBoard;
import com.mrdeveloper.asciipaint.net.PublicBoardManager;
import com.mrdeveloper.asciipaint.util.Callback;
import com.mrdeveloper.asciipaint.util.Subscription;
import com.mrdeveloper.asciipaint.util.Utils;
import com.mrdeveloper.asciipaint.view.OverlayView;

import java.util.Collections;
import java.util.List;

/**
 * Created by MrDeveloper on 04-May-17.
 */

public class BoardListActivity extends AppCompatActivity implements View.OnClickListener,
        BoardsAdapter.Listener {

    private BottomSheetBehavior bottomSheet;

    private TextInputLayout boardNameEt;
    private CheckBox isPublicBoardCb;
    private FloatingActionButton fab;
    private OverlayView overlay;

    private BoardsAdapter boardsAdapter;

    private LocalBoardManager localBoardManager;
    private PublicBoardManager publicBoardManager;
    private Subscription<List<DrawBoard>> boardsSubscription;
    private Subscription<DrawBoard> newBoardSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        overlay = (OverlayView) findViewById(R.id.overlay);
        overlay.setOnClickListener(this);

        boardNameEt = (TextInputLayout) findViewById(R.id.bs_board_name);
        isPublicBoardCb = (CheckBox) findViewById(R.id.bs_is_board_public);

        bottomSheet = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_add_board));
        bottomSheet.setPeekHeight(0);
        Utils.disableBottomSheetDrag(bottomSheet);

        fab = (FloatingActionButton) findViewById(R.id.btn_add_board);
        fab.setOnClickListener(this);

        boardsAdapter = new BoardsAdapter();
        boardsAdapter.setListener(this);
        RecyclerView boardList = (RecyclerView) findViewById(R.id.board_list);
        boardList.setLayoutManager(new LinearLayoutManager(this));
        boardList.setAdapter(boardsAdapter);

        localBoardManager = new LocalBoardManager(this);
        boardsAdapter.addBoards(localBoardManager.getBoards());
        publicBoardManager = new PublicBoardManager();
        boardsSubscription = publicBoardManager.getBoards(new BoardQueryCallback());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        boardsSubscription.cancel();
        if (newBoardSubscription != null) {
            newBoardSubscription.cancel();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_board:
                if (bottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    createBoard();
                } else if (bottomSheet.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    showAddBoardSheet();
                }
                break;
            case R.id.overlay:
                hideAddBoardSheet();
                break;
        }
    }

    private void createBoard() {
        //noinspection ConstantConditions
        Editable boardName = boardNameEt.getEditText().getText();
        if (boardName.length() == 0) {
            boardNameEt.setError(getString(R.string.bs_error_empty_name));
            return;
        }
        boolean isPublic = isPublicBoardCb.isChecked();

        resetNewBoardSheet();

        if (isPublic) {
            newBoardSubscription = publicBoardManager.createBoard(
                    boardName.toString(),
                    new NewBoardCallback());
        } else {
            DrawBoard newBoard = localBoardManager.createBoard(boardName.toString());
            boardsAdapter.addBoards(Collections.singletonList(newBoard));
            openBoard(newBoard);
        }
    }

    private void resetNewBoardSheet() {
        //noinspection ConstantConditions
        boardNameEt.getEditText().setText("");
        boardNameEt.setError(null);
        isPublicBoardCb.setChecked(false);
    }

    @Override
    public void onBoardClicked(DrawBoard board) {
        openBoard(board);
    }

    private void openBoard(DrawBoard board) {
        startActivity(BoardActivity.callingIntent(this, board));
        hideAddBoardSheet();
    }

    private void showAddBoardSheet() {
        fab.setImageResource(R.drawable.ic_arrow_forward_white_24dp);
        bottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
        overlay.show();
    }

    private void hideAddBoardSheet() {
        fab.setImageResource(R.drawable.ic_add_white_24dp);
        bottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
        overlay.hide();
    }

    private void notifyError(Throwable e) {
        Snackbar.make(overlay, e.getMessage(), Snackbar.LENGTH_SHORT).show();
    }

    private class BoardQueryCallback implements Callback<List<DrawBoard>> {

        @Override
        public void onResult(List<DrawBoard> result) {
            boardsAdapter.addBoards(result);
        }

        @Override
        public void onError(Throwable e) {
            notifyError(e);
        }
    }

    private class NewBoardCallback implements Callback<DrawBoard> {

        @Override
        public void onResult(DrawBoard result) {
            boardsAdapter.addBoards(Collections.singletonList(result));
            openBoard(result);
        }

        @Override
        public void onError(Throwable e) {
            notifyError(e);
        }
    }
}
