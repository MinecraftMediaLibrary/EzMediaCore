package io.github.pulsebeat02.deluxemediaplugin;

import io.github.slimjar.app.builder.ApplicationBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeluxeMediaPluginBootstrap extends JavaPlugin {

  private DeluxeMediaPlugin plugin;

  @Override
  public void onLoad() {
    final Logger logger = this.getLogger();
    logger.info("Loading DeluxeMediaPlugin dependencies... this may take a minute!");
    try {
      ApplicationBuilder.appending("DeluxeMediaPlugin").build();
    } catch (final IOException
        | NoSuchAlgorithmException
        | URISyntaxException
        | ReflectiveOperationException e) {
      e.printStackTrace();
    }
    logger.info("Finished loading DeluxeMediaPlugin dependencies!");
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
