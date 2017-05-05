package com.mrdeveloper.asciipaint.data;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.mrdeveloper.asciipaint.draw.model.ASCIIImage;

import java.io.File;

/**
 * Created by MrDeveloper on 03-May-17.
 */

public class ExportToJsonService extends IntentService {

    private static final String EXTRA_IMAGE = "image";
    private static final String EXTRA_OUT_FILE = "directory";

    public static Intent callingIntent(Context context, File outFile, ASCIIImage image) {
        Intent intent = new Intent(context, ExportToJsonService.class);
        intent.putExtra(EXTRA_OUT_FILE, outFile);
        intent.putExtra(EXTRA_IMAGE, image);
        return intent;
    }

    public ExportToJsonService() {
        super(ExportToJsonService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ASCIIImage image = intent.getParcelableExtra(EXTRA_IMAGE);
        File outFile = (File) intent.getSerializableExtra(EXTRA_OUT_FILE);
        JSONSerializer serializer = new JSONSerializer();
        serializer.writeAsJsonToFile(image, outFile);
    }
}
