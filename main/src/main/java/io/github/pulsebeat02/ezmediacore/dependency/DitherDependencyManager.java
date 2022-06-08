package io.github.pulsebeat02.ezmediacore.dependency;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.nativelibraryloader.NativeLibraryLoader;
import io.github.pulsebeat02.nativelibraryloader.os.Arch;
import io.github.pulsebeat02.nativelibraryloader.os.Bits;
import io.github.pulsebeat02.nativelibraryloader.os.OS;
import io.github.pulsebeat02.nativelibraryloader.os.Platform;
import io.github.pulsebeat02.nativelibraryloader.strategy.LibraryLocation;
import io.github.pulsebeat02.nativelibraryloader.strategy.implementation.ResourceLocator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class DitherDependencyManager extends LibraryDependency {

  private static final Map<Platform, ResourceLocator> NATIVE_LIBRARY_MAP;

  static {
    NATIVE_LIBRARY_MAP =
        Map.of(
            Platform.ofPlatform(OS.OSX, Arch.NOT_ARM, Bits.BITS_64),
            LibraryLocation.URL_RESOURCE.create(
                "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/darwin/amd64/libdither.dylib"),
            Platform.ofPlatform(OS.OSX, Arch.IS_ARM, Bits.BITS_64),
            LibraryLocation.URL_RESOURCE.create(
                "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/darwin/arm64/libdither.dylib"),
            Platform.ofPlatform(OS.WIN, Arch.NOT_ARM, Bits.BITS_64),
            LibraryLocation.URL_RESOURCE.create(
                "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/windows/amd64/dither.dll"),
            Platform.ofPlatform(OS.WIN, Arch.NOT_ARM, Bits.BITS_32),
            LibraryLocation.URL_RESOURCE.create(
                "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/windows/i386/dither.dll"),
            Platform.ofPlatform(OS.UNIX, Arch.NOT_ARM, Bits.BITS_64),
            LibraryLocation.URL_RESOURCE.create(
                "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/linux/amd64/libdither.so"),
            Platform.ofPlatform(OS.UNIX, Arch.IS_ARM, Bits.BITS_32),
            LibraryLocation.URL_RESOURCE.create(
                "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/linux/arm/libdither.so"),
            Platform.ofPlatform(OS.UNIX, Arch.IS_ARM, Bits.BITS_64),
            LibraryLocation.URL_RESOURCE.create(
                "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/linux/arm64/libdither.so"),
            Platform.ofPlatform(OS.UNIX, Arch.NOT_ARM, Bits.BITS_32),
            LibraryLocation.URL_RESOURCE.create(
                "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/linux/i386/libdither.so"),
            Platform.ofPlatform(OS.FREEBSD, Arch.NOT_ARM, Bits.BITS_64),
            LibraryLocation.URL_RESOURCE.create(
                "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go/raw/master/binary/freebsd/amd64/libdither.so"));
  }

  public DitherDependencyManager(@NotNull final MediaLibraryCore core) throws IOException {
    super(core);
  }

  @Override
  public void start() throws IOException {

    final NativeLibraryLoader.Builder builder = NativeLibraryLoader.builder();
    NATIVE_LIBRARY_MAP.forEach(builder::addNativeLibrary);

    try {
      final NativeLibraryLoader loader = builder.build();
      loader.load(true);
    } catch (
        final UnsatisfiedLinkError ignored) { // suppress because native libraries aren't supported
    }
  }

  @Override
  public void onInstallation(@NotNull final Path path) {}
}
