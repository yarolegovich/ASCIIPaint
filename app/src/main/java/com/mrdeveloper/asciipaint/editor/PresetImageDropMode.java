package com.mrdeveloper.asciipaint.editor;

import android.app.Activity;
import android.graphics.Color;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.mrdeveloper.asciipaint.R;
import com.mrdeveloper.asciipaint.draw.tool.PresetImageTool;
import com.mrdeveloper.asciipaint.util.Utils;

/**
 * Created by MrDeveloper on 02-May-17.
 */

public class PresetImageDropMode {

    private Activity activity;

    private ActionMode actionMode;
    private boolean notified;

    private Listener listener;

    private int statusBarColor;

    public PresetImageDropMode(Activity activity) {
        this.activity = activity;
        this.statusBarColor = Utils.getStatusBarColor(activity);

        if (activity instanceof Listener) {
            this.listener = (Listener) activity;
        }
    }

    private void notifyApplyTransform(PresetImageTool tool) {
        if (listener != null) {
            notified = true;
            listener.onApplyImageDrop(tool);
        }
    }

    private void notifyDismissTransform() {
        if (!notified) {
            if (listener != null) {
                listener.onDismissImageDrop();
            }
        }
    }

    public void startWith(final PresetImageTool tool) {
        notified = false;
        Utils.setStatusBarColor(activity, Color.BLACK);
        activity.startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                activity.getMenuInflater().inflate(R.menu.menu_image_drop, menu);
                actionMode = mode;
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.mi_apply_image_transform:
                        notifyApplyTransform(tool);
                        mode.finish();
                        break;
                    case R.id.mi_cancel_image_transform:
                        notifyDismissTransform();
                        mode.finish();
                        break;
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                Utils.setStatusBarColor(activity, statusBarColor);
                notifyDismissTransform();
                actionMode = null;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
        });
    }

    public void finish() {
        if (actionMode != null) {
            notified = true;
            actionMode.finish();
        }
    }

    public boolean isActive() {
        return actionMode != null;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onApplyImageDrop(PresetImageTool tool);

        void onDismissImageDrop();
    }
}
