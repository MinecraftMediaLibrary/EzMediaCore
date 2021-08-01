package io.github.pulsebeat02.epicmedialib.vlc;

import io.github.pulsebeat02.epicmedialib.MediaLibraryCore;
import io.github.pulsebeat02.epicmedialib.vlc.os.SilentInstallationProvider;
import io.github.pulsebeat02.epicmedialib.vlc.os.mac.SilentMacInstallation;
import io.github.pulsebeat02.epicmedialib.vlc.os.unix.SilentUnixInstallation;
import io.github.pulsebeat02.epicmedialib.vlc.os.window.SilentWindowsInstallation;
import java.io.IOException;
import java.nio.file.Path;
import org.jcodec.codecs.mjpeg.tools.AssertionException;
import org.jetbrains.annotations.NotNull;

public class NativeBinaryInstaller implements BinaryInstaller {

  private final MediaLibraryCore core;
  private final SilentInstallationProvider provider;

  public NativeBinaryInstaller(@NotNull final MediaLibraryCore core) {
    this.core = core;
    this.provider = getInstallation();
  }

  public NativeBinaryInstaller(
      @NotNull final MediaLibraryCore core, @NotNull final SilentInstallationProvider provider) {
    this.core = core;
    this.provider = provider;
  }

  private SilentInstallationProvider getInstallation() {
    final Path path = this.core.getVlcPath();
    switch (this.core.getDiagnostics().getSystem().getOSType()) {
      case MAC:
        return new SilentMacInstallation(this.core, path);
      case UNIX:
        return new SilentUnixInstallation(this.core, path);
      case WINDOWS:
        return new SilentWindowsInstallation(this.core, path);
    }
    throw new AssertionException("Invalid Operating System!");
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
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
