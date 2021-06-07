/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.deluxemediaplugin;

import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandHandler;
import io.github.pulsebeat02.deluxemediaplugin.config.EncoderConfiguration;
import io.github.pulsebeat02.deluxemediaplugin.config.HttpConfiguration;
import io.github.pulsebeat02.deluxemediaplugin.config.PictureConfiguration;
import io.github.pulsebeat02.deluxemediaplugin.config.VideoConfiguration;
import io.github.pulsebeat02.deluxemediaplugin.update.PluginUpdateChecker;
import io.github.pulsebeat02.deluxemediaplugin.utility.CommandUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.MediaLibraryProvider;
import io.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DeluxeMediaPlugin extends JavaPlugin {

  public static boolean OUTDATED = false;

  private MediaLibrary library;
  private BukkitAudiences audiences;
  private CommandHandler handler;
  private Logger logger;
  private HttpConfiguration httpConfiguration;
  private PictureConfiguration pictureConfiguration;
  private VideoConfiguration videoConfiguration;
  private EncoderConfiguration encoderConfiguration;

  @Override
  public void onDisable() {

    logger.info("DeluxeMediaPlugin is Shutting Down");

    logger.info("Shutting Down MinecraftMediaLibrary Instance...");
    if (library != null) {
      library.shutdown();
    } else {
      logger.severe(
          "WARNING: MinecraftMediaLibrary instance is null... something is fishy going on.");
    }

    logger.info("Unregistering Commands");
    if (handler != null) {
      final Set<BaseCommand> cmds = handler.getCommands();
      if (cmds != null) {
        for (final BaseCommand cmd : handler.getCommands()) {
          CommandUtilities.unRegisterBukkitCommand(this, cmd);
        }
      }
    }

    logger.info("DeluxeMediaPlugin Successfully Shutdown");
  }

  @Override
  public void onEnable() {

    // Get the plugin logger
    logger = getLogger();

    if (!OUTDATED) {

      logger.info("DeluxeMediaPlugin is Initializing");
      CommandUtilities.ensureInit();

      logger.info("Loading MinecraftMediaLibrary Instance...");
      library = MediaLibraryProvider.create(this);

      logger.info("Loading Commands...");
      registerCommands();

      logger.info("Loading Configuration Files...");
      registerConfigurations();

      logger.info("Sending Metrics Statistics...");
      new Metrics(this, 10229);

      logger.info("Checking for Updates...");
      new PluginUpdateChecker(this).checkForUpdates();

      audiences = BukkitAudiences.create(this);

      logger.info("Finished Loading Instance and Plugin");

    } else {

      logger.severe("Plugin cannot load until server version is at least 1.8");
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }

  private void registerConfigurations() {

    httpConfiguration = new HttpConfiguration(this);
    pictureConfiguration = new PictureConfiguration(this);
    videoConfiguration = new VideoConfiguration(this);
    encoderConfiguration = new EncoderConfiguration(this);

    httpConfiguration.read();
    pictureConfiguration.read();
    videoConfiguration.read();
    encoderConfiguration.read();
  }

  private Optional<MediaLibrary> setupMediaLibrary() {
    try {
      final Constructor<MinecraftMediaLibrary> constructor =
          MinecraftMediaLibrary.class.getDeclaredConstructor(Plugin.class);
      constructor.setAccessible(true);
      return Optional.of(constructor.newInstance(this));
    } catch (final ReflectiveOperationException exception) {
      getLogger()
          .log(
              Level.SEVERE,
              "Couldn't instantiate media library instance, disabling...",
              new Error(exception));
      return Optional.empty();
    }
  }

  private void registerCommands() {
    handler = new CommandHandler(this);
  }

  public MediaLibrary getLibrary() {
    return library;
  }

  public HttpConfiguration getHttpConfiguration() {
    return httpConfiguration;
  }

  public PictureConfiguration getPictureConfiguration() {
    return pictureConfiguration;
  }

  public VideoConfiguration getVideoConfiguration() {
    return videoConfiguration;
  }

  public EncoderConfiguration getEncoderConfiguration() {
    return encoderConfiguration;
  }

  public BukkitAudiences audience() {
    return audiences;
  }

  public CommandHandler getHandler() {
    return handler;
  }
}
