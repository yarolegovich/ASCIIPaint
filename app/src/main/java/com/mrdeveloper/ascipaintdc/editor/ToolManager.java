package com.mrdeveloper.ascipaintdc.editor;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mrdeveloper.ascipaintdc.R;
import com.mrdeveloper.ascipaintdc.draw.ASCIICanvas;
import com.mrdeveloper.ascipaintdc.draw.tool.BrushTool;
import com.mrdeveloper.ascipaintdc.draw.tool.EraserTool;
import com.mrdeveloper.ascipaintdc.draw.tool.EyedropperTool;
import com.mrdeveloper.ascipaintdc.draw.tool.PencilTool;
import com.mrdeveloper.ascipaintdc.draw.tool.Tool;

import java.util.Arrays;
import java.util.List;

/**
 * Created by yarolegovich on 01-May-17.
 */

public class ToolManager {

    private static final String EXTRA_COLOR = "tool_color";
    private static final String EXTRA_SIZE = "tool_size";
    private static final String EXTRA_SYMBOL = "tool_symbol";
    private static final String EXTRA_CURRENT = "tool_current";

    private static final char DEFAULT_SYMBOL = 'X';
    private static final int DEFAULT_SIZE = 1;
    private static final int DEFAULT_CURRENT = 0;
    private final int DEFAULT_COLOR;

    private int currentColor;
    private int currentSize;
    private char currentSymbol;

    private List<Tool> tools;
    private List<ToolInfo> toolInfo;
    private int current;

    private Listener listener;

    public ToolManager(Context context, ASCIICanvas canvas, Bundle savedInstanceState) {
        DEFAULT_COLOR = ContextCompat.getColor(context, R.color.colorAccent);
        initToolParameters(savedInstanceState);
        tools = Arrays.asList(
                setToolParams(new PencilTool(canvas)),
                setToolParams(new EraserTool(canvas)),
                setToolParams(new BrushTool(canvas)),
                setToolParams(new EyedropperTool(canvas)));
        toolInfo = Arrays.asList(
                new ToolInfo(R.string.tool_pencil, R.drawable.ic_mode_edit_black_24dp),
                new ToolInfo(R.string.tool_eraser, R.drawable.ic_eraser_black_24dp),
                new ToolInfo(R.string.tool_brush, R.drawable.ic_brush_black_24dp),
                new ToolInfo(R.string.tool_eyedropper, R.drawable.ic_eyedropper_black_24dp));
    }

    private void initToolParameters(Bundle savedState) {
        if (savedState == null) {
            currentColor = DEFAULT_COLOR;
            currentSize = DEFAULT_SIZE;
            currentSymbol = DEFAULT_SYMBOL;
            current = DEFAULT_CURRENT;
        } else {
            currentColor = savedState.getInt(EXTRA_COLOR, DEFAULT_COLOR);
            currentSize = savedState.getInt(EXTRA_SIZE, DEFAULT_SIZE);
            currentSymbol = savedState.getChar(EXTRA_SYMBOL, DEFAULT_SYMBOL);
            current = savedState.getInt(EXTRA_CURRENT, DEFAULT_CURRENT);
        }
    }

    private Tool setToolParams(Tool tool) {
        tool.setColor(currentColor);
        tool.setSize(currentSize);
        tool.setSymbol(currentSymbol);
        return tool;
    }

    public void updateTools(int color, char symbol, int size) {
        currentColor = color;
        currentSize = size;
        currentSymbol = symbol;
        for (Tool tool : tools) {
            setToolParams(tool);
        }
    }

    public void showAnchoredTo(View anchor) {
        PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
        Menu menu = popup.getMenu();
        for (int i = 0; i < toolInfo.size(); i++) {
            ToolInfo info = toolInfo.get(i);
            menu.add(0, i, i, info.name).setIcon(info.icon);
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                current = item.getItemId();
                if (listener != null) {
                    listener.onToolSelected(tools.get(current), toolInfo.get(current).icon);
                }
                return true;
            }
        });

        MenuPopupHelper menuHelper = new MenuPopupHelper(anchor.getContext(), (MenuBuilder) menu, anchor);
        menuHelper.setForceShowIcon(true);
        menuHelper.show();
    }

    public Tool getCurrentTool() {
        return tools.get(current);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public EyedropperTool getEyedropper() {
        return (EyedropperTool) tools.get(tools.size() - 1);
    }

    public void saveStateTo(Bundle outState) {
        outState.putInt(EXTRA_COLOR, currentColor);
        outState.putInt(EXTRA_SIZE, currentSize);
        outState.putChar(EXTRA_SYMBOL, currentSymbol);
        outState.putInt(EXTRA_CURRENT, current);
    }

    public int getSize() {
        return currentSize;
    }

    public int getColor() {
        return currentColor;
    }

    public int getSymbol() {
        return currentSymbol;
    }

    public ToolInfo getCurrentToolInfo() {
        return toolInfo.get(current);
    }

    public interface Listener {
        void onToolSelected(Tool tool, @DrawableRes int toolIcon);
    }

    public static class ToolInfo {
        private int name;
        private int icon;

        private ToolInfo(@StringRes int name, @DrawableRes int iconRes) {
            this.name = name;
            this.icon = iconRes;
        }

        @StringRes
        public int getName() {
            return name;
        }

        @DrawableRes
        public int getIcon() {
            return icon;
        }
    }
}
