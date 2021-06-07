/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.deluxemediaplugin.config;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.minecraftmedialibrary.http.HttpDaemon;
import io.github.pulsebeat02.minecraftmedialibrary.http.HttpFileDaemonServer;
import io.github.pulsebeat02.minecraftmedialibrary.http.request.ZipHeader;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.hosting.HttpDaemonProvider;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.hosting.HttpServerDaemon;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class HttpConfiguration extends ConfigurationProvider {

  private HttpServerDaemon daemon;
  private boolean enabled;

  public HttpConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, "httpserver.yml");
  }

  @Override
  public void deserialize() {
    final FileConfiguration configuration = getFileConfiguration();
    configuration.set("enabled", enabled);
    configuration.set("port", daemon.getPort());
    final HttpDaemon http = daemon.getDaemon();
    configuration.set(
        "directory",
        getPlugin()
            .getDataFolder()
            .toPath()
            .relativize(http.getDirectory().toAbsolutePath())
            .toString());
    configuration.set(
        "header",
        ((HttpFileDaemonServer) http).getZipHeader() == ZipHeader.ZIP ? "ZIP" : "OCTET_STREAM");
    configuration.set("verbose", http.isVerbose());
    saveConfig();
  }

  @Override
  public void serialize() {
    final FileConfiguration configuration = getFileConfiguration();
    final boolean enabled = configuration.getBoolean("enabled");
    final String ip = configuration.getString("ip");
    final int port = configuration.getInt("port");
    final String directory =
        String.format(
            "%s/%s",
            getPlugin().getDataFolder().getAbsolutePath(), configuration.getString("directory"));
    final String header = configuration.getString("header");
    final boolean verbose = configuration.getBoolean("verbose");
    if (enabled) {
      if (ip == null || ip.equals("public")) {
        daemon = new HttpDaemonProvider(directory, port);
      } else {
        daemon = new HttpDaemonProvider(directory, port, ip);
      }
      final HttpFileDaemonServer http = (HttpFileDaemonServer) daemon.getDaemon();
      if (header == null) {
        Logger.info(
            "Invalid Header in httpserver.yml! Can only be ZIP or OCTET-STREAM. Resorting to ZIP.");
      }
      http.setZipHeader(
          header == null || header.equals("ZIP") ? ZipHeader.ZIP : ZipHeader.OCTET_STREAM);
      http.setVerbose(verbose);
      daemon.startServer();
    }
    this.enabled = enabled;
  }

  public HttpServerDaemon getDaemon() {
    return daemon;
  }

  public boolean isEnabled() {
    return enabled;
  }
}
