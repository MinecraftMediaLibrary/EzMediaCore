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
package io.github.pulsebeat02.ezmediacore.analysis;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.locale.Locale;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class SystemDiagnostics implements Diagnostic {

  private final MediaLibraryCore core;
  private final OperatingSystem system;
  private final CPUArchitecture cpu;

  public SystemDiagnostics(@NotNull final MediaLibraryCore core) {
    this.core = core;
    this.system = new OperatingSystem();
    this.cpu = new CPUArchitecture();
  }

  @Override
  public void debugInformation() {
    this.logPluginInfo();
    this.logServerInfo();
    this.logSystemInfo();
  }

  private void logPluginInfo() {
    this.core.getLogger().info(this.getPluginInfoMessage());
  }

  private @NotNull String getPluginInfoMessage() {
    final Plugin plugin = this.core.getPlugin();
    return Locale.PLUGIN_INFO.build(
        plugin.getName(),
        plugin.getDescription().getDescription(),
        this.core.isDisabled(),
        this.core.getLibraryPath(),
        this.core.getVlcPath(),
        this.core.getImagePath(),
        this.core.getAudioPath());
  }

  private void logServerInfo() {
    this.core.getLogger().info(this.getServerInfoMessage());
  }

  private @NotNull String getServerInfoMessage() {
    final Server server = this.core.getPlugin().getServer();
    return Locale.SERVER_INFO.build(server.getName(), server.getVersion(), server.getOnlineMode());
  }

  private void logSystemInfo() {
    this.core.getLogger().info(this.getSystemInfoMessage());
  }

  private @NotNull String getSystemInfoMessage() {
    return Locale.SYSTEM_INFO.build(
        this.system.getOSName(),
        this.system.getVersion(),
        this.system.getLinuxDistribution(),
        this.cpu.getArchitecture());
  }

  @Override
  public @NotNull OperatingSystemInfo getSystem() {
    return this.system;
  }

  @Override
  public @NotNull CpuInfo getCpu() {
    return this.cpu;
  }
}
