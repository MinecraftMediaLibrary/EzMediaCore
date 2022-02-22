package io.github.pulsebeat02.ezmediacore.dependency;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.nativelibraryloader.NativeLibraryLoader;
import io.github.pulsebeat02.nativelibraryloader.os.Arch;
import io.github.pulsebeat02.nativelibraryloader.os.Bits;
import io.github.pulsebeat02.nativelibraryloader.os.OS;
import io.github.pulsebeat02.nativelibraryloader.strategy.LibraryLocation;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public final class DitherDependencyManager extends LibraryDependency {

  public DitherDependencyManager(@NotNull final MediaLibraryCore core) throws IOException {
    super(core);
  }

  @Override
  public void start() throws IOException {
    final NativeLibraryLoader loader =
        NativeLibraryLoader.builder()
            .addNativeLibrary(
                OS.OSX,
                Arch.NOT_ARM,
                Bits.BITS_64,
                LibraryLocation.URL_RESOURCE.create(
                    "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/darwin/amd64/libdither.dylib"))
            .addNativeLibrary(
                OS.OSX,
                Arch.IS_ARM,
                Bits.BITS_64,
                LibraryLocation.URL_RESOURCE.create(
                    "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/darwin/arm64/libdither.dylib"))
            .addNativeLibrary(
                OS.WIN,
                Arch.NOT_ARM,
                Bits.BITS_64,
                LibraryLocation.URL_RESOURCE.create(
                    "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/windows/amd64/dither.dll"))
            .addNativeLibrary(
                OS.WIN,
                Arch.NOT_ARM,
                Bits.BITS_32,
                LibraryLocation.URL_RESOURCE.create(
                    "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/windows/i386/dither.dll"))
            .addNativeLibrary(
                OS.UNIX,
                Arch.NOT_ARM,
                Bits.BITS_64,
                LibraryLocation.URL_RESOURCE.create(
                    "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/linux/amd64/libdither.so"))
            .addNativeLibrary(
                OS.UNIX,
                Arch.IS_ARM,
                Bits.BITS_32,
                LibraryLocation.URL_RESOURCE.create(
                    "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/linux/arm/libdither.so"))
            .addNativeLibrary(
                OS.UNIX,
                Arch.IS_ARM,
                Bits.BITS_64,
                LibraryLocation.URL_RESOURCE.create(
                    "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/linux/arm64/libdither.so"))
            .addNativeLibrary(
                OS.UNIX,
                Arch.NOT_ARM,
                Bits.BITS_32,
                LibraryLocation.URL_RESOURCE.create(
                    "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/linux/i386/libdither.so"))
            .addNativeLibrary(
                OS.FREEBSD,
                Arch.NOT_ARM,
                Bits.BITS_64,
                LibraryLocation.URL_RESOURCE.create(
                    "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/freebsd/amd64/libdither.so"))
            .build();
    try {
      loader.load(true);
    } catch (final UnsatisfiedLinkError ignored) { // suppress because native libraries aren't supported
    }
  }

  @Override
  public void onInstallation(@NotNull final Path path) {
  }
}
