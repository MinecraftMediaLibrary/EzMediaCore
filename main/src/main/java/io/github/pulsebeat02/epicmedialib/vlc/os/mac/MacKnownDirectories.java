package io.github.pulsebeat02.epicmedialib.vlc.os.mac;

import com.google.common.collect.ImmutableSet;
import io.github.pulsebeat02.epicmedialib.analysis.OSType;
import io.github.pulsebeat02.epicmedialib.vlc.os.WellKnownDirectoryProvider;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class MacKnownDirectories implements WellKnownDirectoryProvider {
  @Override
  public @NotNull OSType getOperatingSystem() {
    return OSType.MAC;
  }

  @Override
  public @NotNull Collection<String> getSearchDirectories() {
    return ImmutableSet.of(
        "/Applications/VLC.app/Contents/Frameworks", "/Applications/VLC.app/Contents/MacOS/lib");
  }
}
