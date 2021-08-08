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

import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.TextComponent.ofChildren;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandHandler;
import io.github.pulsebeat02.deluxemediaplugin.config.EncoderConfiguration;
import io.github.pulsebeat02.deluxemediaplugin.config.HttpConfiguration;
import io.github.pulsebeat02.deluxemediaplugin.config.PersistentPictureManager;
import io.github.pulsebeat02.deluxemediaplugin.update.UpdateChecker;
import io.github.pulsebeat02.deluxemediaplugin.utility.CommandUtils;
import io.github.pulsebeat02.ezmediacore.LibraryProvider;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import io.github.pulsebeat02.ezmediacore.sneaky.ThrowingConsumer;
import io.github.pulsebeat02.ezmediacore.utility.FileUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class DeluxeMediaPlugin extends JavaPlugin {

  private MediaLibraryCore library;
  private BukkitAudiences audiences;
  private CommandHandler handler;
  private Logger logger;

  private PersistentPictureManager manager;
  private AudioConfiguration audioConfiguration;
  private HttpServer server;

  @Override
  public void onEnable() {
    this.logger = this.getLogger();
    this.audiences = BukkitAudiences.create(this);
    this.printLogo();
    this.log(ofChildren(text("Running DeluxeMediaPlugin ", AQUA), text("[CLOSED BETA]", GOLD)));
    try {
      this.library = LibraryProvider.builder().plugin(this).build();
      this.library.initialize();
    } catch (final ExecutionException | InterruptedException e) {
      this.log("There was a severe issue while loading the EzMediaCore instance!");
      e.printStackTrace();
    }
    this.loadPersistentData();
    this.registerCommands();
    this.startMetrics();
    this.checkUpdates();
    this.log("Finished DeluxeMediaPlugin!");
    this.log("""
        Hello %%__USER__%%! Thank you for purchasing DeluxeMediaPlugin. For identifier purposes, this
         is your purchase identification code: %%__NONCE__%% - Enjoy using the plugin, and ask for
         support at my Discord! (https://discord.gg/MgqRKvycMC)
        """);
  }

  private void startMetrics() {
    new Metrics(this, 10229);
  }

  private void checkUpdates() {
    new UpdateChecker(this).check();
  }

  private void printLogo() {
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
      this.audiences.console().sendMessage(text(line, BLUE));
    }
  }

  @Override
  public void onDisable() {
    this.log("DeluxeMediaPlugin is Shutting Down");
    if (this.library != null) {
      this.library.shutdown();
    } else {
      this.logger.severe("[ERROR]: EzMediaCore instance is null... something is fishy going on.");
    }
    if (this.handler != null) {
      final Set<BaseCommand> cmds = this.handler.getCommands();
      if (cmds != null) {
        for (final BaseCommand cmd : this.handler.getCommands()) {
          CommandUtils.unRegisterBukkitCommand(this, cmd);
        }
      }
    }
    this.log("Good Bye!");
  }

  private void loadPersistentData() {

    try {

      Set.of(this.getDataFolder().toPath().resolve("configuration")).forEach(
          ThrowingConsumer.unchecked(FileUtils::createFolderIfNotExists));

      final HttpConfiguration http = new HttpConfiguration(this);
      final EncoderConfiguration configuration = new EncoderConfiguration(this);

      http.read();
      configuration.read();

      this.server = http.getServer();
      this.audioConfiguration = configuration.getSettings();
      this.manager = new PersistentPictureManager(this);

      this.manager.startTask();

    } catch (final IOException e) {
      this.logger.severe("A severe issue occurred while reading data from configuration files!");
      e.printStackTrace();
    }
  }

  public void log(@NotNull final String line) {
    this.audiences.console().sendMessage(format(text(line)));
  }

  public void log(@NotNull final Component line) {
    this.audiences.console().sendMessage(line);
  }

  private void registerCommands() {
    this.handler = new CommandHandler(this);
  }

  public PersistentPictureManager getPictureManager() {
    return this.manager;
  }

  public AudioConfiguration getAudioConfiguration() {
    return this.audioConfiguration;
  }

  public HttpServer getHttpServer() {
    return this.server;
  }

  public MediaLibraryCore library() {
    return this.library;
  }

  public BukkitAudiences audience() {
    return this.audiences;
  }
}
