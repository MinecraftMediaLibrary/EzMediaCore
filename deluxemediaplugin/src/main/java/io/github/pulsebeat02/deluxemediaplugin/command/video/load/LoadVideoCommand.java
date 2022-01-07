package io.github.pulsebeat02.deluxemediaplugin.command.video.load;

import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;
import static io.github.pulsebeat02.deluxemediaplugin.executors.FixedExecutors.RESOURCE_WRAPPER_EXECUTOR;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleTrue;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.deluxemediaplugin.command.video.load.wrapper.SimpleResourcepackWrapper;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio.AudioPlayback;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.deluxemediaplugin.utility.nullability.Nill;
import io.github.pulsebeat02.ezmediacore.ffmpeg.EnhancedExecution;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.PathInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.UrlInput;
import io.github.pulsebeat02.ezmediacore.utility.future.Throwing;
import io.github.pulsebeat02.ezmediacore.utility.io.FileUtils;
import io.github.pulsebeat02.ezmediacore.utility.io.ResourcepackUtils;
import io.github.pulsebeat02.ezmediacore.utility.media.RequestUtils;
import io.github.pulsebeat02.ezmediacore.utility.misc.Try;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class LoadVideoCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;

  private final Path videoFolder;

  public LoadVideoCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ScreenConfig config) {
    this.plugin = plugin;
    this.config = config;
    this.videoFolder = this.plugin.getDataFolder().resolve("videos");
    this.node =
        this.literal("load")
            .requires(has("deluxemediaplugin.command.video.load"))
            .then(new LoadUrlCommand(plugin, config, this).getNode())
            .then(new LoadMrlCommand(plugin, config, this).getNode())
            .then(new LoadPathCommand(plugin, config, this).getNode())
            .then(new LoadDeviceCommand(plugin, config, this).getNode())
            .then(new LoadDesktopCommand(plugin, config, this).getNode())
            .then(new LoadWindowCommand(plugin, config, this).getNode())
            .build();
  }

  public void loadVideo(@NotNull final Audience audience) {

    audience.sendMessage(Locale.LOADING_VIDEO.build());

    this.createFolders();
    this.cancelStream();

    this.config.setTask(
        CompletableFuture.runAsync(() -> this.handleVideo(audience), RESOURCE_WRAPPER_EXECUTOR)
            .handle(Throwing.THROWING_FUTURE));
  }

  private void handleVideo(@NotNull final Audience audience) {

    this.setCompletion(false);

    final Input input = this.config.getMedia();

    if (this.handleUrlInput(audience, input)) {
      return;
    }

    if (this.handleResourcepackAudio(audience, input)) {
      return;
    }

    this.sendCompletionMessage(audience, input);

    this.setCompletion(true);

    this.config.setTask(null);
  }

  private void setCompletion(final boolean completion) {
    this.config.setCompletion(completion);
  }

  private void sendCompletionMessage(@NotNull final Audience audience, @NotNull final Input input) {
    final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
    final String url = this.config.getPackUrl();
    final byte[] hash = this.config.getPackHash();
    ResourcepackUtils.forceResourcepackLoad(this.plugin.library(), players, url, hash);
    players.forEach(this::sendSeparatePackMessage);
    audience.sendMessage(Locale.LOADED_MEDIA.build(input.getInput()));
  }

  private void sendSeparatePackMessage(@NotNull final Player player) {
    final Audience playerAudience = this.plugin.audience().player(player);
    playerAudience.sendMessage(Locale.SEND_RESOURCEPACK_URL.build(player));
  }

  private boolean handleResourcepackAudio(
      @NotNull final Audience audience, @NotNull final Input input) {
    if (this.isResourcepackAudio()) {
      audience.sendMessage(Locale.CREATE_RESOURCEPACK.build());
      return this.handleResourcepack(audience, input);
    }
    return false;
  }

  private boolean handleResourcepack(@NotNull final Audience audience, @NotNull final Input input) {
    try {

      final Optional<Path> download = this.downloadUrlInput(audience, input);
      if (download.isEmpty()) {
        audience.sendMessage(Locale.ERR_DOWNLOAD_VIDEO.build());
        return true;
      }

      new SimpleResourcepackWrapper(this.plugin, this.config, download.get(), this.videoFolder)
          .loadResourcepack();
      
    } catch (final IOException | InterruptedException e) {
      audience.sendMessage(Locale.ERR_LOAD_VIDEO.build());
      e.printStackTrace();
    }
    return false;
  }

  private @NotNull Optional<Path> downloadUrlInput(
      @NotNull final Audience audience, @NotNull final Input input)
      throws IOException, InterruptedException {
    return this.isPathInput(input)
        ? Optional.of(Path.of(input.getInput()))
        : this.downloadFile(audience, this.videoFolder, input);
  }

  private boolean isPathInput(@NotNull final Input input) {
    return input instanceof PathInput;
  }

  private Optional<Path> downloadFile(
      @NotNull final Audience audience, @NotNull final Path folder, @NotNull final Input input)
      throws IOException, InterruptedException {

    final List<Input> results = RequestUtils.getAudioURLs(input);

    if (this.checkInvalidUrl(audience, results)) {
      return Optional.empty();
    }

    final Input first = results.get(0);
    final Path target = RequestUtils.downloadFile(folder.resolve(".audio.tmp"), first.getInput());

    return Optional.of(target);
  }

  private boolean checkInvalidUrl(
      @NotNull final Audience audience, @NotNull final List<Input> results) {
    return handleTrue(audience, Locale.ERR_INVALID_MRL.build(), results.isEmpty());
  }

  private boolean isResourcepackAudio() {
    return this.config.getAudioPlayback() == AudioPlayback.RESOURCEPACK;
  }

  private boolean handleUrlInput(@NotNull final Audience audience, @NotNull final Input input) {
    if (this.isUrlInput(input)) {
      return this.checkStreamMrl(audience, input);
    }
    return false;
  }

  private boolean checkStreamMrl(@NotNull final Audience audience, @NotNull final Input input) {
    try {
      return this.isStream(input)
          ? this.handleStream(audience, input)
          : this.checkInvalidUrl(audience, input);
    } catch (final IllegalArgumentException e) {
      audience.sendMessage(Locale.ERR_INVALID_MRL.build());
      return true;
    }
  }

  private boolean isStream(@NotNull final Input input) {
    return RequestUtils.isStream(input);
  }

  private boolean checkInvalidUrl(@NotNull final Audience audience, @NotNull final Input input) {

    final List<Input> urls = RequestUtils.getVideoURLs(input);

    final boolean equal = urls.get(0).equals(input);
    final boolean size = urls.size() == 1;

    return handleTrue(audience, Locale.ERR_INVALID_MRL.build(), size && equal);
  }

  private boolean handleStream(@NotNull final Audience audience, @NotNull final Input input) {

    if (this.checkInvalidAudioPlayback(audience)) {
      return true;
    }

    audience.sendMessage(Locale.LOADED_MEDIA.build(input.getInput()));

    this.config.setCompletion(true);

    return true;
  }

  private boolean checkInvalidAudioPlayback(@NotNull final Audience audience) {
    return handleTrue(
        audience, Locale.ERR_INVALID_AUDIO_OUTPUT.build(), this.isResourcepackAudio());
  }

  private boolean isUrlInput(@NotNull final Input input) {
    return input instanceof UrlInput;
  }

  private void createFolders() {
    FileUtils.createDirectoryIfNotExistsExceptionally(this.videoFolder);
  }

  private void cancelStream() {
    final EnhancedExecution stream = this.config.getStream();
    Nill.ifNot(stream, () -> Try.closeable(stream));
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
