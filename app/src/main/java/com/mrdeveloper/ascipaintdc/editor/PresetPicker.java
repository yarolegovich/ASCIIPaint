package com.mrdeveloper.ascipaintdc.editor;

import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mrdeveloper.ascipaintdc.R;
import com.mrdeveloper.ascipaintdc.adapter.PresetAdapter;
import com.mrdeveloper.ascipaintdc.draw.model.ASCIIImage;
import com.mrdeveloper.ascipaintdc.util.Utils;
import com.mrdeveloper.ascipaintdc.view.ASCIIPreviewView;
import com.mrdeveloper.ascipaintdc.view.OverlayView;

import java.util.List;

/**
 * Created by yarolegovich on 02-May-17.
 */

public class PresetPicker {

    private static final int DURATION_OVERLAY_ANIM = 200;

    private BottomSheetBehavior bottomSheet;
    private ASCIIPreviewView previewView;
    private PresetAdapter choiceAdapter;

    private OverlayView overlay;

    private Listener listener;

    public PresetPicker(View bsView, View overlayView, List<ASCIIImage> images) {
        ClickHandler clickHandler = new ClickHandler();

        bottomSheet = BottomSheetBehavior.from(bsView);
        bottomSheet.setPeekHeight(0);
        Utils.disableBottomSheetDrag(bottomSheet);

        overlay = (OverlayView) overlayView;
        overlayView.setOnClickListener(clickHandler);

        RecyclerView predefinedPicker = (RecyclerView) bsView.findViewById(R.id.bs_predefined_picker);
        choiceAdapter = new PresetAdapter(images);
        choiceAdapter.setListener(clickHandler);
        predefinedPicker.setLayoutManager(new LinearLayoutManager(
                bsView.getContext(), LinearLayoutManager.HORIZONTAL,
                false));
        predefinedPicker.setAdapter(choiceAdapter);

        previewView = (ASCIIPreviewView) bsView.findViewById(R.id.bs_predefined_preview);
        previewView.preview(choiceAdapter.getSelectedImage());
        previewView.setOnClickListener(clickHandler);

        bsView.findViewById(R.id.bs_btn_ok).setOnClickListener(clickHandler);
    }

    public boolean isShown() {
        return bottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }

    public void hide() {
        overlay.hide();
        bottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void show() {
        overlay.show();
        bottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void onConfirmSelection() {
        if (listener != null) {
            listener.onImagePicked(choiceAdapter.getSelectedImage());
        }
        hide();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private class ClickHandler implements View.OnClickListener, PresetAdapter.Listener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.overlay:
                    hide();
                    break;
                case R.id.bs_predefined_preview:
                    onConfirmSelection();
                case R.id.bs_btn_ok:
                    onConfirmSelection();
                    break;
            }
        }

        @Override
        public void onItemSelected(ASCIIImage image) {
            previewView.preview(image);
        }
    }

    public interface Listener {
        void onImagePicked(ASCIIImage image);
    }
}
