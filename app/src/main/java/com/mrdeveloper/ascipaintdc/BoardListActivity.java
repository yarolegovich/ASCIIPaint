package com.mrdeveloper.ascipaintdc;

import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.View;
import android.widget.CheckBox;

import com.google.firebase.FirebaseApp;
import com.mrdeveloper.ascipaintdc.adapter.BoardsAdapter;
import com.mrdeveloper.ascipaintdc.data.PrivateBoardManager;
import com.mrdeveloper.ascipaintdc.draw.model.DrawBoard;
import com.mrdeveloper.ascipaintdc.net.PublicBoardManager;
import com.mrdeveloper.ascipaintdc.net.Subscription;
import com.mrdeveloper.ascipaintdc.util.Utils;
import com.mrdeveloper.ascipaintdc.view.OverlayView;

import java.util.Collections;
import java.util.List;

/**
 * Created by yarolegovich on 04-May-17.
 */

public class BoardListActivity extends AppCompatActivity implements View.OnClickListener,
        BoardsAdapter.Listener {

    private BottomSheetBehavior bottomSheet;

    private TextInputLayout boardName;
    private CheckBox isPublicBoard;
    private FloatingActionButton fab;
    private OverlayView overlay;

    private BoardsAdapter boardsAdapter;

    private PrivateBoardManager privateBoardManager;
    private PublicBoardManager publicBoardManager;
    private Subscription<List<DrawBoard>> boardsSubscription;
    private Subscription<DrawBoard> newBoardSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boards);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        overlay = (OverlayView) findViewById(R.id.overlay);
        overlay.setOnClickListener(this);

        boardName = (TextInputLayout) findViewById(R.id.bs_board_name);
        isPublicBoard = (CheckBox) findViewById(R.id.bs_is_board_public);

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

        privateBoardManager = new PrivateBoardManager();
        boardsAdapter.addBoards(privateBoardManager.getBoards());
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
        Editable boardName = this.boardName.getEditText().getText();
        if (boardName.length() == 0) {
            this.boardName.setError("Must be not empty");
            return;
        }
        if (isPublicBoard.isChecked()) {
            newBoardSubscription = publicBoardManager.createBoard(
                    boardName.toString(),
                    new NewBoardCallback());
        } else {
            DrawBoard newBoard = privateBoardManager.createBoard(boardName.toString());
            boardsAdapter.addBoards(Collections.singletonList(newBoard));
            openBoard(newBoard);
        }
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

    }

    private class BoardQueryCallback implements PublicBoardManager.Callback<List<DrawBoard>> {

        @Override
        public void onResult(List<DrawBoard> result) {
            boardsAdapter.addBoards(result);
        }

        @Override
        public void onError(Throwable e) {
            notifyError(e);
        }
    }

    private class NewBoardCallback implements PublicBoardManager.Callback<DrawBoard> {

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
