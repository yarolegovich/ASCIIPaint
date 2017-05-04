package com.mrdeveloper.ascipaintdc.editor;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mrdeveloper.ascipaintdc.R;
import com.mrdeveloper.ascipaintdc.adapter.SymbolAdapter;

/**
 * Created by yarolegovich on 01-May-17.
 */

public class SymbolPickerDialog extends BottomSheetDialogFragment {

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
        return inflater.inflate(R.layout.dialog_symbol_picker, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView list = (RecyclerView) view.findViewById(R.id.picker_list);
        list.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL,
                false));
        SymbolAdapter adapter = new SymbolAdapter();
        adapter.setSymbolClickListener(new SymbolAdapter.Listener() {
            @Override
            public void onSymbolSelected(char symbol) {
                if (listener != null) {
                    listener.onSymbolSelected(symbol);
                }
                dismiss();
            }
        });
        list.setAdapter(adapter);
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

    public interface Listener {
        void onSymbolSelected(char symbol);
    }
}
