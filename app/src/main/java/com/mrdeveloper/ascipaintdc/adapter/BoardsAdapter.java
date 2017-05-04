package com.mrdeveloper.ascipaintdc.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mrdeveloper.ascipaintdc.R;
import com.mrdeveloper.ascipaintdc.draw.model.DrawBoard;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yarolegovich on 04-May-17.
 */

public class BoardsAdapter extends RecyclerView.Adapter<BoardsAdapter.ViewHolder> {

    private List<DrawBoard> boards;

    private Listener listener;

    public BoardsAdapter() {
        boards = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_board, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DrawBoard board = boards.get(position);
        holder.boardName.setText(board.getName());
    }

    @Override
    public int getItemCount() {
        return boards.size();
    }

    public void addBoards(List<DrawBoard> newBoards) {
        boards.addAll(newBoards);
        notifyItemRangeInserted(boards.size() - newBoards.size(), newBoards.size());
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView boardName;

        public ViewHolder(View itemView) {
            super(itemView);
            boardName = (TextView) itemView.findViewById(R.id.li_board_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onBoardClicked(boards.get(getAdapterPosition()));
            }
        }
    }

    public interface Listener {
        void onBoardClicked(DrawBoard board);
    }
}
