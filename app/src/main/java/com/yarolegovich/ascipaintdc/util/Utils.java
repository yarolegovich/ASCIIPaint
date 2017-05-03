package com.yarolegovich.ascipaintdc.util;

import android.support.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by yarolegovich on 03-May-17.
 */

public class Utils {

    public static void closeSilently(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) { /* NOP */ }
        }
    }
}
