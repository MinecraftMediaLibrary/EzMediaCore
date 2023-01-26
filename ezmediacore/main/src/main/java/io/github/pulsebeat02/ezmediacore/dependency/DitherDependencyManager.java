package io.github.pulsebeat02.ezmediacore.dependency;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.pulsebeat02.emcdependencymanagement.component.Repository;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.json.GsonProvider;
import io.github.pulsebeat02.ezmediacore.utility.io.ResourceUtils;
import io.github.pulsebeat02.nativelibraryloader.NativeLibraryLoader;
import io.github.pulsebeat02.nativelibraryloader.os.Arch;
import io.github.pulsebeat02.nativelibraryloader.os.Bits;
import io.github.pulsebeat02.nativelibraryloader.os.OS;
import io.github.pulsebeat02.nativelibraryloader.os.Platform;
import io.github.pulsebeat02.nativelibraryloader.strategy.LibraryLocation;
import io.github.pulsebeat02.nativelibraryloader.strategy.implementation.NativeResourceLocator;
import io.github.pulsebeat02.nativelibraryloader.strategy.implementation.ResourceLocator;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import io.github.pulsebeat02.nativelibraryloader.strategy.implementation.UrlResourceNativeLibrary;
import org.jetbrains.annotations.NotNull;

public final class DitherDependencyManager extends LibraryDependency {

  public static final Map<Platform, NativeResourceLocator> NATIVE_LIBRARY_MAP;

  static {
    final Gson gson = GsonProvider.getSimple();
    try (final Reader reader =
        ResourceUtils.getResourceAsInputStream("/emc-json/dither/binaries.json")) {
      final TypeToken<Map<Platform, UrlResourceNativeLibrary>> token = new TypeToken<>() {};
      NATIVE_LIBRARY_MAP = gson.fromJson(reader, token.getType());
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
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
