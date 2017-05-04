package com.mrdeveloper.ascipaintdc.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mrdeveloper.ascipaintdc.R;

/**
 * Created by yarolegovich on 01-May-17.
 */

public class SymbolAdapter extends RecyclerView.Adapter<SymbolAdapter.ViewHolder> {

    private char[] symbols = ("@!~#$%^&*()_+:\"{}[];'" +
            "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjkl" +
            "1234567890").toCharArray();

    private Listener symbolClickListener;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_symbol, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.symbol.setText(String.valueOf(symbols[position]));
    }

    @Override
    public int getItemCount() {
        return symbols.length;
    }

    public void setSymbolClickListener(Listener symbolClickListener) {
        this.symbolClickListener = symbolClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView symbol;

        public ViewHolder(View itemView) {
            super(itemView);
            symbol = (TextView) itemView.findViewById(R.id.li_symbol);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (symbolClickListener != null) {
                symbolClickListener.onSymbolSelected(symbols[getAdapterPosition()]);
            }
        }
    }

    public interface Listener {
        void onSymbolSelected(char symbol);
    }
}
