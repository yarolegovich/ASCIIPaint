package com.mrdeveloper.asciipaint;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import com.mrdeveloper.asciipaint.data.ShareManager;
import com.mrdeveloper.asciipaint.draw.MemoizingCanvas;
import com.mrdeveloper.asciipaint.draw.model.DrawBoard;
import com.mrdeveloper.asciipaint.draw.tool.EyedropperTool;
import com.mrdeveloper.asciipaint.draw.model.ASCIIImage;
import com.mrdeveloper.asciipaint.draw.tool.PresetImageTool;
import com.mrdeveloper.asciipaint.draw.PresetImages;
import com.mrdeveloper.asciipaint.draw.tool.Tool;
import com.mrdeveloper.asciipaint.editor.BoardManager;
import com.mrdeveloper.asciipaint.editor.PresetImageDropMode;
import com.mrdeveloper.asciipaint.editor.PresetPicker;
import com.mrdeveloper.asciipaint.editor.SizePickerDialog;
import com.mrdeveloper.asciipaint.editor.SymbolPickerDialog;
import com.mrdeveloper.asciipaint.editor.ToolManager;
import com.mrdeveloper.asciipaint.view.ASCIIPaintView;
import com.mrdeveloper.asciipaint.view.ColorCircleView;
import com.mrdeveloper.asciipaint.view.TintableIconTextView;
import com.mrdeveloper.asciipaint.view.TintableMenuItem;

public class BoardActivity extends AppCompatActivity implements View.OnClickListener,
        EyedropperTool.Listener, ColorPickerDialogListener,
        SizePickerDialog.Listener, SymbolPickerDialog.Listener,
        ToolManager.Listener, PresetPicker.Listener, PresetImageDropMode.Listener,
        MemoizingCanvas.HistoryChangeListener, ShareManager.ImportCallback {

    private static final String EXTRA_BOARD = "board";

    public static Intent callingIntent(Context context, DrawBoard board) {
        Intent intent = new Intent(context, BoardActivity.class);
        intent.putExtra(EXTRA_BOARD, board);
        return intent;
    }

    private ColorCircleView currentColor;
    private TextView currentSymbol;
    private TintableIconTextView toolIcon;
    private ASCIIPaintView paintView;
    private TintableMenuItem undoBtn, redoBtn;
    private Toolbar toolbar;

    private ToolManager toolManager;
    private ShareManager shareManager;

    private PresetPicker presetImagePicker;
    private PresetImageDropMode presetTransformMode;

    private DrawBoard board;
    private BoardManager boardManager;
    private MemoizingCanvas canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        shareManager = new ShareManager(this);
        shareManager.setImportCallback(this);

        PresetImages images = new PresetImages();
        presetImagePicker = new PresetPicker(
                findViewById(R.id.bottom_presets),
                findViewById(R.id.overlay),
                images.getPresets());
        presetImagePicker.setListener(this);
        presetTransformMode = new PresetImageDropMode(this);

        board = (DrawBoard) getIntent().getSerializableExtra(EXTRA_BOARD);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(board.getName());

        paintView = (ASCIIPaintView) findViewById(R.id.paint_view);
        canvas = new MemoizingCanvas(paintView.getCanvas());
        board.setCanvas(paintView.getCanvas());
        paintView.setCanvas(canvas);
        canvas.setListener(this);

        boardManager = new BoardManager(this, board, savedInstanceState);
        paintView.setBufferStateListener(boardManager);

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
            case R.id.option_import:
                shareManager.importFromJson();
                break;
            case R.id.option_export:
                shareManager.exportToJson(board);
                break;
            case R.id.option_share:
                shareManager.shareImage(canvas);
                break;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        toolManager.saveStateTo(outState);
        boardManager.saveStateTo(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        shareManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        shareManager.onDestroy();
        boardManager.exitTheBoard();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        shareManager.onRequestPermissionResult(requestCode, permissions, grantResults);
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
        Tool currentTool = paintView.getCurrentTool();
        if (currentTool instanceof PresetImageTool) {
            currentTool.setColor(color);
            ((PresetImageTool) currentTool).drawToBufferAtLastLocation();
            canvas.drawToScreen();
        }
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
    public void onImagePicked(ASCIIImage image) {
        canvas.setTemporaryBuffer(true);
        PresetImageTool imagePlacementTool = new PresetImageTool(canvas, image);
        imagePlacementTool.setColor(toolManager.getColor());
        paintView.setCurrentTool(imagePlacementTool);

        executeCanvasDraw();

        if (presetTransformMode.isActive()) {
            presetTransformMode.finish();
        }

        presetTransformMode.startWith(imagePlacementTool);
    }

    private void drawTheImage(ASCIIImage image) {
        PresetImageTool placementTool = new PresetImageTool(canvas, image);
        paintView.setCurrentTool(placementTool);
        executeCanvasDraw();
        paintView.setCurrentTool(toolManager.getCurrentTool());
    }


    @Override
    public void onImageImported(ASCIIImage image) {
        drawTheImage(image);
    }

    @Override
    public void onApplyImageDrop(PresetImageTool tool) {
        onExitPresetDropMode(tool);
    }

    @Override
    public void onDismissImageDrop() {
        onExitPresetDropMode(null);
    }

    @Override
    public void onDrawHistoryChanged() {
        updateUndoRedoBtnsState();
    }

    @Override
    public void onDrawAction(int index, char symbol, int color) {
        boardManager.onDrawAction(index, symbol, color);
    }

    private void executeCanvasDraw() {
        Tool currentTool = paintView.getCurrentTool();
        canvas.onDrawGestureStart();
        currentTool.onDrawStart(paintView.getWidth() * 0.5f, paintView.getHeight() * 0.5f);
        canvas.drawToScreen();
        canvas.onDrawGestureEnd();
    }

    private void updateUndoRedoBtnsState() {
        undoBtn.setEnabled(canvas.canUndo());
        redoBtn.setEnabled(canvas.canRedo());
        toolbar.invalidate();
    }

    private void onExitPresetDropMode(PresetImageTool configuredPreset) {
        canvas.setTemporaryBuffer(false);
        if (configuredPreset != null) {
            canvas.onDrawGestureStart();
            configuredPreset.drawToBufferAtLastLocation();
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
    public void onImageImportFailed() {
        Snackbar.make(toolbar, R.string.board_msg_import_failed, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }
}
