package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.dither.palette.MapPalette;

import java.awt.*;
import java.util.List;

public final class DitheringColorTest {

  public static void main(final String[] args) {
    final Color red = findClosestColor(Color.RED);
    final Color green = findClosestColor(Color.GREEN);
    final Color blue = findClosestColor(Color.BLUE);
    final Color white = findClosestColor(Color.WHITE);
    final Color black = findClosestColor(Color.BLACK);
    final Color yellow = findClosestColor(Color.YELLOW);
    final Color cyan = findClosestColor(Color.CYAN);
    final Color magenta = findClosestColor(Color.MAGENTA);
    System.out.println("Red: " + red);
    System.out.println("Green: " + green);
    System.out.println("Blue: " + blue);
    System.out.println("White: " + white);
    System.out.println("Black: " + black);
    System.out.println("Yellow: " + yellow);
    System.out.println("Cyan: " + cyan);
    System.out.println("Magenta: " + magenta);
    System.out.println(List.of(red.getRGB(), green.getRGB(), blue.getRGB(), white.getRGB(), black.getRGB(), yellow.getRGB(), cyan.getRGB(), magenta.getRGB()));
  }

  private static Color findClosestColor(final Color other) {
    Color closest = null;
    final Color[] palette = MapPalette.NMS_PALETTE;
    double closestDistanceSquared = Double.MAX_VALUE;
    for (final Color color : palette) {
      final double distanceSquared = calculateDistanceSquared(other, color);
      if (distanceSquared < closestDistanceSquared) {
        closest = color;
        closestDistanceSquared = distanceSquared;
      }
    }
    return closest;
  }

  private static double calculateDistanceSquared(final Color other, final Color color) {
    final int otherR = other.getRed();
    final int otherG = other.getGreen();
    final int otherB = other.getBlue();
    final int r = color.getRed();
    final int g = color.getGreen();
    final int b = color.getBlue();
    final int dr = otherR - r;
    final int dg = otherG - g;
    final int db = otherB - b;
    return dr * dr + dg * dg + db * db;
  }
}
