/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.brandonli.mcav.media.mcv2;

/**
 * The normative pixel arithmetic of MCV2 leaves, shared by the decoder and the encoder so that the reconstruction the
 * encoder scores is, by construction, exactly the one the decoder produces.
 *
 * <p>Every kernel reconstructs one whole square block, {@code size * size} pixels in row-major order, three channels
 * each, into an {@code int} array of 0..255 values. Motion predictions are passed as four times the predicted value,
 * which is always an integer: half-pixel bilinear sampling averages one, two or four reference pixels.
 *
 * <p>The reference decoder computes in float32 and float64, but every number it forms is a dyadic rational with few
 * significant bits: predictions are quarters, a grid's interpolation weights are multiples of {@code 1 / (2 size)} on
 * each axis, nodes are small integers scaled by powers of two, and the largest sum stays below 2^13 with at most ten
 * fractional bits, which is 23 of float32's 24 significand bits. So none of its operations rounds, and every channel
 * is {@code floor(clamp(v, 0, 255) + 0.5)} of the exact value {@code v}. The kernels compute that exact value in integer
 * arithmetic, scaled by {@code (2 size)^2}, interpolating separably (rows, then columns), which is the same result
 * without floating point. The first, floating-point form of the kernels is kept by the tests as the oracle these are
 * compared with.
 */
public final class Reconstruction {

  /** Interpolation tables for leaf sizes 8, 16, 32 (index 0..2) and grid widths 1, 2, 4, 8 (index 0..3). */
  private static final int[][][] LOWER = new int[3][4][];
  private static final int[][][] UPPER = new int[3][4][];
  /** The distance to the lower node in units of {@code 1 / (2 size)}. */
  private static final int[][][] WEIGHT = new int[3][4][];

  static {
    for (int s = 0; s < 3; s++) {
      final int size = 8 << s;
      final int span = 2 * size;
      for (int g = 0; g < 4; g++) {
        final int grid = 1 << g;
        final int[] lower = new int[size];
        final int[] upper = new int[size];
        final int[] weight = new int[size];
        for (int p = 0; p < size; p++) {
          // the node position ((p + 0.5) * grid) / size - 0.5, clamped to the grid, times 2 * size
          final int position = Math.min(Math.max((2 * p + 1) * grid - size, 0), (grid - 1) * span);
          lower[p] = position / span;
          upper[p] = Math.min(lower[p] + 1, grid - 1);
          weight[p] = position - lower[p] * span;
        }
        LOWER[s][g] = lower;
        UPPER[s][g] = upper;
        WEIGHT[s][g] = weight;
      }
    }
  }

  /**
   * Scratch space for the kernels. A decoder or an encoder worker keeps one and passes it to every kernel it calls;
   * it is not thread-safe.
   */
  public static final class Scratch {

    private final int[] nodes = new int[3 * 64];
    private final int[] rows = new int[8 * 32];
    private final int[] first = new int[32 * 32];
    private final int[] second = new int[32 * 32];
    private final int[] third = new int[32 * 32];

    /** Constructs new scratch space. */
    public Scratch() {
      // the arrays are the whole state
    }
  }

  private Reconstruction() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Rounds a reconstructed channel exactly as the reference does: {@code floor(clamp(value, 0, 255) + 0.5)} in float32.
   *
   * @param value the channel value
   * @return the byte value, 0 to 255
   */
  public static int rgb8(final float value) {
    final float clamped = Math.min(Math.max(value, 0.0f), 255.0f);
    return (int) Math.floor(clamped + 0.5f);
  }

  /** {@code floor(clamp(value / 2^shift, 0, 255) + 0.5)} of an exact fixed-point value. */
  private static int round(final int value, final int shift) {
    return Math.min(Math.max((value + (1 << (shift - 1))) >> shift, 0), 255);
  }

  private static int sizeIndex(final int size) {
    return Integer.numberOfTrailingZeros(size) - 3;
  }

  /** The shift that undoes the {@code (2 size)^2} scale of interpolated values. */
  private static int shift(final int size) {
    return 2 * (Integer.numberOfTrailingZeros(size) + 1);
  }

  /**
   * Interpolates a grid of nodes over a block: {@code plane[y * size + x]} is the bilinear value at the pixel times
   * {@code (2 size)^2}, exactly.
   *
   * @param nodes  the node values
   * @param offset the index of node (0, 0)
   * @param stride the distance between consecutive nodes
   * @param grid   the grid width, 1, 2, 4 or 8
   * @param size   the block size, 8, 16 or 32
   * @param rows   scratch space for {@code grid * size} values
   * @param plane  receives {@code size * size} values
   */
  private static void plane(
    final int[] nodes,
    final int offset,
    final int stride,
    final int grid,
    final int size,
    final int[] rows,
    final int[] plane
  ) {
    final int s = sizeIndex(size);
    final int g = Integer.numberOfTrailingZeros(grid);
    final int[] lower = LOWER[s][g];
    final int[] upper = UPPER[s][g];
    final int[] weight = WEIGHT[s][g];
    final int span = 2 * size;
    for (int j = 0; j < grid; j++) {
      final int row = offset + j * grid * stride;
      final int at = j * size;
      for (int x = 0; x < size; x++) {
        final int w = weight[x];
        rows[at + x] = nodes[row + lower[x] * stride] * (span - w) + nodes[row + upper[x] * stride] * w;
      }
    }
    for (int y = 0; y < size; y++) {
      final int top = lower[y] * size;
      final int bottom = upper[y] * size;
      final int w = weight[y];
      final int v = span - w;
      final int at = y * size;
      for (int x = 0; x < size; x++) {
        plane[at + x] = rows[top + x] * v + rows[bottom + x] * w;
      }
    }
  }

  /**
   * Predicts a block from a reference picture: every pixel plus a half-pixel displacement, each coordinate clamped to
   * the picture before bilinear sampling.
   *
   * @param reference the reference picture, row-major RGB
   * @param width     the picture width
   * @param height    the picture height
   * @param x         the block's left edge
   * @param y         the block's top edge
   * @param size      the block size; pixels outside the picture are predicted too, from clamped coordinates
   * @param mx        the horizontal displacement in half pixels
   * @param my        the vertical displacement in half pixels
   * @param out       receives four times each predicted channel, {@code size * size * 3} values
   */
  public static void predict(
    final byte[] reference,
    final int width,
    final int height,
    final int x,
    final int y,
    final int size,
    final int mx,
    final int my,
    final int[] out
  ) {
    for (int py = 0; py < size; py++) {
      final int hy = Math.min(Math.max(2 * (y + py) + my, 0), 2 * (height - 1));
      final int y0 = hy >> 1;
      final int y1 = Math.min(y0 + 1, height - 1);
      final boolean halfY = (hy & 1) != 0;
      for (int px = 0; px < size; px++) {
        final int hx = Math.min(Math.max(2 * (x + px) + mx, 0), 2 * (width - 1));
        final int x0 = hx >> 1;
        final int x1 = Math.min(x0 + 1, width - 1);
        final boolean halfX = (hx & 1) != 0;
        final int a = (y0 * width + x0) * 3;
        final int at = (py * size + px) * 3;
        if (!halfX && !halfY) {
          for (int c = 0; c < 3; c++) {
            out[at + c] = 4 * (reference[a + c] & 0xFF);
          }
        } else if (!halfY) {
          final int b = (y0 * width + x1) * 3;
          for (int c = 0; c < 3; c++) {
            out[at + c] = 2 * ((reference[a + c] & 0xFF) + (reference[b + c] & 0xFF));
          }
        } else if (!halfX) {
          final int b = (y1 * width + x0) * 3;
          for (int c = 0; c < 3; c++) {
            out[at + c] = 2 * ((reference[a + c] & 0xFF) + (reference[b + c] & 0xFF));
          }
        } else {
          final int b = (y0 * width + x1) * 3;
          final int d = (y1 * width + x0) * 3;
          final int e = (y1 * width + x1) * 3;
          for (int c = 0; c < 3; c++) {
            out[at + c] = (reference[a + c] & 0xFF) + (reference[b + c] & 0xFF) + (reference[d + c] & 0xFF) + (reference[e + c] & 0xFF);
          }
        }
      }
    }
  }

  /**
   * Reconstructs a skip or motion block: the prediction, rounded.
   *
   * @param prediction four times the predicted channels
   * @param size       the block size
   * @param out        the reconstructed channels
   */
  public static void predicted(final int[] prediction, final int size, final int[] out) {
    final int n = size * size * 3;
    for (int i = 0; i < n; i++) {
      // four times a byte, so rounding needs no clamp
      out[i] = (prediction[i] + 2) >> 2;
    }
  }

  /**
   * Reconstructs a flat block.
   *
   * @param color the colour as {@code 0xRRGGBB}
   * @param size  the block size
   * @param out   the reconstructed channels
   */
  public static void solid(final int color, final int size, final int[] out) {
    final int n = size * size;
    for (int i = 0; i < n; i++) {
      out[i * 3] = (color >> 16) & 0xFF;
      out[i * 3 + 1] = (color >> 8) & 0xFF;
      out[i * 3 + 2] = color & 0xFF;
    }
  }

  /**
   * Reconstructs a two-colour palette block from its record: two RGB endpoints, then one selector bit per pixel.
   *
   * @param record the bytes holding the record
   * @param offset the record offset
   * @param size   the block size
   * @param out    the reconstructed channels
   */
  public static void palette(final byte[] record, final int offset, final int size, final int[] out) {
    final int n = size * size;
    for (int i = 0; i < n; i++) {
      final int bit = (record[offset + 6 + i / 8] >> (i & 7)) & 1;
      final int at = offset + bit * 3;
      out[i * 3] = record[at] & 0xFF;
      out[i * 3 + 1] = record[at + 1] & 0xFF;
      out[i * 3 + 2] = record[at + 2] & 0xFF;
    }
  }

  /**
   * Reconstructs a pattern palette block.
   *
   * @param pattern the resolved pattern
   * @param size    the block size
   * @param out     the reconstructed channels
   */
  public static void pattern(final PatternRecord pattern, final int size, final int[] out) {
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        final int color = pattern.colorAt(x, y);
        final int at = (y * size + x) * 3;
        out[at] = (color >> 16) & 0xFF;
        out[at + 1] = (color >> 8) & 0xFF;
        out[at + 2] = color & 0xFF;
      }
    }
  }

  /**
   * Reconstructs an RGB intra grid block (modes 4 to 7) from its interleaved unsigned nodes.
   *
   * @param record  the bytes holding the nodes
   * @param offset  the offset of the first node
   * @param grid    the grid width
   * @param size    the block size
   * @param scratch the scratch space
   * @param out     the reconstructed channels
   */
  public static void intraGrid(
    final byte[] record,
    final int offset,
    final int grid,
    final int size,
    final Scratch scratch,
    final int[] out
  ) {
    final int[] nodes = scratch.nodes;
    for (int i = 0; i < 3 * grid * grid; i++) {
      nodes[i] = record[offset + i] & 0xFF;
    }
    final int[] r = scratch.first;
    final int[] g = scratch.second;
    final int[] b = scratch.third;
    plane(nodes, 0, 3, grid, size, scratch.rows, r);
    plane(nodes, 1, 3, grid, size, scratch.rows, g);
    plane(nodes, 2, 3, grid, size, scratch.rows, b);
    final int shift = shift(size);
    final int n = size * size;
    for (int i = 0; i < n; i++) {
      out[i * 3] = round(r[i], shift);
      out[i * 3 + 1] = round(g[i], shift);
      out[i * 3 + 2] = round(b[i], shift);
    }
  }

  /**
   * Reconstructs a YCoCg residual grid block (modes 8 to 11): signed nodes scaled by {@code 2^q} before
   * interpolation, converted and added to the prediction.
   *
   * @param prediction four times the predicted channels
   * @param record     the bytes holding the nodes
   * @param offset     the offset of the first node, after the motion bytes
   * @param grid       the grid width
   * @param q          the quantizer
   * @param size       the block size
   * @param scratch    the scratch space
   * @param out        the reconstructed channels
   */
  public static void residualGrid(
    final int[] prediction,
    final byte[] record,
    final int offset,
    final int grid,
    final int q,
    final int size,
    final Scratch scratch,
    final int[] out
  ) {
    final int[] nodes = scratch.nodes;
    for (int i = 0; i < 3 * grid * grid; i++) {
      nodes[i] = record[offset + i] << q;
    }
    final int[] c0 = scratch.first;
    final int[] c1 = scratch.second;
    final int[] c2 = scratch.third;
    plane(nodes, 0, 3, grid, size, scratch.rows, c0);
    plane(nodes, 1, 3, grid, size, scratch.rows, c1);
    plane(nodes, 2, 3, grid, size, scratch.rows, c2);
    final int shift = shift(size);
    final int quarter = size * size;
    final int n = size * size;
    for (int i = 0; i < n; i++) {
      final int at = i * 3;
      out[at] = round(prediction[at] * quarter + ((c0[i] + c1[i]) - c2[i]), shift);
      out[at + 1] = round(prediction[at + 1] * quarter + (c0[i] + c2[i]), shift);
      out[at + 2] = round(prediction[at + 2] * quarter + ((c0[i] - c1[i]) - c2[i]), shift);
    }
  }

  /**
   * Reconstructs a reduced-chroma block (modes 12 to 15): a luma grid and a coarser interleaved chroma grid, luma
   * unsigned in an intra record and signed in a residual one, a residual record scaled by {@code 2^q} and added to the
   * prediction.
   *
   * @param prediction four times the predicted channels, or null for an intra record
   * @param record     the bytes holding the record
   * @param offset     the offset of the first luma node, after any motion bytes
   * @param luma       the luma grid width
   * @param chroma     the chroma grid width
   * @param q          the quantizer, zero for an intra record
   * @param size       the block size
   * @param scratch    the scratch space
   * @param out        the reconstructed channels
   */
  public static void reduced(
    final int@org.checkerframework.checker.nullness.qual.Nullable[] prediction,
    final byte[] record,
    final int offset,
    final int luma,
    final int chroma,
    final int q,
    final int size,
    final Scratch scratch,
    final int[] out
  ) {
    final int[] nodes = scratch.nodes;
    for (int i = 0; i < luma * luma; i++) {
      nodes[i] = prediction != null ? record[offset + i] : record[offset + i] & 0xFF;
    }
    final int chromaAt = offset + luma * luma;
    for (int i = 0; i < 2 * chroma * chroma; i++) {
      nodes[64 + i] = record[chromaAt + i];
    }
    final int[] y = scratch.first;
    final int[] co = scratch.second;
    final int[] cg = scratch.third;
    plane(nodes, 0, 1, luma, size, scratch.rows, y);
    plane(nodes, 64, 2, chroma, size, scratch.rows, co);
    plane(nodes, 65, 2, chroma, size, scratch.rows, cg);
    final int shift = shift(size);
    final int n = size * size;
    if (prediction == null) {
      for (int i = 0; i < n; i++) {
        final int at = i * 3;
        out[at] = round((y[i] + co[i]) - cg[i], shift);
        out[at + 1] = round(y[i] + cg[i], shift);
        out[at + 2] = round((y[i] - co[i]) - cg[i], shift);
      }
      return;
    }
    final int quarter = size * size;
    for (int i = 0; i < n; i++) {
      final int at = i * 3;
      out[at] = round(prediction[at] * quarter + (((y[i] + co[i]) - cg[i]) << q), shift);
      out[at + 1] = round(prediction[at + 1] * quarter + ((y[i] + cg[i]) << q), shift);
      out[at + 2] = round(prediction[at + 2] * quarter + (((y[i] - co[i]) - cg[i]) << q), shift);
    }
  }

  /**
   * Reconstructs a compact record's body (mode 17) on top of its motion prediction.
   *
   * @param prediction four times the predicted channels, at the record's motion
   * @param record     the bytes holding the record
   * @param body       the offset of the first body byte
   * @param kind       the class, 0 to 8
   * @param q          the quantizer
   * @param size       the block size
   * @param scratch    the scratch space
   * @param out        the reconstructed channels
   */
  public static void compact(
    final int[] prediction,
    final byte[] record,
    final int body,
    final int kind,
    final int q,
    final int size,
    final Scratch scratch,
    final int[] out
  ) {
    final int n = size * size;
    if (kind == CompactRecord.GAIN_BIAS) {
      // p * (1 + gain / 64) + bias, scaled by 256: four times the prediction times (64 + gain), plus 256 times the bias
      final int gain = 64 + record[body];
      final int by = record[body + 1];
      final int bco = record[body + 2];
      final int bcg = record[body + 3];
      final int red = ((by + bco) - bcg) << 8;
      final int green = (by + bcg) << 8;
      final int blue = ((by - bco) - bcg) << 8;
      for (int i = 0; i < n; i++) {
        final int at = i * 3;
        out[at] = round(prediction[at] * gain + red, 8);
        out[at + 1] = round(prediction[at + 1] * gain + green, 8);
        out[at + 2] = round(prediction[at + 2] * gain + blue, 8);
      }
      return;
    }
    if (kind == CompactRecord.DC_Y) {
      // four times the prediction plus four times the offset
      final int dc = (record[body] << q) * 4;
      for (int i = 0; i < n * 3; i++) {
        out[i] = round(prediction[i] + dc, 2);
      }
      return;
    }
    final int[] luma = scratch.first;
    final int co;
    final int cg;
    if (kind == CompactRecord.LOW2) {
      // (dc + gx * axis[x] + gy * axis[y]) * size, with axis[i] = (2 i + 1 - size) / size, times 4 size
      final int dc = record[body] * size;
      final int gx = record[body + 1];
      final int gy = record[body + 2];
      for (int y = 0; y < size; y++) {
        for (int x = 0; x < size; x++) {
          luma[y * size + x] = (dc + gx * (2 * x + 1 - size) + gy * (2 * y + 1 - size)) * 4 * size;
        }
      }
      co = record[body + 3];
      cg = record[body + 4];
    } else {
      co = chromaOffset(record, body, kind, 0);
      cg = chromaOffset(record, body, kind, 1);
      final int grid = nodes(record, body, kind, scratch.nodes);
      plane(scratch.nodes, 0, 1, grid, size, scratch.rows, luma);
    }
    final int shift = shift(size);
    final int quarter = size * size;
    final int scale = 4 * size * size;
    final int red = ((co - cg) * scale) << q;
    final int green = (cg * scale) << q;
    final int blue = (-(co + cg) * scale) << q;
    for (int i = 0; i < n; i++) {
      final int at = i * 3;
      final int ys = luma[i] << q;
      out[at] = round(prediction[at] * quarter + ys + red, shift);
      out[at + 1] = round(prediction[at + 1] * quarter + ys + green, shift);
      out[at + 2] = round(prediction[at + 2] * quarter + ys + blue, shift);
    }
  }

  /** The chroma offset of a grid class: {@code which} 0 for Co, 1 for Cg; zero for the luma-only class. */
  private static int chromaOffset(final byte[] record, final int body, final int kind, final int which) {
    return switch (kind) {
      case CompactRecord.GRID2_YC -> record[body + 4 + which];
      case CompactRecord.GRID4_N4_YC -> record[body + 8 + which];
      case CompactRecord.GRID4_YC -> record[body + 16 + which];
      case CompactRecord.VQ64, CompactRecord.PQ64 -> record[body + 1 + which];
      default -> 0;
    };
  }

  /** Reads a grid class's luma nodes into {@code nodes} and returns the grid width. */
  private static int nodes(final byte[] record, final int body, final int kind, final int[] nodes) {
    return switch (kind) {
      case CompactRecord.GRID2_YC -> {
        for (int i = 0; i < 4; i++) {
          nodes[i] = record[body + i];
        }
        yield 2;
      }
      case CompactRecord.GRID4_N4_YC, CompactRecord.GRID4_N4_Y -> {
        for (int i = 0; i < 16; i++) {
          final int value = ((record[body + i / 2] & 0xFF) >> ((i & 1) * 4)) & 15;
          nodes[i] = (value ^ 8) - 8;
        }
        yield 4;
      }
      case CompactRecord.GRID4_YC -> {
        for (int i = 0; i < 16; i++) {
          nodes[i] = record[body + i];
        }
        yield 4;
      }
      default -> {
        // VQ64 and PQ64: book entries plus the DC
        final int dc = record[body];
        final int ids = (record[body + 3] & 0xFF) | (kind == CompactRecord.PQ64 ? (record[body + 4] & 0xFF) << 8 : 0);
        for (int i = 0; i < 16; i++) {
          final int row = i / 4;
          final int column = i % 4;
          final int book;
          if (kind == CompactRecord.VQ64) {
            book = ResidualBooks.vq(ids, i);
          } else if (column < 2) {
            book = ResidualBooks.pq(0, ids & 63, row * 2 + column);
          } else {
            book = ResidualBooks.pq(1, ids >> 6, row * 2 + column - 2);
          }
          nodes[i] = book + dc;
        }
        yield 4;
      }
    };
  }
}
