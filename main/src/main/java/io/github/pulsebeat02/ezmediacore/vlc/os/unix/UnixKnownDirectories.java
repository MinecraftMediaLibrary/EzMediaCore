package io.github.pulsebeat02.ezmediacore.vlc.os.unix;

import com.google.common.collect.ImmutableList;
import io.github.pulsebeat02.ezmediacore.analysis.OSType;
import io.github.pulsebeat02.ezmediacore.vlc.os.WellKnownDirectoryProvider;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class UnixKnownDirectories implements WellKnownDirectoryProvider {
  @Override
  public @NotNull OSType getOperatingSystem() {
    return OSType.UNIX;
  }

  @Override
  public @NotNull List<String> getSearchDirectories() {
    return ImmutableList.of(
        "/usr/lib/x86_64-linux-gnu",
        "/usr/lib64",
        "/usr/local/lib64",
        "/usr/lib/i386-linux-gnu",
        "/usr/lib",
        "/usr/local/lib");
  }
}
