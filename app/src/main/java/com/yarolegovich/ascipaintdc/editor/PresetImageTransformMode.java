package com.yarolegovich.ascipaintdc.editor;

import android.app.Activity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.yarolegovich.ascipaintdc.R;
import com.yarolegovich.ascipaintdc.draw.tool.PresetImageTool;

/**
 * Created by yarolegovich on 02-May-17.
 */

public class PresetImageTransformMode {

    private Activity activity;
    private Listener listener;
    private ActionMode actionMode;
    private boolean notified;

    public PresetImageTransformMode(Activity activity) {
        this.activity = activity;
        if (activity instanceof Listener) {
            this.listener = (Listener) activity;
        }
    }

    private void notifyApplyTransform(PresetImageTool tool) {
        if (listener != null) {
            notified = true;
            listener.onApplyImageTransform(tool);
        }
    }

    private void notifyDismissTransform() {
        if (!notified) {
            if (listener != null) {
                listener.onDismissImageTransform();
            }
        }
    }

    public void startWith(final PresetImageTool tool) {
        notified = false;
        activity.startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                activity.getMenuInflater().inflate(R.menu.menu_image_transform, menu);
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
                Log.d("tag", "onDestroyActionMode");
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
        void onApplyImageTransform(PresetImageTool tool);

        void onDismissImageTransform();
    }
}
