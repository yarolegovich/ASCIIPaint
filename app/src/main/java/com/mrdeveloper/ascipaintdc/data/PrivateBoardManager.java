package com.mrdeveloper.ascipaintdc.data;

import com.mrdeveloper.ascipaintdc.draw.model.DrawBoard;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yarolegovich on 04-May-17.
 */

public class PrivateBoardManager {

    public DrawBoard createBoard(String name) {
        return new DrawBoard(
                String.valueOf(System.currentTimeMillis()),
                name, false);
    }

    public List<DrawBoard> getBoards() {
        return new ArrayList<>();
    }
}
