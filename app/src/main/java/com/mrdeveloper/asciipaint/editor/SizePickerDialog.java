package com.mrdeveloper.asciipaint.editor;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mrdeveloper.asciipaint.R;

/**
 * Created by MrDeveloper on 02-May-17.
 */

public class SizePickerDialog extends BottomSheetDialogFragment implements
        AppCompatSeekBar.OnSeekBarChangeListener {

    private static final String EXTRA_DEF_SIZE = "def_size";

    public static SizePickerDialog create(int defaultSize) {
        SizePickerDialog fragment = new SizePickerDialog();
        Bundle args = new Bundle();
        args.putInt(EXTRA_DEF_SIZE, defaultSize);
        fragment.setArguments(args);
        return fragment;
    }

    private TextView currentSizeLabel;
    private AppCompatSeekBar seekBar;

    private Listener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            listener = (Listener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_size_picker, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        currentSizeLabel = (TextView) view.findViewById(R.id.picker_size_indicator);
        seekBar = (AppCompatSeekBar) view.findViewById(R.id.picker_size_bar);
        if (savedInstanceState == null) {
            seekBar.setProgress(getArguments().getInt(EXTRA_DEF_SIZE) - 1);
        }
        seekBar.setOnSeekBarChangeListener(this);
        onProgressChanged(seekBar, seekBar.getProgress(), false);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null) {
            int size = seekBar.getProgress() + 1;
            listener.onSizePicked(size);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (listener instanceof Context) {
            listener = null;
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        currentSizeLabel.setText(String.valueOf(progress + 1));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public interface Listener {
        void onSizePicked(int size);
    }
}
