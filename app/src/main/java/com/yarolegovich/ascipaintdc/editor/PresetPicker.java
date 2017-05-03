package com.yarolegovich.ascipaintdc.editor;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yarolegovich.ascipaintdc.R;
import com.yarolegovich.ascipaintdc.adapter.PresetAdapter;
import com.yarolegovich.ascipaintdc.draw.ASCIIImage;
import com.yarolegovich.ascipaintdc.view.ASCIIPreviewView;

import java.util.List;

/**
 * Created by yarolegovich on 02-May-17.
 */

public class PresetPicker {

    private static final int DURATION_OVERLAY_ANIM = 200;

    private BottomSheetBehavior bottomSheet;
    private ASCIIPreviewView previewView;
    private PresetAdapter choiceAdapter;

    private View overlay;

    private Listener listener;

    public PresetPicker(View bsView, View overlayView, List<ASCIIImage> images) {
        ClickHandler clickHandler = new ClickHandler();

        bottomSheet = BottomSheetBehavior.from(bsView);
        bottomSheet.setPeekHeight(0);
        disableDragging(bottomSheet);

        overlay = overlayView;
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
        overlay.animate().cancel();
        overlay.setAlpha(1f);
        overlay.animate().alpha(0)
                .setDuration(DURATION_OVERLAY_ANIM)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        overlay.setVisibility(View.GONE);
                    }
                })
                .start();
        bottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void show() {
        overlay.setVisibility(View.VISIBLE);
        overlay.animate().cancel();
        overlay.setAlpha(0f);
        overlay.animate().alpha(1f)
                .setDuration(DURATION_OVERLAY_ANIM)
                .start();
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
                case R.id.bs_overlay:
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

    private void disableDragging(final BottomSheetBehavior bottomSheet) {
        bottomSheet.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bs, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    public interface Listener {
        void onImagePicked(ASCIIImage image);
    }
}
