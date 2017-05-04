package com.mrdeveloper.ascipaintdc.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.MenuItem;

/**
 * Created by yarolegovich on 03-May-17.
 */

public class TintableMenuItem {

    private MenuItem wrapped;

    private Drawable enabledIcon;
    private Drawable disabledIcon;

    public TintableMenuItem(Context context, MenuItem wrapped, int icon, int disabledColor) {
        this.wrapped = wrapped;
        this.enabledIcon = ContextCompat.getDrawable(context, icon);
        this.disabledIcon = ContextCompat.getDrawable(context, icon).mutate();
        DrawableCompat.setTint(disabledIcon, disabledColor);
    }

    public void setEnabled(boolean enabled) {
        wrapped.setEnabled(enabled);
        wrapped.setIcon(enabled ? enabledIcon : disabledIcon);
    }
}
