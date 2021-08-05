package io.github.pulsebeat02.ezmediacore.vlc.os.mac;

import io.github.pulsebeat02.ezmediacore.analysis.OSType;
import io.github.pulsebeat02.ezmediacore.vlc.os.WellKnownDirectoryProvider;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class MacKnownDirectories implements WellKnownDirectoryProvider {

  @Override
  public @NotNull OSType getOperatingSystem() {
    return OSType.MAC;
  }

  @Override
  public @NotNull List<String> getSearchDirectories() {
    return List.of(
        "/Applications/VLC.app/Contents/Frameworks", "/Applications/VLC.app/Contents/MacOS/lib");
  }
}
