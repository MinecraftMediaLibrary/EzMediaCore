package rewrite.dither.load;

import rewrite.dither.MapPalette;

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
        final Color color = MapPalette.getColor((byte) i);
        colors.add(color.getRGB());
      } catch (final IndexOutOfBoundsException e) {
        break;
      }
    }
    return colors;
  }
}
