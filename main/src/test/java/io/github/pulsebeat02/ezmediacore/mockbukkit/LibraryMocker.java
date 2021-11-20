package io.github.pulsebeat02.ezmediacore.mockbukkit;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.utility.io.FileUtils;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public final class LibraryMocker {

  private static final Constructor<?> LIBRARY_CONSTRUCTOR;
  private static final Field FILE_HANDLE;
  private static final Path USER_DIR;

  static {
    try {
      LIBRARY_CONSTRUCTOR =
          Class.forName("io.github.pulsebeat02.ezmediacore.EzMediaCore")
              .getDeclaredConstructors()[0];
      LIBRARY_CONSTRUCTOR.setAccessible(true);
      FILE_HANDLE = JavaPlugin.class.getDeclaredField("dataFolder");
      FILE_HANDLE.setAccessible(true);
      USER_DIR = Path.of(System.getProperty("user.dir"), "mockbukkit");
      FileUtils.createFolderIfNotExists(USER_DIR);
    } catch (final ClassNotFoundException e) {
      throw new AssertionError("EzMediaCore class cannot be found!");
    } catch (final NoSuchFieldException e) {
      throw new AssertionError("Data Folder variable cannot be found in JavaPlugin!");
    } catch (final IOException e) {
      throw new AssertionError("Cannot create mockbukkit folder!");
    }
  }

  @Test
  public void mockLibrary() {
    final MockPlugin plugin = this.createMockPlugin();
    final EzMediaCore core = this.createInstance(plugin);
    core.initialize();
  }

  private @NotNull MockPlugin createMockPlugin() {
    MockBukkit.mock(new MockingServer());
    final MockPlugin plugin = MockBukkit.createMockPlugin("EzMediaCore Testing Plugin");
    this.setPath(plugin);
    return plugin;
  }

  private void setPath(@NotNull final MockPlugin plugin) {
    try {
      FILE_HANDLE.set(plugin, USER_DIR.toFile());
    } catch (final IllegalAccessException e) {
      throw new AssertionError("Could not set data folder field of plugin!");
    }
  }

  private @NotNull EzMediaCore createInstance(@NotNull final MockPlugin plugin) {
    try {
      return (EzMediaCore)
          LIBRARY_CONSTRUCTOR.newInstance(
              plugin, null, null, null, null, null, null, null, null, null);
    } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
      throw new AssertionError("EzMediaCore class cannot be instantiated!");
    }
  }
}
