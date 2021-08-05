package io.github.pulsebeat02.ezmediacore.dimension;

public class Dimension {

  public static final ImmutableDimension X360_640;
  public static final ImmutableDimension X375_667;
  public static final ImmutableDimension X414_896;
  public static final ImmutableDimension X360_780;
  public static final ImmutableDimension X375_812;

  public static final ImmutableDimension X1366_768;
  public static final ImmutableDimension X1920_1080;
  public static final ImmutableDimension X1536_864;
  public static final ImmutableDimension X1440_900;
  public static final ImmutableDimension X1280_720;
  public static final ImmutableDimension X3840_2160;

  static {
    X360_640 = of(360, 640);
    X375_667 = of(375, 667);
    X414_896 = of(414, 896);
    X360_780 = of(360, 780);
    X375_812 = of(375, 812);

    X1366_768 = of(1366, 768);
    X1920_1080 = of(1920, 1080);
    X1536_864 = of(1536, 864);
    X1440_900 = of(1440, 900);
    X1280_720 = of(1280, 720);
    X3840_2160 = of(3840, 2160);
  }

  public static ImmutableDimension of(final int width, final int height) {
    return new ImmutableDimension(width, height);
  }
}
