package com.github.pulsebeat02.minecraftmedialibrary.video.dither;

import java.nio.ByteBuffer;

public interface AbstractDitherHolder {

    void dither(final int[] buffer, final int width);

    ByteBuffer ditherIntoMinecraft(final int[] buffer, final int width);

    DitherSetting getSetting();

}
