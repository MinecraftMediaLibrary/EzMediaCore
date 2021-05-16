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

package com.github.pulsebeat02.deluxemediaplugin;

import com.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import com.github.pulsebeat02.deluxemediaplugin.command.CommandHandler;
import com.github.pulsebeat02.deluxemediaplugin.config.EncoderConfiguration;
import com.github.pulsebeat02.deluxemediaplugin.config.HttpConfiguration;
import com.github.pulsebeat02.deluxemediaplugin.config.PictureConfiguration;
import com.github.pulsebeat02.deluxemediaplugin.config.VideoConfiguration;
import com.github.pulsebeat02.deluxemediaplugin.update.PluginUpdateChecker;
import com.github.pulsebeat02.deluxemediaplugin.utility.CommandUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class DeluxeMediaPlugin extends JavaPlugin {

  public static boolean OUTDATED = false;

  private MinecraftMediaLibrary library;
  private BukkitAudiences audiences;
  private CommandHandler handler;
  private Logger logger;
  private HttpConfiguration httpConfiguration;
  private PictureConfiguration pictureConfiguration;
  private VideoConfiguration videoConfiguration;
  private EncoderConfiguration encoderConfiguration;

  @Override
  public void onEnable() {

    // Get the plugin logger
    logger = getLogger();

    if (!OUTDATED) {
      logger.info("DeluxeMediaPlugin is Initializing");
      CommandUtilities.ensureInit();

      logger.info("Loading MinecraftMediaLibrary Instance...");

      // Define a new MinecraftMediaLibrary Instance
      library = new MinecraftMediaLibrary(this);

      com.github.pulsebeat02.minecraftmedialibrary.logger.Logger.setVerbose(true);

      registerCommands();
      registerConfigurations();
      new Metrics(this, 10229);
      new PluginUpdateChecker(this).checkForUpdates();
      audiences = BukkitAudiences.create(this);
      logger.info("Finished Loading Instance and Plugin");
    } else {
      logger.severe("Plugin cannot load until server version is at least 1.8");
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }

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
    for (final BaseCommand cmd : handler.getCommands()) {
      CommandUtilities.unRegisterBukkitCommand(this, cmd);
    }

    logger.info("Enclosing MinecraftMediaLibrary and Plugin Successfully Shutdown");
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

  private void registerCommands() {
    handler = new CommandHandler(this);
  }

  public MinecraftMediaLibrary getLibrary() {
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

  public BukkitAudiences getAudiences() {
    return audiences;
  }

  public CommandHandler getHandler() {
    return handler;
  }
}
