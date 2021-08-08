package io.github.pulsebeat02.ezmediacore.vlc;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.vlc.os.SilentInstallationProvider;
import io.github.pulsebeat02.ezmediacore.vlc.os.mac.SilentMacInstallation;
import io.github.pulsebeat02.ezmediacore.vlc.os.unix.SilentUnixInstallation;
import io.github.pulsebeat02.ezmediacore.vlc.os.window.SilentWindowsInstallation;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public class NativeBinaryInstaller implements BinaryInstaller {

  private final MediaLibraryCore core;
  private final SilentInstallationProvider provider;

  public NativeBinaryInstaller(@NotNull final MediaLibraryCore core) {
    this.core = core;
    this.provider = this.getInstallation();
  }

  public NativeBinaryInstaller(
      @NotNull final MediaLibraryCore core, @NotNull final SilentInstallationProvider provider) {
    this.core = core;
    this.provider = provider;
  }

  private SilentInstallationProvider getInstallation() {
    final Path path = this.core.getVlcPath();
    return switch (this.core.getDiagnostics().getSystem().getOSType()) {
      case MAC -> new SilentMacInstallation(this.core, path);
      case UNIX -> new SilentUnixInstallation(this.core, path);
      case WINDOWS -> new SilentWindowsInstallation(this.core, path);
    };
  }

  @Override
  public @NotNull MediaLibraryCore core() {
    return this.core;
  }

  @Override
  public @NotNull SilentInstallationProvider getProvider() {
    return this.provider;
  }

  @Override
  public void download() throws IOException, InterruptedException {
    this.provider.downloadBinaries();
  }
}
