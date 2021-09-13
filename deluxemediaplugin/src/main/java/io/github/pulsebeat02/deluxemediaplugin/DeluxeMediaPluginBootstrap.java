package io.github.pulsebeat02.deluxemediaplugin;

import io.github.slimjar.app.builder.ApplicationBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeluxeMediaPluginBootstrap extends JavaPlugin {

  private DeluxeMediaPlugin plugin;

  @Override
  public void onLoad() {
    try {
      ApplicationBuilder.appending("DeluxeMediaPlugin").build();
    } catch (final IOException
        | NoSuchAlgorithmException
        | URISyntaxException
        | ReflectiveOperationException e) {
      e.printStackTrace();
    }
    this.plugin = new DeluxeMediaPlugin(this);
    this.plugin.load();
  }

  @Override
  public void onEnable() {
    this.plugin.enable();
  }

  @Override
  public void onDisable() {
    this.plugin.disable();
  }
}
