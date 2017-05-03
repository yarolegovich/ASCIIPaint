package com.yarolegovich.ascipaintdc.data;

import android.support.annotation.Nullable;
import android.util.Log;

import com.yarolegovich.ascipaintdc.draw.ASCIIImage;
import com.yarolegovich.ascipaintdc.util.Logger;
import com.yarolegovich.ascipaintdc.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * Created by yarolegovich on 03-May-17.
 */

public class JSONSerializer {

    private static final String KEY_ROW_COUNT = "rows";
    private static final String KEY_COLUMN_COUNT = "columns";
    private static final String KEY_CONTENT = "symbols";
    private static final String KEY_COLORS = "colors";

    @Nullable
    public ASCIIImage fromJson(InputStream is) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return fromJson(sb);
        } catch (IOException e) {
            Logger.e(e);
            return null;
        } finally {
            Utils.closeSilently(br);
        }
    }

    @Nullable
    public ASCIIImage fromJson(CharSequence json) {
        try {
            JSONObject jObj = new JSONObject(json.toString());
            int rows = jObj.getInt(KEY_ROW_COUNT);

            JSONArray contentJson = jObj.getJSONArray(KEY_CONTENT);
            char[][] content = new char[rows][];
            for (int i = 0; i < rows; i++) {
                content[i] = contentJson.getString(i).toCharArray();
            }

            ASCIIImage.ColorRange[][] colors = new ASCIIImage.ColorRange[rows][];
            JSONArray colorsJson = jObj.getJSONArray(KEY_COLORS);
            for (int rowIndex = 0; rowIndex < colorsJson.length(); rowIndex++) {
                JSONArray colorRowJson = colorsJson.getJSONArray(rowIndex);
                ASCIIImage.ColorRange[] colorRow = new ASCIIImage.ColorRange[colorRowJson.length()];
                for (int rangeIndex = 0; rangeIndex < colorRowJson.length(); rangeIndex++) {
                    JSONArray colorRangeJson = colorRowJson.getJSONArray(rangeIndex);
                    colorRow[rangeIndex] = new ASCIIImage.ColorRange(
                            colorRangeJson.getInt(0),
                            colorRangeJson.getInt(1),
                            colorRangeJson.getInt(2));
                }
                colors[rowIndex] = colorRow;
            }

            ASCIIImage image = new ASCIIImage(content);
            image.setColors(colors);
            return image;
        } catch (Exception e) {
            Logger.e(e);
            return null;
        }
    }

    @Nullable
    public JSONObject toJson(ASCIIImage image) {
        try {
            JSONObject result = new JSONObject();
            result.put(KEY_ROW_COUNT, image.getRowCount());
            result.put(KEY_COLUMN_COUNT, image.getColumnCount());

            JSONArray rows = new JSONArray();
            for (int i = 0; i < image.getRowCount(); i++) {
                rows.put(image.getRow(i).toString());
            }
            result.put(KEY_CONTENT, rows);

            if (image.hasColors()) {
                JSONArray colorsJson = new JSONArray();
                ASCIIImage.ColorRange[][] colors = image.getColors();
                for (ASCIIImage.ColorRange[] imageRow : colors) {
                    JSONArray colorRowJson = new JSONArray();
                    for (ASCIIImage.ColorRange range : imageRow) {
                        colorRowJson.put(new JSONArray().put(range.getColor())
                                .put(range.getRangeStart())
                                .put(range.getRangeEnd()));
                    }
                    colorsJson.put(colorRowJson);
                }
                result.put(KEY_COLORS, colorsJson);
            }
            return result;
        } catch (JSONException e) {
            Logger.e(e);
            return null;
        }
    }

    public boolean writeAsJsonToFile(ASCIIImage image, File outFile) {
        JSONObject jObj = toJson(image);
        if (jObj == null) {
            return false;
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outFile));
            writer.write(jObj.toString());
            writer.flush();
            return true;
        } catch (IOException e) {
            Logger.e(e);
            return false;
        } finally {
            Utils.closeSilently(writer);
        }
    }
}
