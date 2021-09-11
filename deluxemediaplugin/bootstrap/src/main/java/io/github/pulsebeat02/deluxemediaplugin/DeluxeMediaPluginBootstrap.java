package io.github.pulsebeat02.deluxemediaplugin;

import io.github.slimjar.app.builder.ApplicationBuilder;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeluxeMediaPluginBootstrap extends JavaPlugin {

  private static MethodHandle LOAD;
  private static MethodHandle ENABLE;
  private static MethodHandle DISABLE;

  static {
    try {
      final Lookup lookup = MethodHandles.publicLookup();
      final MethodType none = MethodType.methodType(void.class);
      final Class<?> clazz =
          Class.forName("io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin");
      LOAD = lookup.findVirtual(clazz, "load", none);
      ENABLE = lookup.findVirtual(clazz, "enable", none);
      DISABLE = lookup.findVirtual(clazz, "disable", none);
    } catch (final ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  static {
    try {
      ApplicationBuilder.appending("DeluxeMediaPlugin").build();
    } catch (final IOException
        | NoSuchAlgorithmException
        | URISyntaxException
        | ReflectiveOperationException e) {
      e.printStackTrace();
    }
  }

  private DeluxeMediaPlugin plugin;

  @Override
  public void onLoad() {
    this.plugin = new DeluxeMediaPlugin(this);
    try {
      LOAD.invoke(this.plugin);
    } catch (final Throwable e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onEnable() {
    this.plugin.enable();
    try {
      ENABLE.invoke(this.plugin);
    } catch (final Throwable e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onDisable() {
    this.plugin.disable();
    try {
      DISABLE.invoke(this.plugin);
    } catch (final Throwable e) {
      e.printStackTrace();
    }
  }
}
