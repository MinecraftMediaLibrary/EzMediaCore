package com.github.pulsebeat02.video.dither;

import org.jetbrains.annotations.NotNull;

public class OrderedDithering {

    private static final int[] PALETTE;
    private static final byte[] COLOR_MAP;
    private static final int[] FULL_COLOR_MAP;

    private final static float[][] bayerMatrixTwo;
    private final static float[][] bayerMatrixFour;
    private final static float[][] bayerMatrixEight;

    static {

        PALETTE = StaticDitherInitialization.PALETTE;
        COLOR_MAP = StaticDitherInitialization.COLOR_MAP;
        FULL_COLOR_MAP = new int[128 * 128 * 128];

        /*

        2 by 2 Bayer Ordered Dithering

        0   2
        3   1  (1/4)

        */

        bayerMatrixTwo = new float[][]{
                {1f, 3f},
                {4f, 2f},
        };

        /*

        4 by 4 Bayer Ordered Dithering

        1  9  3  11
        13 5  15  7
        4  12  2  10
        16 8  14  6   (1/16)

         */

        bayerMatrixFour = new float[][]{
                {1f, 9f, 3f, 11f},
                {13f, 5f, 15f, 7f},
                {4f, 12f, 2f, 10f},
                {16f, 8f, 14f, 6f}
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

        bayerMatrixEight = new float[][]{
                {1f, 49f, 13f, 61f, 4f, 52f, 16f, 64f},
                {33f, 17f, 45f, 29f, 36f, 20f, 48f, 32f},
                {9f, 57f, 5f, 53f, 12f, 60f, 8f, 56f},
                {41f, 25f, 37f, 21f, 44f, 28f, 40f, 24f},
                {3f, 51f, 15f, 63f, 2f, 50f, 14f, 62f},
                {35f, 19f, 47f, 31f, 34f, 18f, 46f, 30f},
                {11f, 59f, 7f, 55f, 10f, 58f, 6f, 54f},
                {43f, 27f, 39f, 23f, 42f, 26f, 38f, 22f}
        };

    }

    private float[][] matrix;
    private int n;
    private float multiplicative;

    public enum DitherType {

        ModeTwo("Two Dimensional"),
        ModeFour("Four Dimensional"),
        ModeEight("Eight Dimensional");

        private final String name;

        DitherType(@NotNull final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    public OrderedDithering(@NotNull final DitherType type) {
        switch (type) {
            case ModeTwo:
                matrix = bayerMatrixTwo;
                n = 2;
                multiplicative = 0.25f;
                break;
            case ModeFour:
                matrix = bayerMatrixFour;
                n = 4;
                multiplicative = 0.0625f;
                break;
            case ModeEight:
                matrix = bayerMatrixEight;
                n = 8;
                multiplicative = 0.015625f;
                break;
        }
        convertToFloat();
    }

    public void convertToFloat() {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] *= multiplicative;
            }
        }
    }

    public void dither(int[] buffer, int width) {
        int height = buffer.length / width;
        for (int y = 0; y < height; y++) {
            int yIndex = y * width;
            for (int x = 0; x < width; x++) {
                int index = yIndex + x;
                buffer[index] = performNearestColor(x, y, buffer[index]);
            }
        }
    }

    /*

    Approximately 86 Palette Colors in Minecraft

    2^3n = 86
    n ~= 2.14 ~= 2

     */
    public int performNearestColor(int x, int y, int color) {
        return getBestColor((int) (color + 2 * (matrix[x % n][y % n] - 0.5)));
    }

    public static byte getBestColor(int rgb) {
        return COLOR_MAP[(rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7 | (rgb & 0xFF) >> 1];
    }

}
