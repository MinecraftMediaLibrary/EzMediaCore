package com.github.pulsebeat02.video.dither;

import com.github.pulsebeat02.utility.VideoUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SierraFilterLiteDither {

    private static final int[] PALETTE;
    private static final int[] FULL_COLOR_MAP = new int[128 * 128 * 128];
    private static final byte[] COLOR_MAP = new byte[128 * 128 * 128];

    static {
        List<Integer> colors = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < 256; ++i) {
            try {
                @SuppressWarnings("deprecation")
                Color color = MapPalette.getColor((byte) i);
                colors.add(color.getRGB());
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Captured " + (i - 1) + " colors!");
                break;
            }
        }
        PALETTE = new int[colors.size()];
        int index = 0;
        for (int color : colors) {
            PALETTE[index++] = color;
        }
        PALETTE[0] = 0;
        List<LoadRed> tasks = new ArrayList<>(128);
        for (int r = 0; r < 256; r += 2) {
            LoadRed red = new LoadRed(PALETTE, r);
            tasks.add(red);
            red.fork();
        }
        for (int i = 0; i < 128; i++) {
            byte[] sub = tasks.get(i).join();
            int ci = i << 14;
            for (int si = 0; si < 16384; si++) {
                COLOR_MAP[ci + si] = sub[si];
                FULL_COLOR_MAP[ci + si] = PALETTE[Byte.toUnsignedInt(sub[si])];
            }
        }
        long end = System.nanoTime();
        System.out.println("Initial lookup table initialized in " + (end - start) / 1_000_000.0 + " ms");
    }

    public static void main(String[] args) throws FileNotFoundException {
        int[] buffer = VideoUtilities.getBuffer(new File("/Users/Brandon/Desktop/platform1/6vv2qz15h7e51.png"));
        long start = System.currentTimeMillis();
        dither(buffer, 3000);
        long after = System.currentTimeMillis();
        System.out.println(after - start);
    }

    public static void dither(int[] buffer, int width) {
        int height = buffer.length / width;
        int widthMinus = width - 1;
        int heightMinus = height - 1;
        int[][] dither_buffer = new int[2][width + width << 1];

        /*

        Simple Sierra 2-4A Dithering (Filter Lite)


            *   2
        1   1           (1/4)

        When Jagged Matrix is Multiplied:

              *   2/4
        1/4  1/4

         */

        for (int y = 0; y < height; ++y) {
            boolean hasNextY = y < heightMinus;
            int yIndex = y * width;
            if ((y & 0x1) == 0) {
                int bufferIndex = 0;
                int[] buf1 = dither_buffer[0];
                int[] buf2 = dither_buffer[1];
                for (int x = 0; x < width; ++x) {
                    int index = yIndex + x;
                    int rgb = buffer[index];
                    int red = rgb >> 16 & 0xFF;
                    int green = rgb >> 8 & 0xFF;
                    int blue = rgb & 0xFF;
                    red = (red += buf1[bufferIndex++]) > 255 ? 255 : red < 0 ? 0 : red;
                    green = (green += buf1[bufferIndex++]) > 255 ? 255 : green < 0 ? 0 : green;
                    blue = (blue += buf1[bufferIndex++]) > 255 ? 255 : blue < 0 ? 0 : blue;
                    int closest = getBestFullColor(red, green, blue);
                    int delta_r = red - (closest >> 16 & 0xFF);
                    int delta_g = green - (closest >> 8 & 0xFF);
                    int delta_b = blue - (closest & 0xFF);
                    if (x < widthMinus) {
                        buf1[bufferIndex] = (int) (0.5 * delta_r);
                        buf1[bufferIndex + 1] = (int) (0.5 * delta_g);
                        buf1[bufferIndex + 2] = (int) (0.5 * delta_b);
                    }
                    if (hasNextY) {
                        if (x > 0) {
                            buf2[bufferIndex - 6] = (int) (0.25 * delta_r);
                            buf2[bufferIndex - 5] = (int) (0.25 * delta_g);
                            buf2[bufferIndex - 4] = (int) (0.25 * delta_b);
                        }
                        buf2[bufferIndex - 3] = (int) (0.25 * delta_r);
                        buf2[bufferIndex - 2] = (int) (0.25 * delta_g);
                        buf2[bufferIndex - 1] = (int) (0.25 * delta_b);
                    }
                    buffer[index] = closest;
                }
            } else {
                int bufferIndex = width + (width << 1) - 1;
                int[] buf1 = dither_buffer[1];
                int[] buf2 = dither_buffer[0];
                for (int x = width - 1; x >= 0; --x) {
                    int index = yIndex + x;
                    int rgb = buffer[index];
                    int red = rgb >> 16 & 0xFF;
                    int green = rgb >> 8 & 0xFF;
                    int blue = rgb & 0xFF;
                    blue = (blue += buf1[bufferIndex--]) > 255 ? 255 : blue < 0 ? 0 : blue;
                    green = (green += buf1[bufferIndex--]) > 255 ? 255 : green < 0 ? 0 : green;
                    red = (red += buf1[bufferIndex--]) > 255 ? 255 : red < 0 ? 0 : red;
                    int closest = getBestFullColor(red, green, blue);
                    int delta_r = red - (closest >> 16 & 0xFF);
                    int delta_g = green - (closest >> 8 & 0xFF);
                    int delta_b = blue - (closest & 0xFF);
                    if (x > 0) {
                        buf1[bufferIndex] = (int) (0.5 * delta_b);
                        buf1[bufferIndex - 1] = (int) (0.5 * delta_g);
                        buf1[bufferIndex - 2] = (int) (0.5 * delta_r);
                    }
                    if (hasNextY) {
                        if (x < widthMinus) {
                            buf2[bufferIndex + 6] = (int) (0.25 * delta_b);
                            buf2[bufferIndex + 5] = (int) (0.25 * delta_g);
                            buf2[bufferIndex + 4] = (int) (0.25 * delta_r);
                        }
                        buf2[bufferIndex + 3] = (int) (0.25 * delta_b);
                        buf2[bufferIndex + 2] = (int) (0.25 * delta_g);
                        buf2[bufferIndex + 1] = (int) (0.25 * delta_r);
                    }
                    buffer[index] = closest;
                }
            }
        }
    }

    public static int getBestFullColor(int red, int green, int blue) {
        return FULL_COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
    }

}
