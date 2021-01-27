package com.github.pulsebeat02.video.dither;

import com.github.pulsebeat02.logger.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class FloydImageDither {

    /**
     * What a piece of optimization;
     * Performs incredibly fast Minecraft color conversion and dithering.
     *
     * @author jetp250
     */
    private static int largest = 0;

    private static final int[] PALETTE;
    private static final byte[] COLOR_MAP;
    private static final int[] FULL_COLOR_MAP;

    static {
        PALETTE = StaticDitherInitialization.PALETTE;
        COLOR_MAP = StaticDitherInitialization.COLOR_MAP;
        FULL_COLOR_MAP = new int[128 * 128 * 128];
    }

    public static int getLargestColorVal() {
        return largest;
    }

    public static int getColorFromMinecraftPalette(byte val) {
        return PALETTE[(val + 256) % 256];
    }

    public static byte getBestColorIncludingTransparent(int rgb) {
        return (rgb >>> 24 & 0xFF) == 0 ? 0 : getBestColor(rgb);
    }

    public static byte getBestColor(int rgb) {
        return COLOR_MAP[(rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7 | (rgb & 0xFF) >> 1];
    }

    public static byte getBestColor(int red, int green, int blue) {
        return COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
    }

    public static int getBestFullColor(int red, int green, int blue) {
        return FULL_COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
    }

    public static byte[] simplify(int[] buffer) {
        byte[] map = new byte[buffer.length];
        for (int index = 0; index < buffer.length; index++) {
            int rgb = buffer[index];
            int red = rgb >> 16 & 0xFF;
            int green = rgb >> 8 & 0xFF;
            int blue = rgb & 0xFF;
            byte ptr = getBestColor(red, green, blue);
            map[index] = ptr;
        }
        return map;
    }

    public static void dither(int[] buffer, int width) {
        int height = buffer.length / width;
        int widthMinus = width - 1;
        int heightMinus = height - 1;
        int[][] dither_buffer = new int[2][width + width << 1];
        for (int y = 0; y < height; y++) {
            boolean hasNextY = y < heightMinus;
            int yIndex = y * width;
            if ((y & 0x1) == 0) {
                int bufferIndex = 0;
                int[] buf1 = dither_buffer[0];
                int[] buf2 = dither_buffer[1];
                for (int x = 0; x < width; x++) {
                    boolean hasPrevX = x > 0;
                    boolean hasNextX = x < widthMinus;
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
                    if (hasNextX) {
                        buf1[bufferIndex] = (int) (0.4375 * delta_r);
                        buf1[bufferIndex + 1] = (int) (0.4375 * delta_g);
                        buf1[bufferIndex + 2] = (int) (0.4375 * delta_b);
                    }
                    if (hasNextY) {
                        if (hasPrevX) {
                            buf2[bufferIndex - 6] = (int) (0.1875 * delta_r);
                            buf2[bufferIndex - 5] = (int) (0.1875 * delta_g);
                            buf2[bufferIndex - 4] = (int) (0.1875 * delta_b);
                        }
                        buf2[bufferIndex - 3] = (int) (0.3125 * delta_r);
                        buf2[bufferIndex - 2] = (int) (0.3125 * delta_g);
                        buf2[bufferIndex - 1] = (int) (0.3125 * delta_b);
                        if (hasNextX) {
                            buf2[bufferIndex] = (int) (0.0625 * delta_r);
                            buf2[bufferIndex + 1] = (int) (0.0625 * delta_g);
                            buf2[bufferIndex + 2] = (int) (0.0625 * delta_b);
                        }
                    }
                    buffer[index] = closest;
                }
            } else {
                int bufferIndex = width + (width << 1) - 1;
                int[] buf1 = dither_buffer[1];
                int[] buf2 = dither_buffer[0];
                for (int x = width - 1; x >= 0; x--) {
                    boolean hasPrevX = x < widthMinus;
                    boolean hasNextX = x > 0;
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
                    if (hasNextX) {
                        buf1[bufferIndex] = (int) (0.4375 * delta_b);
                        buf1[bufferIndex - 1] = (int) (0.4375 * delta_g);
                        buf1[bufferIndex - 2] = (int) (0.4375 * delta_r);
                    }
                    if (hasNextY) {
                        if (hasPrevX) {
                            buf2[bufferIndex + 6] = (int) (0.1875 * delta_b);
                            buf2[bufferIndex + 5] = (int) (0.1875 * delta_g);
                            buf2[bufferIndex + 4] = (int) (0.1875 * delta_r);
                        }
                        buf2[bufferIndex + 3] = (int) (0.3125 * delta_b);
                        buf2[bufferIndex + 2] = (int) (0.3125 * delta_g);
                        buf2[bufferIndex + 1] = (int) (0.3125 * delta_r);
                        if (hasNextX) {
                            buf2[bufferIndex] = (int) (0.0625 * delta_b);
                            buf2[bufferIndex - 1] = (int) (0.0625 * delta_g);
                            buf2[bufferIndex - 2] = (int) (0.0625 * delta_r);
                        }
                    }
                    buffer[index] = closest;
                }
            }
        }
    }

    public static ByteBuffer ditherIntoMinecraft(int[] buffer, int width) {
        int height = buffer.length / width;
        int widthMinus = width - 1;
        int heightMinus = height - 1;
        int[][] dither_buffer = new int[2][width + width << 1];
        ByteBuffer data = ByteBuffer.allocate(buffer.length);
        for (int y = 0; y < height; y++) {
            boolean hasNextY = y < heightMinus;
            int yIndex = y * width;
            if ((y & 0x1) == 0) {
                int bufferIndex = 0;
                int[] buf1 = dither_buffer[0];
                int[] buf2 = dither_buffer[1];
                for (int x = 0; x < width; x++) {
                    boolean hasPrevX = x > 0;
                    boolean hasNextX = x < widthMinus;
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
                    if (hasNextX) {
                        buf1[bufferIndex] = (int) (0.4375 * delta_r);
                        buf1[bufferIndex + 1] = (int) (0.4375 * delta_g);
                        buf1[bufferIndex + 2] = (int) (0.4375 * delta_b);
                    }
                    if (hasNextY) {
                        if (hasPrevX) {
                            buf2[bufferIndex - 6] = (int) (0.1875 * delta_r);
                            buf2[bufferIndex - 5] = (int) (0.1875 * delta_g);
                            buf2[bufferIndex - 4] = (int) (0.1875 * delta_b);
                        }
                        buf2[bufferIndex - 3] = (int) (0.3125 * delta_r);
                        buf2[bufferIndex - 2] = (int) (0.3125 * delta_g);
                        buf2[bufferIndex - 1] = (int) (0.3125 * delta_b);
                        if (hasNextX) {
                            buf2[bufferIndex] = (int) (0.0625 * delta_r);
                            buf2[bufferIndex + 1] = (int) (0.0625 * delta_g);
                            buf2[bufferIndex + 2] = (int) (0.0625 * delta_b);
                        }
                    }
                    data.put(index, getBestColor(closest));
                }
            } else {
                int bufferIndex = width + (width << 1) - 1;
                int[] buf1 = dither_buffer[1];
                int[] buf2 = dither_buffer[0];
                for (int x = width - 1; x >= 0; x--) {
                    boolean hasPrevX = x < widthMinus;
                    boolean hasNextX = x > 0;
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
                    if (hasNextX) {
                        buf1[bufferIndex] = (int) (0.4375 * delta_b);
                        buf1[bufferIndex - 1] = (int) (0.4375 * delta_g);
                        buf1[bufferIndex - 2] = (int) (0.4375 * delta_r);
                    }
                    if (hasNextY) {
                        if (hasPrevX) {
                            buf2[bufferIndex + 6] = (int) (0.1875 * delta_b);
                            buf2[bufferIndex + 5] = (int) (0.1875 * delta_g);
                            buf2[bufferIndex + 4] = (int) (0.1875 * delta_r);
                        }
                        buf2[bufferIndex + 3] = (int) (0.3125 * delta_b);
                        buf2[bufferIndex + 2] = (int) (0.3125 * delta_g);
                        buf2[bufferIndex + 1] = (int) (0.3125 * delta_r);
                        if (hasNextX) {
                            buf2[bufferIndex] = (int) (0.0625 * delta_b);
                            buf2[bufferIndex - 1] = (int) (0.0625 * delta_g);
                            buf2[bufferIndex - 2] = (int) (0.0625 * delta_r);
                        }
                    }
                    data.put(index, getBestColor(closest));
                }
            }
        }
        return data;
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    public static int[] getRGBArray(BufferedImage image) {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }

}