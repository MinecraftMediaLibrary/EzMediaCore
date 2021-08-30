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
package io.github.pulsebeat02.ezmediacore.vlc.os.window;

import com.sun.jna.NativeLibrary;
import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.analysis.OSType;
import io.github.pulsebeat02.ezmediacore.utility.ArchiveUtils;
import io.github.pulsebeat02.ezmediacore.utility.DependencyUtils;
import io.github.pulsebeat02.ezmediacore.vlc.VLCDownloadPortal;
import io.github.pulsebeat02.ezmediacore.vlc.os.SilentInstallation;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.binding.RuntimeUtil;

public class SilentWindowsInstallation extends SilentInstallation {

  public SilentWindowsInstallation(
      @NotNull final MediaLibraryCore core, @NotNull final Path directory) {
    super(core, directory);
  }

  @Override
  public @NotNull OSType getOperatingSystem() {
    return OSType.WINDOWS;
  }

  @Override
  public void downloadBinaries() throws IOException, InterruptedException {

    Logger.info("No VLC binary found on machine, installing Windows binaries.");

    final Path folder = this.getDirectory();
    final Path archive = folder.resolve("VLC.zip");

    DependencyUtils.downloadFile(archive, this.getCore().getDiagnostics().getVlcUrl());

    Logger.info("Successfully downloaded archived binaries.");

    ArchiveUtils.decompressArchive(archive, folder);
    Logger.info("Successfully extracted archived binaries.");

    this.setInstallationPath(folder.resolve("vlc-%s".formatted(VLCDownloadPortal.VLC_VERSION)));
    this.deleteArchive(archive);
    this.loadNativeBinaries();
  }

  @Override
  public void loadNativeBinaries() throws IOException {
    NativeLibrary.addSearchPath(
        RuntimeUtil.getLibVlcLibraryName(), this.getInstallationPath().toString());
    super.loadNativeBinaries();
  }
}
