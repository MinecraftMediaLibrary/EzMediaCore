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
import io.github.pulsebeat02.ezmediacore.http.HttpDaemon;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpDaemonSolution;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class HttpConfiguration extends ConfigurationProvider {

  private HttpDaemonSolution daemon;
  private boolean enabled;

  public HttpConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, "configuration/httpserver.yml");
  }

  @Override
  public void deserialize() {
    final FileConfiguration configuration = getFileConfiguration();
    configuration.set("enabled", enabled);
    configuration.set("port", daemon.getDaemon().getPort());
    final HttpDaemon http = daemon.getDaemon();
    configuration.set(
        "directory",
        getPlugin()
            .getDataFolder()
            .toPath()
            .relativize(http.getServerPath().toAbsolutePath())
            .toString());
    configuration.set("verbose", http.isVerbose());
    saveConfig();
  }

  @Override
  public void serialize() throws IOException {
    final FileConfiguration configuration = getFileConfiguration();
    final boolean enabled = configuration.getBoolean("enabled");
    final String ip = configuration.getString("ip");
    final int port = configuration.getInt("port");
    final String directory =
        String.format(
            "%s/%s",
            getPlugin().getDataFolder().getAbsolutePath(), configuration.getString("directory"));
    final boolean verbose = configuration.getBoolean("verbose");
    if (enabled) {
      daemon =
          ip == null || ip.equals("public")
              ? new HttpServer(directory, port)
              : new HttpServer(directory, ip, port, verbose);
      daemon.startServer();
    }
    this.enabled = enabled;
  }

  public HttpDaemonSolution getServer() {
    return daemon;
  }

  public boolean isEnabled() {
    return enabled;
  }
}
