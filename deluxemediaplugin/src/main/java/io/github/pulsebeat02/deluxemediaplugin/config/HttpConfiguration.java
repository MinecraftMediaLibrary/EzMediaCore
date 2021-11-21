/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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

package io.github.pulsebeat02.deluxemediaplugin.config;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.http.HttpDaemon;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import java.io.IOException;
import java.nio.file.Path;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public final class HttpConfiguration extends ConfigurationProvider<HttpServer> {

  private HttpServer daemon;
  private boolean enabled;

  public HttpConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, "configuration/httpserver.yml");
  }

  @Override
  public void deserialize() throws IOException {
    final FileConfiguration configuration = this.getFileConfiguration();
    configuration.set("enabled", this.enabled);
    configuration.set("port", this.daemon.getDaemon().getPort());
    final HttpDaemon http = this.daemon.getDaemon();
    configuration.set(
        "directory",
        this.getPlugin()
            .getBootstrap()
            .getDataFolder()
            .toPath()
            .relativize(http.getServerPath().toAbsolutePath())
            .toString());
    configuration.set("verbose", http.isVerbose());
    this.saveConfig();
  }

  @Override
  public HttpServer serialize() throws IOException {
    final FileConfiguration configuration = this.getFileConfiguration();
    final boolean enabled = configuration.getBoolean("enabled");
    final String ip = configuration.getString("ip");
    final int port = configuration.getInt("port");
    final Path directory =
        Path.of(
            this.getPlugin().getBootstrap().getDataFolder().getAbsolutePath(),
            configuration.getString("directory"));
    final boolean verbose = configuration.getBoolean("verbose");
    final MediaLibraryCore core = this.getPlugin().library();
    if (enabled) {
      this.daemon =
          ip == null || ip.equals("public")
              ? HttpServer.ofServer(core, directory, port)
              : HttpServer.ofServer(core, directory, ip, port, verbose);
    }
    this.enabled = enabled;
    return this.daemon;
  }

  public boolean isEnabled() {
    return this.enabled;
  }
}
