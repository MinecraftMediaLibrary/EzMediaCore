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
package rewrite.resourcepack.provider;

import rewrite.EzMediaCore;
import rewrite.http.HttpDaemon;
import rewrite.http.netty.NettyServer;
import rewrite.util.io.NetworkUtils;

import java.nio.file.Path;

public final class HttpServer {

  private final HttpDaemon daemon;

  public HttpServer(
       final EzMediaCore core,
       final Path path,
       final String ip,
       final int port,
       final boolean verbose) {
    final String address = ip == null ? NetworkUtils.getPublicAddress() : ip;
    this.daemon = NettyServer.ofDaemon(core, path, address, port, verbose);
  }

  public String createUrl(final String file) {
    return "http://%s:%d/%s"
        .formatted(
            this.daemon.getAddress(),
            this.daemon.getPort(),
            this.daemon.getRelativePath(Path.of(file)));
  }

  public String createUrl(final Path path) {
    return this.createUrl(path.toString());
  }

  public void start() {
    this.daemon.start();
  }

  public void shutdown() {
    this.daemon.stop();
  }
}
