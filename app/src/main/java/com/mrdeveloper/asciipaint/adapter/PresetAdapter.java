package com.mrdeveloper.asciipaint.adapter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mrdeveloper.asciipaint.R;
import com.mrdeveloper.asciipaint.draw.model.ASCIIImage;

import java.util.List;

/**
 * Created by MrDeveloper on 02-May-17.
 */

public class PresetAdapter extends RecyclerView.Adapter<PresetAdapter.ViewHolder> {

    private List<ASCIIImage> images;

    private int activeColor;
    private int inactiveColor;

    private int activePosition;

    private Listener listener;

    public PresetAdapter(List<ASCIIImage> images) {
        this.images = images;
        this.activePosition = 0;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        activeColor = ContextCompat.getColor(recyclerView.getContext(), R.color.colorAccent);
        inactiveColor = ContextCompat.getColor(recyclerView.getContext(), R.color.secondaryText);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_preset_label, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.label.setText(images.get(position).getName());
        holder.label.setTextColor(position == activePosition ? activeColor : inactiveColor);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public ASCIIImage getSelectedImage() {
        return images.get(activePosition);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView label;

        public ViewHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.li_preset_label);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (activePosition == getAdapterPosition()) {
                return;
            }
            notifyItemChanged(activePosition);
            activePosition = getAdapterPosition();
            notifyItemChanged(activePosition);
            if (listener != null) {
                listener.onItemSelected(images.get(getAdapterPosition()));
            }
        }
    }

    public interface Listener {
        void onItemSelected(ASCIIImage image);
    }
}
