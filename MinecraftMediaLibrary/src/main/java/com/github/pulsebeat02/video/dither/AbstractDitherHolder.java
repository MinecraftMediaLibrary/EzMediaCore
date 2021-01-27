package com.github.pulsebeat02.video.dither;

import java.nio.ByteBuffer;

public interface AbstractDitherHolder {

    void dither(int[] buffer, int width);

    ByteBuffer ditherIntoMinecraft(int[] buffer, int width);

}
