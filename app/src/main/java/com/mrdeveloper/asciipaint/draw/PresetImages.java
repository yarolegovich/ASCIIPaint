package com.mrdeveloper.asciipaint.draw;

import com.mrdeveloper.asciipaint.draw.model.ASCIIImage;

import java.util.Arrays;
import java.util.List;

/**
 * Created by MrDeveloper on 02-May-17.
 */

public class PresetImages {

    private List<ASCIIImage> images;

    public PresetImages() {
        images = Arrays.asList(
                new ASCIIImage("Hello", new char[][]{
                        "H H EEE L   L   OOO".toCharArray(),
                        "H H E   L   L   O O".toCharArray(),
                        "HHH EEE L   L   O O".toCharArray(),
                        "H H E   L   L   O O".toCharArray(),
                        "H H EEE LLL LLL OOO".toCharArray()
                }),
                new ASCIIImage("Lol", new char[][]{
                        "LL      OOOOOO  LL    ".toCharArray(),
                        "LL      OO  OO  LL    ".toCharArray(),
                        "LL      OO  OO  LL    ".toCharArray(),
                        "LL      OO  OO  LL    ".toCharArray(),
                        "LLLLLL  OOOOOO  LLLLLL".toCharArray()
                }),
                new ASCIIImage("Smile", new char[][]{
                        "             OOOOOOOOOOO             ".toCharArray(),
                        "         OOOOOOOOOOOOOOOOOOO         ".toCharArray(),
                        "      OOOOOO  OOOOOOOOO  OOOOOO      ".toCharArray(),
                        "    OOOOOO      OOOOO      OOOOOO    ".toCharArray(),
                        "  OOOOOOOO  #   OOOOO  #   OOOOOOOO  ".toCharArray(),
                        " OOOOOOOOOO    OOOOOOO    OOOOOOOOOO ".toCharArray(),
                        "OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO".toCharArray(),
                        "OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO".toCharArray(),
                        "OOOO  OOOOOOOOOOOOOOOOOOOOOOOOO  OOOO".toCharArray(),
                        " OOOO  OOOOOOOOOOOOOOOOOOOOOOO  OOOO ".toCharArray(),
                        "  OOOO   OOOOOOOOOOOOOOOOOOOO  OOOO  ".toCharArray(),
                        "    OOOOO   OOOOOOOOOOOOOOO   OOOO   ".toCharArray(),
                        "      OOOOOO   OOOOOOOOO   OOOOOO    ".toCharArray(),
                        "         OOOOOO         OOOOOO       ".toCharArray(),
                        "             OOOOOOOOOOOO            ".toCharArray()
                }), new ASCIIImage("Гарнi очi ;)", new char[][] {
                        "      o|||O      o|||o      ".toCharArray(),
                        "     oO   Oo    oO   O.     ".toCharArray(),
                        "    oO      O  oO      O.   ".toCharArray(),
                        "    oO  #   O  oO  #   O.   ".toCharArray(),
                        "     oO    O    O     O.    ".toCharArray(),
                        "       Oooo      OoooO      ".toCharArray(),
                })
        );
    }

    public List<ASCIIImage> getPresets() {
        return images;
    }

}
