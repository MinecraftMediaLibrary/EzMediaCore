package com.github.pulsebeat02.video.dither;

import com.github.pulsebeat02.utility.VideoUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PulseImageDither {

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

    public static void main(String[] args) {
        ByteBuffer buffer = dither(VideoUtilities.getBuffer(new File("/Users/bli24/Desktop/platform1/6vv2qz15h7e51.png")), 3000);
        byte[] arr = VideoUtilities.byteBufferArrayTranslation(buffer);
        System.out.println(Arrays.toString(arr));
        BufferedImage image = VideoUtilities.toBufferedImage(arr);
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);
    }

    public static ByteBuffer dither(int[] buffer, int width) {
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

    public static int getBestFullColor(int red, int green, int blue) {
        return FULL_COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
    }

    public static byte getBestColor(int rgb) {
        return COLOR_MAP[(rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7 | (rgb & 0xFF) >> 1];
    }

}
