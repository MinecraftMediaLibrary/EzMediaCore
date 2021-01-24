package com.github.pulsebeat02.video.dither;

import com.github.pulsebeat02.utility.VideoUtilities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class JetpImageDither {

    /**
     * What a piece of optimization;
     * Performs incredibly fast Minecraft color conversion and dithering.
     *
     * @author jetp250
     */
    private static int largest = 0;

    private static final int[] PALETTE;
    private static final byte[] COLOR_MAP = new byte[128 * 128 * 128];
    private static final int[] FULL_COLOR_MAP = new int[128 * 128 * 128];

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
        System.out.println("Initial lookup table initialized in " + (end - start) / 1_000_000.0 + " ms");
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
