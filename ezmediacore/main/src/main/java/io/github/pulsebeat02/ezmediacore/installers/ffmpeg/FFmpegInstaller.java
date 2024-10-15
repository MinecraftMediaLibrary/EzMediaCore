/**
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
package io.github.pulsebeat02.ezmediacore.installers.ffmpeg;

import com.google.common.collect.Table;
import io.github.pulsebeat02.ezmediacore.installers.BaseInstaller;
import io.github.pulsebeat02.ezmediacore.util.io.ResourceUtils;
import io.github.pulsebeat02.ezmediacore.util.os.OS;

import java.nio.file.Path;

/** FFmpeg installer class. */
public final class FFmpegInstaller extends BaseInstaller {
  private static final Table<OS, Boolean, String> BITS_64;
  private static final Table<OS, Boolean, String> BITS_32;

  static {
    BITS_64 = ResourceUtils.parseTable("/installers/ffmpeg/bits64.json");
    BITS_32 = ResourceUtils.parseTable("/installers/ffmpeg/bits32.json");
  }

  FFmpegInstaller(final Path folder) {
    super(folder, "ffmpeg", BITS_32, BITS_64);
  }

  FFmpegInstaller() {
    super("ffmpeg", BITS_32, BITS_64);
  }

  /**
   * Constructs a new FFmpegInstaller with the specified directory for the executable.
   *
   * @param executable directory
   * @return new FFmpegInstaller
   */
  public static FFmpegInstaller create(final Path executable) {
    return new FFmpegInstaller(executable);
  }

  /**
   * Constructs a new FFmpegInstaller with the default directory for the executable.
   *
   * <p>For Windows, it is C:/Program Files/static-emc Otherwise, it is [user home
   * directory]/static-emc
   *
   * @return new FFmpegInstaller
   */
  public static FFmpegInstaller create() {
    return new FFmpegInstaller();
  }

  @Override
  public boolean isSupported() {
    return true;
  }
}
