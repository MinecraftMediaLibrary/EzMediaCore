package com.github.pulsebeat02.minecraftmedialibrary.vlc.os.windows;

import com.github.pulsebeat02.minecraftmedialibrary.vlc.os.SilentInstallationType;
import com.github.pulsebeat02.minecraftmedialibrary.vlc.os.SilentOSDependentSolution;

public interface WindowsSilentInstallation extends SilentOSDependentSolution {
  String getVlcPath();

  @Override
  default SilentInstallationType getType() {
    return SilentInstallationType.WINDOWS;
  }
}
