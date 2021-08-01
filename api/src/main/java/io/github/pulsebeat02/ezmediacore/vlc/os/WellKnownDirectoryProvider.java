package io.github.pulsebeat02.ezmediacore.vlc.os;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface WellKnownDirectoryProvider extends HostOperator {

  @NotNull
  List<String> getSearchDirectories();
}
