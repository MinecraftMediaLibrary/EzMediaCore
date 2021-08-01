package io.github.pulsebeat02.ezmediacore.vlc.os;

import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public interface VLCBinaryInstallation extends HostOperator {

  void downloadBinaries() throws IOException, InterruptedException;

  void loadNativeBinaries() throws IOException;

  @NotNull
  Path getInstallationPath();
}
