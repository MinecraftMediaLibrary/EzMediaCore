package com.github.pulsebeat02.video.dither;

import java.nio.ByteBuffer;

public class StandardDithering implements AbstractDitherHolder {

    private static final byte[] COLOR_MAP;

    static {
        COLOR_MAP = StaticDitherInitialization.COLOR_MAP;
    }

    public int getBestColorNormal(int rgb) {
        return MinecraftMapPalette.getColor(getBestColor(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF)).getRGB();
    }

    public byte getBestColor(int red, int green, int blue) {
        return COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
    }

    public byte getBestColor(int rgb) {
        return COLOR_MAP[(rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7 | (rgb & 0xFF) >> 1];
    }

    @Override
    public void dither(int[] buffer, int width) {
        int height = buffer.length / width;
        for (int y = 0; y < height; y++) {
            int yIndex = y * width;
            for (int x = 0; x < width; x++) {
                int index = yIndex + x;
                int color = buffer[index];
                buffer[index] = getBestColorNormal(color);
            }
        }
    }

    @Override
    public ByteBuffer ditherIntoMinecraft(int[] buffer, int width) {
        int height = buffer.length / width;
        ByteBuffer data = ByteBuffer.allocate(buffer.length);
        for (int y = 0; y < height; y++) {
            int yIndex = y * width;
            for (int x = 0; x < width; x++) {
                int index = yIndex + x;
                int color = buffer[index];
                data.put(getBestColor(color));
            }
        }
        return data;
    }

    public static byte[] getColorMap() {
        return COLOR_MAP;
    }

}
