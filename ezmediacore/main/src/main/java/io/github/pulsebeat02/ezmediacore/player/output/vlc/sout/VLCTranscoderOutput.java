/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.player.output.vlc.sout;

import io.github.pulsebeat02.ezmediacore.player.output.OutputConfiguration;
import java.util.Map;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class VLCTranscoderOutput extends OutputConfiguration {

  public static final String VENC;
  public static final String VCODEC;
  public static final String VB;
  public static final String SCALE;
  public static final String FPS;
  public static final String DEINTERLACE;
  public static final String DEINTERLACE_MODULE;
  public static final String WIDTH;
  public static final String HEIGHT;
  public static final String MAX_WIDTH;
  public static final String MAX_HEIGHT;
  public static final String VFILTER;

  public static final String AENC;
  public static final String ACODEC;
  public static final String AB;
  public static final String ALANG;
  public static final String CHANNELS;
  public static final String SAMPLERATE;
  public static final String AFILTER;

  public static final String SENC;
  public static final String SCODEC;
  public static final String SOVERLAY;
  public static final String SFILTER;

  public static final String THREADS;
  public static final String POOL_SIZE;
  public static final String HIGH_PRIORITY;

  static {
    VENC = "venc";
    VCODEC = "vcodec";
    VB = "vb";
    SCALE = "scale";
    FPS = "fps";
    DEINTERLACE = "deinterlace";
    DEINTERLACE_MODULE = "deinterlace-module";
    WIDTH = "width";
    HEIGHT = "height";
    MAX_WIDTH = "max-width";
    MAX_HEIGHT = "max-height";
    VFILTER = "vfilter";

    AENC = "aenc";
    ACODEC = "acodec";
    AB = "ab";
    ALANG = "alang";
    CHANNELS = "channels";
    SAMPLERATE = "samplerate";
    AFILTER = "afilter";

    SENC = "senc";
    SCODEC = "scodec";
    SOVERLAY = "soverlay";
    SFILTER = "sfilter";

    THREADS = "threads";
    POOL_SIZE = "pool-size";
    HIGH_PRIORITY = "high-priority";
  }

  VLCTranscoderOutput() {}

  @Contract(" -> new")
  public static @NotNull VLCTranscoderOutput ofOutput() {
    return new VLCTranscoderOutput();
  }

  @Override
  public @NotNull String toString() {
    final Map<String, String> configuration = this.getConfiguration();
    final StringBuilder builder = new StringBuilder("transcode{");
    for (final Map.Entry<String, String> entry : configuration.entrySet()) {
      builder.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
    }
    builder.deleteCharAt(builder.length() - 1);
    builder.append("}");
    return builder.toString();
  }
}
