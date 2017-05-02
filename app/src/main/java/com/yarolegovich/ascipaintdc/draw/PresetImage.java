package com.yarolegovich.ascipaintdc.draw;

/**
 * Created by yarolegovich on 02-May-17.
 */

public class PresetImage {

    private char[][] image;
    private StringBuilder buffer;
    private String name;

    public PresetImage(String name, char[][] image) {
        this.name = name;
        this.image = image;
        this.buffer = new StringBuilder();
    }

    public CharSequence getRow(int i, int start, int length) {
        buffer.delete(0, buffer.length());
        buffer.insert(0, image[i], start, length);
        return buffer;
    }

    public char[] getRawRow(int i) {
        return image[i];
    }

    public int getRowCount() {
        return image.length;
    }

    public String getName() {
        return name;
    }
}
