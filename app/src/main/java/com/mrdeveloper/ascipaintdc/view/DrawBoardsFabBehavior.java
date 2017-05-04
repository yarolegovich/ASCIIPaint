package com.mrdeveloper.ascipaintdc.view;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import com.mrdeveloper.ascipaintdc.R;

import java.util.List;

/**
 * Created by yarolegovich on 04-May-17.
 */

public class DrawBoardsFabBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {

    private int fabCenterRestY;
    private int fabHalfWidth;

    public DrawBoardsFabBehavior() {
    }

    public DrawBoardsFabBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        return dependency.getId() == R.id.bottom_sheet_add_board;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        offsetFabIfRequired(child, dependency);
        return true;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, FloatingActionButton child, int layoutDirection) {
        parent.onLayoutChild(child, layoutDirection);

        fabHalfWidth = child.getWidth() / 2;
        fabCenterRestY = child.getTop() + child.getWidth() / 2;

        List<View> dependencies = parent.getDependencies(child);
        if (!dependencies.isEmpty()) {
            offsetFabIfRequired(child, dependencies.get(0));
        }
        return true;
    }

    private void offsetFabIfRequired(FloatingActionButton fab, View dependency) {
        int centerDestination = Math.min(fabCenterRestY, dependency.getTop());
        int fabCenter = fab.getTop() + fabHalfWidth;
        int translation = centerDestination - fabCenter;
        ViewCompat.offsetTopAndBottom(fab, translation);
    }
}
