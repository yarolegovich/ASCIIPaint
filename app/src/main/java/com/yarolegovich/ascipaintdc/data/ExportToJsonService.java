package com.yarolegovich.ascipaintdc.data;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.yarolegovich.ascipaintdc.draw.ASCIIImage;
import com.yarolegovich.ascipaintdc.util.Logger;

import java.io.File;

/**
 * Created by yarolegovich on 03-May-17.
 */

public class ExportToJsonService extends IntentService {

    private static final String EXTRA_IMAGE = "image";
    private static final String EXTRA_DIRECTORY = "directory";

    public static Intent callingIntent(Context context, File directory, ASCIIImage image) {
        Intent intent = new Intent(context, ExportToJsonService.class);
        intent.putExtra(EXTRA_DIRECTORY, directory);
        intent.putExtra(EXTRA_IMAGE, image);
        return intent;
    }

    public ExportToJsonService() {
        super(ExportToJsonService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ASCIIImage image = intent.getParcelableExtra(EXTRA_IMAGE);
        File directory = (File) intent.getSerializableExtra(EXTRA_DIRECTORY);
        JSONSerializer serializer = new JSONSerializer();
        serializer.writeAsJsonToFile(image, directory);
    }
}
