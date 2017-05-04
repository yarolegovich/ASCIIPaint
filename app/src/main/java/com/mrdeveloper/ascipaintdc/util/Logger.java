package com.mrdeveloper.ascipaintdc.util;

import android.util.Log;

import java.util.Locale;

/**
 * Created by yarolegovich on 03-May-17.
 */

public class Logger {

    private static final boolean LOG_ON = true;

    private static final String LOG_TAG = "ASCIIPaint";

    public static void d(String format, Object ...args) {
        if (LOG_ON) {
            Log.d(LOG_TAG, String.format(Locale.US, format, args));
        }
    }

    public static void e(Throwable e) {
        if (LOG_ON) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }
}
