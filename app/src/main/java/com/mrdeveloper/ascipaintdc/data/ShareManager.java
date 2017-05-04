package com.mrdeveloper.ascipaintdc.data;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

import com.mrdeveloper.ascipaintdc.FilePickerActivity;
import com.mrdeveloper.ascipaintdc.draw.ASCIICanvas;
import com.mrdeveloper.ascipaintdc.draw.model.ASCIIImage;
import com.mrdeveloper.ascipaintdc.util.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.Manifest.permission.*;

/**
 * Created by yarolegovich on 03-May-17.
 */

public class ShareManager {

    private static final File APP_FOLDER = new File(
            Environment.getExternalStorageDirectory(),
            "ASCIIPaint");

    private static final int REQUEST_IMPORT_JSON = 4292;

    private static final int REQUEST_IMPORT_PERMISSION = 4;
    private static final int REQUEST_EXPORT_PERMISSION = 5;

    private Executor executor;
    private Activity context;

    private ASCIICanvas temporary;

    private ImportCallback importCallback;
    private ExportCallback exportCallback;

    public ShareManager(Activity context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
        createAppFolderIfNotExists();
    }

    public void shareImage(ASCIICanvas canvas) {

    }

    public void exportToJson(final ASCIICanvas canvas) {
        if (isPermitted(WRITE_EXTERNAL_STORAGE)) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    ASCIIImage image = canvas.toASCIIImage();
                    context.startService(ExportToJsonService.callingIntent(
                            context, generateNewFile(),
                            image));
                }
            });
        } else {
            temporary = canvas;
            requestPermission(REQUEST_EXPORT_PERMISSION, WRITE_EXTERNAL_STORAGE);
        }
    }

    public void importFromJson() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivityForResult(
                    Intent.createChooser(intent, "Open"),
                    REQUEST_IMPORT_JSON);
        } else {
            if (isPermitted(READ_EXTERNAL_STORAGE)) {
                context.startActivityForResult(
                        FilePickerActivity.callingIntent(context, APP_FOLDER),
                        REQUEST_IMPORT_JSON);
            } else {
                requestPermission(REQUEST_IMPORT_PERMISSION, READ_EXTERNAL_STORAGE);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMPORT_JSON) {
            if (resultCode == Activity.RESULT_OK) {
                ContentResolver cr = context.getContentResolver();
                try {
                    JSONSerializer jsonSerializer = new JSONSerializer();
                    InputStream is = cr.openInputStream(data.getData());
                    ASCIIImage image = jsonSerializer.fromJson(is);
                    notifyImageImported(image);
                } catch (FileNotFoundException e) {
                    Logger.e(e);
                    notifyImportFailed();
                }
            } else {
                notifyImportFailed();
            }
        }
    }

    private void notifyImageImported(ASCIIImage image) {
        if (image == null) {
            notifyImportFailed();
        } else if (importCallback != null) {
            importCallback.onImageImported(image);
        }
    }

    private void notifyImportFailed() {
        if (importCallback != null) {
            importCallback.onImageImportFailed();
        }
    }

    private void notifyExportResult(boolean result) {
        if (exportCallback != null) {
            exportCallback.onExportResult(result);
        }
    }

    public void onRequestPermissionResult(int request, String[] permissions, int[] results) {
        if (request != REQUEST_IMPORT_PERMISSION && request != REQUEST_EXPORT_PERMISSION) {
            return;
        }
        if (results[0] == PackageManager.PERMISSION_GRANTED) {
            if (request == REQUEST_IMPORT_PERMISSION) {
                importFromJson();
            } else {
                createAppFolderIfNotExists();
                exportToJson(temporary);
                temporary = null;
            }
        } else {
            temporary = null;
            if (request == REQUEST_IMPORT_PERMISSION) {
                notifyImportFailed();
            } else {
                notifyExportResult(false);
            }
        }
    }

    public void onDestroy() {

    }

    private void createAppFolderIfNotExists() {
        if (isPermitted(WRITE_EXTERNAL_STORAGE) && !APP_FOLDER.exists()) {
            APP_FOLDER.mkdir();
        }
    }

    private boolean isPermitted(String permission) {
        return ActivityCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(int code, String permission) {
        ActivityCompat.requestPermissions(context, new String[]{permission}, code);
    }

    public void setImportCallback(ImportCallback callback) {
        this.importCallback = callback;
    }

    public void setExportCallback(ExportCallback exportCallback) {
        this.exportCallback = exportCallback;
    }

    private File generateNewFile() {
        return new File(APP_FOLDER, "export_" + String.valueOf(System.currentTimeMillis()) + ".json");
    }

    public interface ImportCallback {
        void onImageImported(ASCIIImage image);

        void onImageImportFailed();
    }

    public interface ExportCallback {
        void onExportResult(boolean success);
    }
}
