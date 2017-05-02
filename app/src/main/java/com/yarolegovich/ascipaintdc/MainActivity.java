package com.yarolegovich.ascipaintdc;

import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;
import com.yarolegovich.ascipaintdc.draw.ASCIICanvas;
import com.yarolegovich.ascipaintdc.draw.MemoizingCanvas;
import com.yarolegovich.ascipaintdc.draw.tool.EyedropperTool;
import com.yarolegovich.ascipaintdc.draw.PresetImage;
import com.yarolegovich.ascipaintdc.draw.tool.PresetImageTool;
import com.yarolegovich.ascipaintdc.draw.PresetImages;
import com.yarolegovich.ascipaintdc.draw.tool.Tool;
import com.yarolegovich.ascipaintdc.editor.PresetImageTransformMode;
import com.yarolegovich.ascipaintdc.editor.PresetPicker;
import com.yarolegovich.ascipaintdc.editor.SizePickerDialog;
import com.yarolegovich.ascipaintdc.editor.SymbolPickerDialog;
import com.yarolegovich.ascipaintdc.editor.ToolManager;
import com.yarolegovich.ascipaintdc.view.ASCIIPaintView;
import com.yarolegovich.ascipaintdc.view.ColorCircleView;
import com.yarolegovich.ascipaintdc.view.TintableIconTextView;
import com.yarolegovich.ascipaintdc.view.TintableMenuItem;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        EyedropperTool.Listener, ColorPickerDialogListener,
        SizePickerDialog.Listener, SymbolPickerDialog.Listener,
        ToolManager.Listener, PresetPicker.Listener,
        PresetImageTransformMode.Listener, MemoizingCanvas.Listener {

    private ColorCircleView currentColor;
    private TextView currentSymbol;
    private TintableIconTextView toolIcon;
    private ASCIIPaintView paintView;
    private TintableMenuItem undoBtn, redoBtn;
    private Toolbar toolbar;

    private ToolManager toolManager;

    private PresetPicker presetImagePicker;
    private PresetImageTransformMode presetTransformMode;

    private MemoizingCanvas canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PresetImages images = new PresetImages();
        presetImagePicker = new PresetPicker(
                findViewById(R.id.bottom_sheet_view),
                findViewById(R.id.bs_overlay),
                images.getPredefined());
        presetImagePicker.setListener(this);
        presetTransformMode = new PresetImageTransformMode(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        paintView = (ASCIIPaintView) findViewById(R.id.paint_view);
        canvas = new MemoizingCanvas(paintView.getCanvas());
        paintView.setCanvas(canvas);
        canvas.setListener(this);

        currentColor = (ColorCircleView) findViewById(R.id.color_circle);
        currentSymbol = (TextView) findViewById(R.id.symbol_view);
        toolIcon = (TintableIconTextView) findViewById(R.id.option_tool);

        toolManager = new ToolManager(this, canvas, savedInstanceState);
        toolManager.getEyedropper().setListener(this);
        toolManager.setListener(this);
        toolIcon.setTopDrawable(toolManager.getCurrentToolInfo().getIcon());
        paintView.setCurrentTool(toolManager.getCurrentTool());

        toolIcon.setOnClickListener(this);
        findViewById(R.id.options_symbol).setOnClickListener(this);
        findViewById(R.id.option_presets).setOnClickListener(this);
        findViewById(R.id.option_color).setOnClickListener(this);
        findViewById(R.id.option_size).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        undoBtn = createMenuItem(R.id.option_undo, R.drawable.ic_undo_white_24dp);
        redoBtn = createMenuItem(R.id.option_redo, R.drawable.ic_redo_white_24dp);
        updateUndoRedoBtnsState();
        return true;
    }

    private TintableMenuItem createMenuItem(@IdRes int id, @DrawableRes int icon) {
        int inactiveColor = ContextCompat.getColor(this, R.color.disabledMenuIcon);
        return new TintableMenuItem(this, toolbar.getMenu().findItem(id), icon, inactiveColor);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_undo:
                canvas.undo();
                break;
            case R.id.option_redo:
                canvas.redo();
                break;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        toolManager.saveStateTo(outState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.option_tool:
                toolManager.showAnchoredTo(v);
                break;
            case R.id.options_symbol:
                showSymbolSelection();
                break;
            case R.id.option_presets:
                presetImagePicker.show();
                break;
            case R.id.option_color:
                showColorSelection();
                break;
            case R.id.option_size:
                showSizeSelection();
                break;
        }
    }

    private void showSymbolSelection() {
        SymbolPickerDialog dialog = new SymbolPickerDialog();
        dialog.show(getSupportFragmentManager(), SymbolPickerDialog.class.getSimpleName());
    }

    private void showColorSelection() {
        ColorPickerDialog.newBuilder()
                .setColor(currentColor.getColor())
                .setShowAlphaSlider(false)
                .create()
                .show(getFragmentManager(), ColorPickerDialog.class.getSimpleName());
    }

    private void showSizeSelection() {
        SizePickerDialog dialog = SizePickerDialog.create(toolManager.getSize());
        dialog.show(getSupportFragmentManager(), SizePickerDialog.class.getSimpleName());
    }

    @Override
    public void onToolSelected(Tool tool, @DrawableRes int icon) {
        if (!presetTransformMode.isActive()) {
            paintView.setCurrentTool(tool);
        }
        toolIcon.setTopDrawable(icon);
    }

    @Override
    public void onColorPicked(int color) {
        currentColor.setColor(color);
        updateTools();
    }

    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
        onColorPicked(color);
    }

    @Override
    public void onSizePicked(int size) {
        updateTools(size);
    }

    @Override
    public void onSymbolSelected(char symbol) {
        currentSymbol.setText(String.valueOf(symbol));
        updateTools();
    }

    @Override
    public void onImagePicked(PresetImage image) {
        canvas.setTemporaryBuffer(true);
        PresetImageTool imagePlacementTool = new PresetImageTool(canvas, image);
        imagePlacementTool.setColor(toolManager.getColor());
        paintView.setCurrentTool(imagePlacementTool);

        canvas.onDrawGestureStart();
        imagePlacementTool.onDrawStart(paintView.getWidth() * 0.5f, paintView.getHeight() * 0.5f);
        canvas.drawToScreen();
        canvas.onDrawGestureEnd();

        if (presetTransformMode.isActive()) {
            presetTransformMode.finish();
        }

        presetTransformMode.startWith(imagePlacementTool);
    }

    @Override
    public void onApplyImageTransform(PresetImageTool tool) {
        onExitPresetPlacementMode(tool);
    }

    @Override
    public void onDismissImageTransform() {
        onExitPresetPlacementMode(null);
    }

    @Override
    public void onDrawHistoryChanged() {
        updateUndoRedoBtnsState();
    }

    private void updateUndoRedoBtnsState() {
        undoBtn.setEnabled(canvas.canUndo());
        redoBtn.setEnabled(canvas.canRedo());
        toolbar.invalidate();
    }

    private void onExitPresetPlacementMode(PresetImageTool configuredPreset) {
        canvas.setTemporaryBuffer(false);
        if (configuredPreset != null) {
            canvas.onDrawGestureStart();
            configuredPreset.drawAtLastLocation();
            canvas.onDrawGestureEnd();
        }
        canvas.drawToScreen();
        Tool tool = toolManager.getCurrentTool();
        paintView.setCurrentTool(tool);
    }

    private void updateTools() {
        updateTools(toolManager.getSize());
    }

    private void updateTools(int size) {
        toolManager.updateTools(
                currentColor.getColor(),
                currentSymbol.getText().charAt(0),
                size);
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }
}
