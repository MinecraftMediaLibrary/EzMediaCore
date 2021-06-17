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
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.TextComponent.ofChildren;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

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

      audiences = BukkitAudiences.create(this);
      final Audience console = audiences.console();

      console.sendMessage(format(text("Started to Initialize DeluxeMediaPlugin...")));

      final List<String> logo =
          Arrays.asList(
              " _____       _                __  __          _ _       _____  _             _       ",
              " |  __ \\     | |              |  \\/  |        | (_)     |  __ \\| |           (_)      ",
              " | |  | | ___| |_   ___  _____| \\  / | ___  __| |_  __ _| |__) | |_   _  __ _ _ _ __  ",
              " | |  | |/ _ \\ | | | \\ \\/ / _ \\ |\\/| |/ _ \\/ _` | |/ _` |  ___/| | | | |/ _` | | '_ \\ ",
              " | |__| |  __/ | |_| |>  <  __/ |  | |  __/ (_| | | (_| | |    | | |_| | (_| | | | | |",
              " |_____/ \\___|_|\\__,_/_/\\_\\___|_|  |_|\\___|\\__,_|_|\\__,_|_|    |_|\\__,_|\\__, |_|_| |_|",
              "                                                                         __/ |        ",
              "                                                                        |___/         ");
      for (final String line : logo) {
        console.sendMessage(text(line, BLUE));
      }
      console.sendMessage(
          format(
              ofChildren(text("Running DeluxeMediaPlugin ", AQUA), text("[CLOSED BETA]", GOLD))));

      CommandUtilities.ensureInit();

      console.sendMessage(format(text("Loading MinecraftMediaLibrary Instance...")));
      library = MediaLibraryProvider.create(this);

      console.sendMessage(format(text("Loading Commands...")));
      registerCommands();

      console.sendMessage(format(text("Loading Configuration Files...")));
      registerConfigurations();

      console.sendMessage(format(text("Sending Metrics Statistics...")));
      new Metrics(this, 10229);

      console.sendMessage(format(text("Checking for Updates...")));
      new PluginUpdateChecker(this).checkForUpdates();

      console.sendMessage(format(text("Finished Loading Instance and Plugin")));

      console.sendMessage(
          format(
              text(
                  "Hello %%__USER__%%! Thank you for purchasing DeluxeMediaPlugin. For "
                      + "identifier purposes, this is your purchase id: %%__NONCE__%%")));

    } else {

      logger.severe("Plugin cannot load unless server version is at least 1.8");
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

  private void registerCommands() {
    handler = new CommandHandler(this);
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

  public MediaLibrary library() {
    return library;
  }

  public BukkitAudiences audience() {
    return audiences;
  }

  public CommandHandler getHandler() {
    return handler;
  }
}
