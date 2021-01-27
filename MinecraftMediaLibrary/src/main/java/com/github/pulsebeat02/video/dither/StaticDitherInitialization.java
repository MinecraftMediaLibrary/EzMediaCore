package com.github.pulsebeat02.video.dither;

import com.github.pulsebeat02.logger.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class StaticDitherInitialization {

    public static int largest = 0;

    public static final int[] PALETTE;
    public static final byte[] COLOR_MAP = new byte[128 * 128 * 128];
    public static final int[] FULL_COLOR_MAP = new int[128 * 128 * 128];

    static {
        List<Integer> colors = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < 256; ++i) {
            try {
                @SuppressWarnings("deprecation")
                Color color = MinecraftMapPalette.getColor((byte) i);
                colors.add(color.getRGB());
            } catch (IndexOutOfBoundsException e) {
                Logger.info("Captured " + (i - 1) + " colors!");
                largest = i - 1;
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
        Logger.info("Initial lookup table initialized in " + (end - start) / 1_000_000.0 + " ms");
    }

}

class LoadRed extends RecursiveTask<byte[]> {

    protected final int r;
    protected final int[] palette;

    protected LoadRed(int[] palette, int r) {
        this.r = r;
        this.palette = palette;
    }

    @Override
    protected byte[] compute() {
        List<LoadGreen> greenSub = new ArrayList<>(128);
        for (int g = 0; g < 256; g += 2) {
            LoadGreen green = new LoadGreen(palette, r, g);
            greenSub.add(green);
            green.fork();
        }
        byte[] vals = new byte[16384];
        for (int i = 0; i < 128; i++) {
            byte[] sub = greenSub.get(i).join();
            int index = i << 7;
            System.arraycopy(sub, 0, vals, index, 128);
        }
        return vals;
    }

}

class LoadGreen extends RecursiveTask<byte[]> {

    protected final int r;
    protected final int g;
    protected final int[] palette;

    protected LoadGreen(int[] palette, int r, int g) {
        this.r = r;
        this.g = g;
        this.palette = palette;
    }

    @Override
    protected byte[] compute() {
        List<LoadBlue> blueSub = new ArrayList<>(128);
        for (int b = 0; b < 256; b += 2) {
            LoadBlue blue = new LoadBlue(palette, r, g, b);
            blueSub.add(blue);
            blue.fork();
        }
        byte[] matches = new byte[128];
        for (int i = 0; i < 128; i++) {
            matches[i] = blueSub.get(i).join();
        }
        return matches;
    }

}

class LoadBlue extends RecursiveTask<Byte> {

    protected final int r, g, b;
    protected final int[] palette;

    protected LoadBlue(int[] palette, int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.palette = palette;
    }

    @Override
    protected Byte compute() {
        int val = 0;
        float best_distance = Float.MAX_VALUE;
        float distance;
        int col;
        for (int i = 4; i < palette.length; ++i) {
            col = palette[i];
            int r2 = col >> 16 & 0xFF;
            int g2 = col >> 8 & 0xFF;
            int b2 = col & 0xFF;

            float red_avg = (r + r2) * .5f;
            int redVal = r - r2;
            int greenVal = g - g2;
            int blueVal = b - b2;
            float weight_red = 2.0f + red_avg * (1f / 256f);
            float weight_green = 4.0f;
            float weight_blue = 2.0f + (255.0f - red_avg) * (1f / 256f);
            distance = weight_red * redVal * redVal + weight_green * greenVal * greenVal + weight_blue * blueVal * blueVal;
            if (distance < best_distance) {
                best_distance = distance;
                val = i;
            }
        }
        return (byte) val;
    }

}

