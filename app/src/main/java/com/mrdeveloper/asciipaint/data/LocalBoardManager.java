package com.mrdeveloper.asciipaint.data;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.mrdeveloper.asciipaint.R;
import com.mrdeveloper.asciipaint.draw.model.ASCIIImage;
import com.mrdeveloper.asciipaint.draw.model.DrawBoard;
import com.mrdeveloper.asciipaint.util.Callback;
import com.mrdeveloper.asciipaint.util.Subscription;
import com.mrdeveloper.asciipaint.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by MrDeveloper on 04-May-17.
 */

public class LocalBoardManager {

    private static final String LOCAL_BOARDS_FOLDER_NAME = "local_boards";
    private final File LOCAL_BOARDS_FOLDER;

    private Context context;

    private Executor executor;
    private Handler uiThreadHandler;

    public LocalBoardManager(Context context) {
        this.context = context.getApplicationContext();
        this.uiThreadHandler = new Handler(Looper.getMainLooper());
        this.executor = Executors.newSingleThreadExecutor();

        LOCAL_BOARDS_FOLDER = new File(
                context.getFilesDir(),
                LOCAL_BOARDS_FOLDER_NAME);
        if (!LOCAL_BOARDS_FOLDER.exists()) {
            //noinspection ResultOfMethodCallIgnored
            LOCAL_BOARDS_FOLDER.mkdir();
        }
    }

    public DrawBoard createBoard(String name) {
        return new DrawBoard(
                String.valueOf(System.currentTimeMillis()),
                name, false);
    }

    public Subscription<ASCIIImage> loadImageForBoard(final DrawBoard board, Callback<ASCIIImage> callback) {
        final Subscription<ASCIIImage> subscription = new Subscription<>(callback);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                InputStream is = null;
                ContentResolver cr = context.getContentResolver();
                try {
                    File boardFile = new File(LOCAL_BOARDS_FOLDER, toFileName(board));
                    is = cr.openInputStream(Uri.fromFile(boardFile));
                    JSONSerializer serializer = new JSONSerializer();
                    ASCIIImage image = serializer.fromJson(is);
                    if (image == null) {
                        notifyErrorOnUi(
                                subscription,
                                new NullPointerException("Wasn't able to load the image"));
                    } else {
                        notifyOnUi(subscription, image);
                    }
                } catch (FileNotFoundException e) {
                    notifyErrorOnUi(subscription, e);
                } finally {
                    Utils.closeSilently(is);
                }
            }
        });
        return subscription;
    }

    public void saveTheBoard(final Context context, final DrawBoard board) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (board.getCanvas() == null) {
                    return;
                }
                ASCIIImage image = board.getCanvas().toASCIIImage();
                image.setName(board.getName());
                File outFile = new File(LOCAL_BOARDS_FOLDER, toFileName(board));
                Intent taskIntent = ExportToJsonService.callingIntent(context, outFile, image);
                context.startService(taskIntent);
            }
        });
    }

    public List<DrawBoard> getBoards() {
        List<DrawBoard> result = new ArrayList<>();
        File[] boardFiles = LOCAL_BOARDS_FOLDER.listFiles();

        if (boardFiles == null || boardFiles.length == 0) {
            return Collections.singletonList(
                    createBoard(context.getString(R.string.default_local_board_name)));
        }

        for (File board : boardFiles) {
            try {
                String keyName = board.getName();
                int keyEndIndex = 0;
                while (keyName.charAt(keyEndIndex) != '_') {
                    keyEndIndex++;
                }
                String key = keyName.substring(0, keyEndIndex);
                String name = keyName.substring(keyEndIndex + 1);
                result.add(new DrawBoard(key, name, false));
            } catch (Exception e) {
                //noinspection ResultOfMethodCallIgnored
                board.delete();
            }
        }

        return result;
    }

    private <T> void notifyOnUi(final Subscription<T> subscription, final T result) {
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                subscription.notifyResult(result);
            }
        });
    }

    private <T> void notifyErrorOnUi(final Subscription<T> subscription, final Throwable e) {
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                subscription.notifyError(e);
            }
        });
    }

    private String toFileName(DrawBoard board) {
        return String.format("%s_%s", board.getKey(), board.getName());
    }
}
