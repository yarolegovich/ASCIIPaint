package com.mrdeveloper.asciipaint.util;

/**
 * Created by MrDeveloper on 05-May-17.
 */

public interface Callback<T> {
    void onResult(T result);

    void onError(Throwable e);
}
