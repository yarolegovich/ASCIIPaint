package com.mrdeveloper.asciipaint.data;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

import com.mrdeveloper.asciipaint.FilePickerActivity;
import com.mrdeveloper.asciipaint.draw.ASCIICanvas;
import com.mrdeveloper.asciipaint.draw.model.ASCIIImage;
import com.mrdeveloper.asciipaint.draw.model.DrawBoard;
import com.mrdeveloper.asciipaint.util.Logger;
import com.mrdeveloper.asciipaint.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.Manifest.permission.*;

/**
 * Created by MrDeveloper on 03-May-17.
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

    private DrawBoard temporary;

    private ImportCallback importCallback;
    private ExportCallback exportCallback;

    public ShareManager(Activity context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
        createAppFolderIfNotExists();
    }

    public void shareImage(ASCIICanvas canvas) {

    }

    public void exportToJson(final DrawBoard board) {
        if (isPermitted(WRITE_EXTERNAL_STORAGE)) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    ASCIICanvas canvas = board.getCanvas();
                    if (canvas == null) {
                        Logger.e("Canvas was null, aborting export");
                        return;
                    }
                    ASCIIImage image = canvas.toASCIIImage();
                    context.startService(ExportToJsonService.callingIntent(
                            context, generateNewFile(board),
                            image));
                }
            });
        } else {
            temporary = board;
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
                InputStream is = null;
                ContentResolver cr = context.getContentResolver();
                try {
                    JSONSerializer jsonSerializer = new JSONSerializer();
                    is = cr.openInputStream(data.getData());
                    ASCIIImage image = jsonSerializer.fromJson(is);
                    notifyImageImported(image);
                } catch (FileNotFoundException e) {
                    Logger.e(e);
                    notifyImportFailed();
                } finally {
                    Utils.closeSilently(is);
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
            //noinspection ResultOfMethodCallIgnored
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

    private File generateNewFile(DrawBoard board) {
        return new File(APP_FOLDER,
                String.format(Locale.US, "export_%s_%d.json", board.getName(),
                        System.currentTimeMillis()));
    }

    public interface ImportCallback {
        void onImageImported(ASCIIImage image);

        void onImageImportFailed();
    }

    public interface ExportCallback {
        void onExportResult(boolean success);
    }
}
