package com.yarolegovich.ascipaintdc.draw;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by yarolegovich on 02-May-17.
 */
public class ASCIIImage implements Parcelable {

    private char[][] image;
    private ColorRange[][] colors;
    private StringBuilder buffer;
    private String name;

    public ASCIIImage(Parcel src) {
        buffer = new StringBuilder();
        name = src.readString();
        colors = new ColorRange[src.readInt()][];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = src.createTypedArray(ColorRange.CREATOR);
        }
        image = new char[src.readInt()][];
        for (int i = 0; i < image.length; i++) {
            image[i] = new char[src.readInt()];
            src.readCharArray(image[i]);
        }
    }

    public ASCIIImage(@NonNull char[][] image) {
        this("", image);
    }

    public ASCIIImage(String name, @NonNull char[][] image) {
        this.name = name;
        this.image = image;
        this.colors = new ColorRange[0][];
        this.buffer = new StringBuilder();
    }

    public boolean hasColors() {
        return colors.length > 0;
    }

    public ColorRange[][] getColors() {
        return colors;
    }

    public CharSequence getRow(int i) {
        return getRow(i, 0, image[i].length);
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

    public int getColumnCount() {
        return image.length == 0 ? 0 : image[0].length;
    }

    public String getName() {
        return name;
    }

    public void setColors(ColorRange[][] colors) {
        this.colors = colors;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(colors.length);
        for (ColorRange[] ranges : colors) {
            dest.writeTypedArray(ranges, flags);
        }
        dest.writeInt(image.length);
        for (char[] row : image) {
            dest.writeInt(row.length);
            dest.writeCharArray(row);
        }
    }

    public static final Creator<ASCIIImage> CREATOR = new Creator<ASCIIImage>() {
        @Override
        public ASCIIImage createFromParcel(Parcel in) {
            return new ASCIIImage(in);
        }

        @Override
        public ASCIIImage[] newArray(int size) {
            return new ASCIIImage[size];
        }
    };

    public static class ColorRange implements Parcelable {

        private int color;
        private int rangeStart;
        private int rangeEnd;

        protected ColorRange(Parcel in) {
            color = in.readInt();
            rangeStart = in.readInt();
            rangeEnd = in.readInt();
        }

        public ColorRange(int color, int rangeStart, int rangeEnd) {
            this.color = color;
            this.rangeStart = rangeStart;
            this.rangeEnd = rangeEnd;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(color);
            dest.writeInt(rangeStart);
            dest.writeInt(rangeEnd);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<ColorRange> CREATOR = new Creator<ColorRange>() {
            @Override
            public ColorRange createFromParcel(Parcel in) {
                return new ColorRange(in);
            }

            @Override
            public ColorRange[] newArray(int size) {
                return new ColorRange[size];
            }
        };

        public boolean isInRange(int index) {
            return index >= rangeStart && index <= rangeEnd;
        }

        public int getRangeStart() {
            return rangeStart;
        }

        public int getRangeEnd() {
            return rangeEnd;
        }

        public int getColor() {
            return color;
        }
    }
}
