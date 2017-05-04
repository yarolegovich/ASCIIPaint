package com.mrdeveloper.ascipaintdc.net;

import com.mrdeveloper.ascipaintdc.util.Logger;

/**
 * Created by yarolegovich on 04-May-17.
 */

public class Subscription<T> {

    private PublicBoardManager.Callback<T> callback;

    public Subscription(PublicBoardManager.Callback<T> callback) {
        this.callback = callback;
    }

    void notifyResult(T result) {
        if (callback != null) {
            callback.onResult(result);
        }
    }

    void notifyError(Throwable e) {
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
