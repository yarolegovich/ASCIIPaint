package com.mrdeveloper.ascipaintdc.net;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.mrdeveloper.ascipaintdc.draw.ASCIICanvas;
import com.mrdeveloper.ascipaintdc.draw.action.BatchedChange;
import com.mrdeveloper.ascipaintdc.draw.action.DrawAction;
import com.mrdeveloper.ascipaintdc.draw.model.DrawBoard;
import com.mrdeveloper.ascipaintdc.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Created by yarolegovich on 04-May-17.
 */

public class PublicBoardManager {

    private static final long DELAY_BATCH_DELIVERY = 800;
    private static final int SIZE_BATCH = 512;

    private static final String PATH_BOARDS = "/";
    private static final String FIELD_BOARD_NAME = "name";
    private static final String FIELD_CONTENT = "content";

    private FirebaseDatabase database;
    private DatabaseReference contentRef;
    private CellChangeListener cellChangeListener;

    private Set<String> selfMadeChanges;

    public PublicBoardManager() {
        database = FirebaseDatabase.getInstance();
        selfMadeChanges = new HashSet<>();
    }

    public Subscription<DrawBoard> createBoard(final String boardName, Callback<DrawBoard> boardCreatedCallback) {
        final Subscription<DrawBoard> subscription = new Subscription<>(boardCreatedCallback);
        DatabaseReference boardsMeta = database.getReference(PATH_BOARDS);
        final DatabaseReference newBoard = boardsMeta.push();
        newBoard.child(FIELD_BOARD_NAME).setValue(boardName)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        subscription.notifyError(e);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        subscription.notifyResult(new DrawBoard(newBoard.getKey(), boardName, true));
                    }
                });
        return subscription;
    }

    public Subscription<List<DrawBoard>> getBoards(Callback<List<DrawBoard>> callback) {
        final Subscription<List<DrawBoard>> subscription = new Subscription<>(callback);
        DatabaseReference boards = database.getReference(PATH_BOARDS);
        boards.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (subscription.isCancelled()) {
                    return;
                }
                List<DrawBoard> boards = new ArrayList<>();
                for (DataSnapshot board : dataSnapshot.getChildren()) {
                    boards.add(new DrawBoard(board.getKey(),
                            board.child(FIELD_BOARD_NAME).getValue(String.class),
                            true));
                }
                subscription.notifyResult(boards);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                subscription.notifyError(databaseError.toException());
            }
        });
        return subscription;
    }

    public Subscription<DrawAction> subscribeForDrawActions(DrawBoard board, Callback<DrawAction> drawActionCallback) {
        Subscription<DrawAction> subscription = new Subscription<>(drawActionCallback);
        ASCIICanvas canvas = board.getCanvas();
        if (canvas == null) {
            throw new IllegalArgumentException("Board must have a reference to ASCIICanvas");
        }
        clearRefAndUnsubscribe();
        contentRef = getContentRefFor(board);
        cellChangeListener = new CellChangeListener(board, subscription, contentRef);
        for (int row = 0; row < canvas.getRows(); row++) {
            for (int col = 0; col < canvas.getColumns(); col++) {
                contentRef.child(toCellKey(row, col)).addValueEventListener(cellChangeListener);
            }
        }
        return subscription;
    }

    private void unsubscribeFromDrawActions(CellChangeListener listener) {
        if (listener == null) {
            return;
        }
        DatabaseReference contentRef = listener.getObservedRef();
        ASCIICanvas canvas = listener.getBoard().getCanvas();
        //noinspection ConstantConditions
        for (int row = 0; row < canvas.getRows(); row++) {
            for (int col = 0; col < canvas.getColumns(); col++) {
                String key = toCellKey(row, col);
                contentRef.child(key).removeEventListener(listener);
            }
        }
    }

    private void clearRefAndUnsubscribe() {
        selfMadeChanges.clear();
        unsubscribeFromDrawActions(cellChangeListener);
        cellChangeListener = null;
        contentRef = null;
    }

    public void submitDrawAction(DrawBoard board, int index, char symbol, int color) {
        ASCIICanvas canvas = board.getCanvas();
        if (canvas == null) {
            return;
        }
        List<Integer> data = Arrays.asList((int) symbol, color);
        int row = canvas.rowFromIndex(index);
        int column = canvas.columnFromIndex(index);
        String key = toCellKey(row, column);
        selfMadeChanges.add(key);
        getContentRefFor(board).child(key).setValue(data);
    }

    private DatabaseReference getContentRefFor(DrawBoard board) {
        if (contentRef == null) {
            contentRef = database.getReference(PATH_BOARDS).child(board.getKey()).child(FIELD_CONTENT);
        }
        return contentRef;
    }

    private String toCellKey(int row, int column) {
        return String.format(Locale.US, "%d,%d", row, column);
    }

    private class CellChangeListener implements ValueEventListener {

        private DrawBoard board;
        private ASCIICanvas canvas;
        private Subscription<DrawAction> subscription;

        private BatchedChange batchedChange;

        private Handler delayedDeliveryHandler;

        private DatabaseReference observedRef;

        private CellChangeListener(DrawBoard board, Subscription<DrawAction> subscription, DatabaseReference observedRef) {
            this.board = board;
            this.canvas = board.getCanvas();
            this.subscription = subscription;
            this.observedRef = observedRef;
            this.batchedChange = new BatchedChange(SIZE_BATCH);
            this.delayedDeliveryHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (subscription.isCancelled()) {
                unsubscribeFromDrawActions(this);
                return;
            }
            String key = dataSnapshot.getKey();
            if (selfMadeChanges.contains(key)) {
                selfMadeChanges.remove(key);
                return;
            }
            delayedDeliveryHandler.removeCallbacks(deliverBatchTask);
            if (batchedChange.isBatchFull()) {
                subscription.notifyResult(batchedChange);
                batchedChange = new BatchedChange(SIZE_BATCH);
            }
            String[] rowColumnStr = key.split(",");
            int row = Integer.parseInt(rowColumnStr[0]);
            int column = Integer.parseInt(rowColumnStr[1]);
            //noinspection unchecked
            List<Integer> values = dataSnapshot.getValue(SYMBOL_COLOR_PAIR);
            if (values != null && values.size() == 2) {
                batchedChange.addChange(canvas,
                        canvas.toIndex(row, column),
                        (char) values.get(0).intValue(),
                        values.get(1));
            }
            delayedDeliveryHandler.postDelayed(deliverBatchTask, DELAY_BATCH_DELIVERY);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            if (subscription.isCancelled()) {
                unsubscribeFromDrawActions(this);
                return;
            }
            subscription.notifyError(databaseError.toException());
        }

        public DrawBoard getBoard() {
            return board;
        }

        public DatabaseReference getObservedRef() {
            return observedRef;
        }

        private GenericTypeIndicator<List<Integer>> SYMBOL_COLOR_PAIR =
                new GenericTypeIndicator<List<Integer>>() {
                };

        private Runnable deliverBatchTask = new Runnable() {
            @Override
            public void run() {
                BatchedChange changes = batchedChange;
                if (changes.hasChanges()) {
                    batchedChange = new BatchedChange(SIZE_BATCH);
                }
                subscription.notifyResult(changes);
            }
        };

    }

    public interface Callback<T> {
        void onResult(T result);

        void onError(Throwable e);
    }
}
