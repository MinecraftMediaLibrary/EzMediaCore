package io.github.pulsebeat02.ezmediacore.dither.palette;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class DefaultPalette extends ColorPalette {

  public DefaultPalette() {
    super(getPaletteColors());
  }

  private static List<Integer> getPaletteColors() {
    final List<Integer> colors = new ArrayList<>();
    for (int i = 0; i < 256; ++i) {
      try {
        final byte index = (byte) i;
        final Color color = MapPalette.getColor(index);
        final int rgb = color.getRGB();
        colors.add(rgb);
      } catch (final IndexOutOfBoundsException e) {
        break;
      }
    }
    return colors;
  }
}
