package com.github.pulsebeat02.minecraftmedialibrary.video.dither;

import java.nio.ByteBuffer;

public class StandardDithering implements AbstractDitherHolder {

    private static final byte[] COLOR_MAP;

    static {
        COLOR_MAP = StaticDitherInitialization.COLOR_MAP;
    }

    public static byte[] getColorMap() {
        return COLOR_MAP;
    }

    public int getBestColorNormal(final int rgb) {
        return MinecraftMapPalette.getColor(getBestColor(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF)).getRGB();
    }

    public byte getBestColor(final int red, final int green, final int blue) {
        return COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
    }

    public byte getBestColor(final int rgb) {
        return COLOR_MAP[(rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7 | (rgb & 0xFF) >> 1];
    }

    @Override
    public void dither(final int[] buffer, final int width) {
        final int height = buffer.length / width;
        for (int y = 0; y < height; y++) {
            final int yIndex = y * width;
            for (int x = 0; x < width; x++) {
                final int index = yIndex + x;
                final int color = buffer[index];
                buffer[index] = getBestColorNormal(color);
            }
        }
    }

    @Override
    public ByteBuffer ditherIntoMinecraft(final int[] buffer, final int width) {
        final int height = buffer.length / width;
        final ByteBuffer data = ByteBuffer.allocate(buffer.length);
        for (int y = 0; y < height; y++) {
            final int yIndex = y * width;
            for (int x = 0; x < width; x++) {
                final int index = yIndex + x;
                final int color = buffer[index];
                data.put(getBestColor(color));
            }
        }
        return data;
    }

    @Override
    public DitherSetting getSetting() {
        return DitherSetting.STANDARD_MINECRAFT_DITHER;
    }

}
