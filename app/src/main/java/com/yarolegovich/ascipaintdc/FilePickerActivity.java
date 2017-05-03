package com.yarolegovich.ascipaintdc;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;

import java.io.File;

/**
 * Created by yarolegovich on 03-May-17.
 */

public class FilePickerActivity extends AppCompatActivity {

    private static final String EXTRA_DIRECTORY = "directory";

    public static Intent callingIntent(Context context, File directory) {
        Intent intent = new Intent(context, FilePickerActivity.class);
        intent.putExtra(EXTRA_DIRECTORY, directory);
        return intent;
    }


}
