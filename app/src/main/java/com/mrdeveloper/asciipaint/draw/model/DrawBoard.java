package com.mrdeveloper.asciipaint.draw.model;

import android.support.annotation.Nullable;

import com.mrdeveloper.asciipaint.draw.ASCIICanvas;

import java.io.Serializable;

/**
 * Created by MrDeveloper on 04-May-17.
 */

public class DrawBoard implements Serializable {

    private String key;
    private String boardName;
    private boolean isPublic;
    private int participants;

    private transient ASCIICanvas canvas;

    public DrawBoard(String key, String boardName, boolean isPublic) {
        this.key = key;
        this.boardName = boardName;
        this.isPublic = isPublic;
    }

    public String getName() {
        return boardName;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public int getParticipants() {
        return participants;
    }

    public void setParticipants(int participants) {
        this.participants = participants;
    }

    public String getKey() {
        return key;
    }

    @Nullable
    public ASCIICanvas getCanvas() {
        return canvas;
    }

    public void setCanvas(ASCIICanvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public String toString() {
        return "DrawBoard{" +
                "key='" + key + '\'' +
                ", boardName='" + boardName + '\'' +
                ", isPublic=" + isPublic +
                '}';
    }
}
