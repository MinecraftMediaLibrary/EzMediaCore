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
package io.github.pulsebeat02.ezmediacore.rtsp;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.capabilities.Capabilities;
import io.github.pulsebeat02.ezmediacore.capabilities.RTSPCapability;
import io.github.pulsebeat02.ezmediacore.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Map;

public class RTSPServer {

  private final EzMediaCore core;
  private final int hlsPort;

  private Process process;

  public RTSPServer(final EzMediaCore core, final int hlsPort) {
    this.core = core;
    this.hlsPort = hlsPort;
  }

  public void start() {
    final ProcessBuilder builder = this.createProcessBuilder();
    this.configureEnvironment(builder);
    this.startServer(builder);
  }

  public void shutdown() {
    if (this.process != null) {
      this.process.destroy();
    }
  }

  private ProcessBuilder createProcessBuilder() {
    final RTSPCapability capability = Capabilities.RTSP;
    final Path path = capability.getBinaryPath();
    final String raw = path.toString();
    return new ProcessBuilder(raw).redirectErrorStream(true);
  }

  private void startServer(final ProcessBuilder builder) {
    try {
      this.process = builder.start();
      this.handleLogging();
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private void configureEnvironment(final ProcessBuilder builder) {
    final String key = "RTSP_HLSADDRESS";
    final String value = ":%s".formatted(this.hlsPort);
    final Map<String, String> env = builder.environment();
    env.put(key, value);
  }

  private void handleLogging()
          throws IOException {
    final Logger log = this.core.getLogger();
    try (final InputStream stream = this.process.getInputStream();
         final InputStreamReader reader = new InputStreamReader(stream);
         final BufferedReader r = new BufferedReader(reader)) {
      String line;
      while ((line = r.readLine()) != null) {
        log.rtp(line);
      }
    }
  }
}
