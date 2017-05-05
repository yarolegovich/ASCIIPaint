package com.mrdeveloper.asciipaint.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mrdeveloper.asciipaint.R;
import com.mrdeveloper.asciipaint.draw.model.DrawBoard;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MrDeveloper on 04-May-17.
 */

public class BoardsAdapter extends RecyclerView.Adapter<BoardsAdapter.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_BOARD = 1;

    private List<DrawBoard> publicBoards;
    private List<DrawBoard> localBoards;

    private Listener listener;

    public BoardsAdapter() {
        publicBoards = new ArrayList<>();
        localBoards = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        int res = viewType == VIEW_TYPE_HEADER ? R.layout.item_board_header : R.layout.item_board;
        View v = inflater.inflate(res, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (isHeader(position)) {
            holder.board = null;
            holder.label.setText(getHeaderTextFor(position));
        } else {
            holder.board = getBoardFor(position);
            holder.label.setText(holder.board.getName());
        }
    }

    private int getHeaderTextFor(int position) {
        if (position == 0 && !localBoards.isEmpty()) {
            return R.string.board_list_header_local;
        } else {
            return R.string.board_list_header_public;
        }
    }

    private DrawBoard getBoardFor(int position) {
        if (!localBoards.isEmpty()) {
            if (position - 1 < localBoards.size()) {
                return localBoards.get(position - 1);
            } else {
                return publicBoards.get(position - 2 - localBoards.size());
            }
        } else {
            return publicBoards.get(position - 1);
        }
    }


    private boolean isHeader(int position) {
        return position == 0 || (!localBoards.isEmpty() && position == localBoards.size() + 1);
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? VIEW_TYPE_HEADER : VIEW_TYPE_BOARD;
    }

    @Override
    public int getItemCount() {
        final int headerCount = Math.min(1, publicBoards.size()) + Math.min(1, localBoards.size());
        return headerCount
                + publicBoards.size()
                + localBoards.size();
    }

    public void addBoards(List<DrawBoard> newBoards) {
        for (DrawBoard board : newBoards) {
            if (board.isPublic()) {
                publicBoards.add(board);
                notifyItemInserted(getItemCount() - 1);
            } else {
                localBoards.add(board);
                notifyItemInserted(localBoards.size());
            }
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private DrawBoard board;
        private TextView label;

        public ViewHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.li_board_list_text);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (board != null && listener != null) {
                listener.onBoardClicked(board);
            }
        }
    }

    public interface Listener {
        void onBoardClicked(DrawBoard board);
    }
}
