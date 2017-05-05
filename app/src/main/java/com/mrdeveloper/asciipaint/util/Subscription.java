package com.mrdeveloper.asciipaint.util;

import com.mrdeveloper.asciipaint.net.PublicBoardManager;
import com.mrdeveloper.asciipaint.util.Logger;

/**
 * Created by MrDeveloper on 04-May-17.
 */

public class Subscription<T> {

    private Callback<T> callback;

    public Subscription(Callback<T> callback) {
        this.callback = callback;
    }

    public void notifyResult(T result) {
        if (callback != null) {
            callback.onResult(result);
        }
    }

    public void notifyError(Throwable e) {
        Logger.e(e);
        if (callback != null) {
            callback.onError(e);
        }
    }

    public boolean isCancelled() {
        return callback == null;
    }

    public void cancel() {
        callback = null;
    }
}
