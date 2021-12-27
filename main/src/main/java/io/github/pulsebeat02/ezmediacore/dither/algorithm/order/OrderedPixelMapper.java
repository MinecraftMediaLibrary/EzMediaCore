package io.github.pulsebeat02.ezmediacore.dither.algorithm.order;

import io.github.pulsebeat02.ezmediacore.dither.OrderedPixel;

public final class OrderedPixelMapper {

  public int[][] createBayerMatrixPowerTwo(final int xdim, final int ydim) {
    final int M = this.log2(xdim);
    final int L = this.log2(ydim);
    final int[][] matrix = new int[xdim][ydim];
    for (int y = 0; y < ydim; y++) {
      for (int x = 0; x < xdim; x++) {
        int v = 0;
        int offset = 0;
        int xmask = M;
        int ymask = L;
        if (M == 0 || (M > L && L != 0)) {
          final int xc = x ^ ((y << M) >> L);
          final int yc = y;
          for (int bit = 0; bit < M + L; ) {
            ymask--;
            v |= ((yc >> ymask) & 1) << bit;
            bit++;
            for (offset += M; offset >= L; offset -= L) {
              xmask--;
              v |= ((xc >> xmask) & 1) << bit;
              bit++;
            }
          }
        } else {
          final int xc = x;
          final int yc = y ^ ((x << L) >> M);
          for (int bit = 0; bit < M + L; ) {
            xmask--;
            v |= ((xc >> xmask) & 1) << bit;
            bit++;
            for (offset += L; offset >= M; offset -= M) {
              ymask--;
              v |= ((yc >> ymask) & 1) << bit;
              bit++;
            }
          }
        }
        matrix[y][x] = v;
      }
    }
    return matrix;
  }

  private int log2(int num) {
    int r = 0;
    num >>= 1;
    while (num != 0) {
      r++;
      num >>= 1;
    }
    return r;
  }

  private float convertThresholdToAddition(final float scale, final int value, final int max) {
    return (float) (scale * ((value + 1.0) / max - 0.50000006));
  }

  private OrderedDitherFunction createOrderedDitherFunction(final int[][] matrix, final int max,
      final float strength) {
    final int ydim = matrix.length;
    final int xdim = matrix[0].length;
    final float scale = 65535.0f * strength;
    final float[][] precalc = new float[ydim][xdim];
    for (int i = 0; i < ydim; i++) {
      for (int j = 0; j < xdim; j++) {
        precalc[i][j] = this.convertThresholdToAddition(scale, matrix[i][j], max);
      }
    }
    return (x, y, r, g, b) -> {
      final int red = (int) (r + precalc[y % ydim][x % xdim]);
      final int green = (int) (g + precalc[y % ydim][x % xdim]);
      final int blue = (int) (b + precalc[y % ydim][x % xdim]);
      return OrderedPixel.ofPixel(red, green, blue);
    };
  }
}
