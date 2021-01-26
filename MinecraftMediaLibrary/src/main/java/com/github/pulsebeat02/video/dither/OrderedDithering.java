package com.github.pulsebeat02.video.dither;

import org.jetbrains.annotations.NotNull;

public class OrderedDithering {

    private static final int[] PALETTE;
    private static final byte[] COLOR_MAP;
    private static final int[] FULL_COLOR_MAP;

    private final static int[][] bayerMatrixTwo;
    private final static int[][] bayerMatrixFour;
    private final static int[][] bayerMatrixEight;

    static {

        PALETTE = StaticDitherTools.PALETTE;
        COLOR_MAP = StaticDitherTools.COLOR_MAP;
        FULL_COLOR_MAP = new int[128 * 128 * 128];

        /*

        2 by 2 Bayer Ordered Dithering

        0   2
        3   1  (1/4)

        */

        bayerMatrixTwo = new int[][]{
                {1, 3},
                {4, 2},
        };

        /*

        4 by 4 Bayer Ordered Dithering

        1  9  3  11
        13 5  15  7
        4  12  2  10
        16 8  14  6   (1/16)

         */

        bayerMatrixFour = new int[][]{
                {1, 9, 3, 11},
                {13, 5, 15, 7},
                {4, 12, 2, 10},
                {16, 8, 14, 6}
        };

        /*

        8 by 8 Bayer Ordered Dithering

        1  49  13  61  4  52  16  64
        33 17  45  29  36  20  48  32
        9  57  5  53  12  60  8  56
        41  25  37  21  44  28  40  24
        3  51  15  63  2  50  14  62
        35  19  47  31  34  18  46  30
        11  59  7  55  10  58  6  54
        43  27  39  23  42  26  38  22   (1/64)

         */

        bayerMatrixEight = new int[][]{
                {1, 49, 13, 61, 4, 52, 16, 64},
                {33, 17, 45, 29, 36, 20, 48, 32},
                {9, 57, 5, 53, 12, 60, 8, 56},
                {41, 25, 37, 21, 44, 28, 40, 24},
                {3, 51, 15, 63, 2, 50, 14, 62},
                {35, 19, 47, 31, 34, 18, 46, 30},
                {11, 59, 7, 55, 10, 58, 6, 54},
                {43, 27, 39, 23, 42, 26, 38, 22}
        };

    }

    private int[][] matrix;

    private enum DitherType {
        ModeTwo, ModeFour, ModeEight
    }

    public OrderedDithering(@NotNull final DitherType type) {
        switch (type) {
            case ModeTwo:
                matrix = bayerMatrixTwo;
                break;
            case ModeFour:
                matrix = bayerMatrixFour;
                break;
            case ModeEight:
                matrix = bayerMatrixEight;
                break;
        }
    }

    public void dither(int[] buffer, int width, int side) {
        int height = buffer.length / width;
        for (int y = 0; y < height; y++) {
            int yIndex = y * width;
            for (int x = 0; x < width; x++) {
                int index = yIndex + x;
                buffer[index] = performNearestColor(x, y, buffer[index]);
            }
        }
    }

    public int performNearestColor(int x, int y, int color) {
        int n = matrix.length;
        return (int) (color + (matrix[x % n][y % n] - 0.5) / 2);
    }

}
