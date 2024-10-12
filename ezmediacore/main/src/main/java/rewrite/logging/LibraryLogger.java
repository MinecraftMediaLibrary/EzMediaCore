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
package rewrite.logging;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;

import java.io.IOException;
import java.nio.file.Path;

public final class LibraryLogger implements Logger {

  private final FileLogger emcLogger;
  private final FileLogger vlcLogger;
  private final FileLogger rtpLogger;
  private final FileLogger ffmpegPlayerLogger;
  private final FileLogger ffmpegStreamLogger;

  public LibraryLogger(final EzMediaCore core) {
    final Path path = core.getLibraryPath();
    this.emcLogger = new FileLogger(path.resolve("emc.log"));
    this.vlcLogger = new FileLogger(path.resolve("vlc.log"));
    this.rtpLogger = new FileLogger(path.resolve("rtp.log"));
    this.ffmpegPlayerLogger = new FileLogger(path.resolve("ffmpeg-player.log"));
    this.ffmpegStreamLogger = new FileLogger(path.resolve("ffmpeg-stream.log"));
  }

  @Override
  public void start() throws IOException {
    this.emcLogger.start();
    this.vlcLogger.start();
    this.rtpLogger.start();
    this.ffmpegPlayerLogger.start();
    this.ffmpegStreamLogger.start();
  }

  @Override
  public void info( final Object info) {
    this.directPrint("%d: [INFO] %s".formatted(System.currentTimeMillis(), info));
  }

  @Override
  public void warn( final Object warning) {
    this.directPrint("%d: [WARN] %s".formatted(System.currentTimeMillis(), warning));
  }

  @Override
  public void error( final Object error) {
    this.directPrint("%d: [ERROR] %s".formatted(System.currentTimeMillis(), error));
  }

  @Override
  public void vlc( final String line) {
    this.vlcLogger.printLine(line);
  }

  @Override
  public void ffmpegPlayer( final String line) {
    this.ffmpegPlayerLogger.printLine(line);
  }

  @Override
  public void ffmpegStream( final String line) {
    this.ffmpegStreamLogger.printLine(line);
  }

  @Override
  public void rtp( final String line) {
    this.rtpLogger.printLine(line);
  }

  @Override
  public void directPrint( final String line) {
    this.emcLogger.printLine(line);
  }

  @Override
  public void close() {
    this.emcLogger.close();
    this.vlcLogger.close();
    this.rtpLogger.close();
    this.ffmpegPlayerLogger.close();
    this.ffmpegStreamLogger.close();
  }
}
