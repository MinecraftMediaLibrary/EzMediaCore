/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.pulsebeat02.deluxemediaplugin.command.audio;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.executors.FixedExecutors.RESOURCE_WRAPPER_EXECUTOR;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleFalse;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleNull;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleTrue;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import io.github.pulsebeat02.ezmediacore.ffmpeg.YoutubeVideoAudioExtractor;
import io.github.pulsebeat02.ezmediacore.resourcepack.ResourcepackSoundWrapper;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import io.github.pulsebeat02.ezmediacore.utility.io.HashingUtils;
import io.github.pulsebeat02.ezmediacore.utility.io.ResourcepackUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class AudioLoadCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final AudioCommandAttributes attributes;
  private final DeluxeMediaPlugin plugin;

  public AudioLoadCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final AudioCommandAttributes attributes) {
    this.plugin = plugin;
    this.attributes = attributes;
    this.node =
        this.literal("load")
            .then(this.argument("mrl", StringArgumentType.greedyString()).executes(this::loadAudio))
            .then(this.literal("resourcepack").executes(this::sendResourcepack))
            .build();
  }

  private boolean loadSoundMrl(@NotNull final Audience audience, @NotNull final String mrl) {

    final MediaLibraryCore core = this.plugin.library();
    final Path audio = core.getAudioPath().resolve("audio.ogg");
    final AudioConfiguration configuration = this.plugin.getAudioConfiguration();
    this.executeYoutubeVideoAudioExtractor(mrl, core, audio, configuration);

    this.attributes.setAudio(audio);

    return true;
  }

  private void executeYoutubeVideoAudioExtractor(
      @NotNull final String mrl,
      final MediaLibraryCore core,
      @NotNull final Path audio,
      @NotNull final AudioConfiguration configuration) {
    YoutubeVideoAudioExtractor.ofYoutubeVideoAudioExtractor(core, configuration, mrl, audio)
        .executeAsync();
  }

  private boolean loadSoundFile(@NotNull final String mrl, @NotNull final Audience audience) {

    final Path file = Path.of(mrl);
    if (handleTrue(audience, Locale.ERR_INVALID_MRL.build(), Files.notExists(file))) {
      return true;
    }

    this.attributes.setAudio(file);

    return false;
  }

  private void wrapPack() {
    this.attributes.setCompletion(false);
    try {
      final HttpServer daemon = this.getHttpDaemon();
      this.setPackInfo(daemon, this.createPackWrapper(daemon));
    } catch (final IOException e) {
      this.plugin.getConsoleAudience().sendMessage(Locale.ERR_RESOURCEPACK_WRAP.build());
      e.printStackTrace();
    }
    this.attributes.setCompletion(true);
  }

  @NotNull
  private HttpServer getHttpDaemon() {

    final HttpServer daemon = this.plugin.getHttpServer();
    if (!daemon.isRunning()) {
      daemon.startServer();
    }

    return daemon;
  }

  private void setPackInfo(
      final @NotNull HttpServer daemon, final @NotNull ResourcepackSoundWrapper wrapper) {
    final Path path = wrapper.getResourcepackFilePath();
    this.attributes.setLink(daemon.createUrl(path));
    this.attributes.setHash(HashingUtils.createHashSha1(path).orElseThrow(AssertionError::new));
  }

  @NotNull
  private ResourcepackSoundWrapper createPackWrapper(@NotNull final HttpServer daemon)
      throws IOException {

    final Path output = daemon.getDaemon().getServerPath().resolve("resourcepack.zip");
    final String name = this.plugin.getBootstrap().getName().toLowerCase(java.util.Locale.ROOT);

    final ResourcepackSoundWrapper wrapper =
        ResourcepackSoundWrapper.ofSoundPack(output, "Audio Pack", 6);
    wrapper.addSound(name, this.attributes.getAudio());
    wrapper.wrap();

    return wrapper;
  }

  private int loadAudio(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String mrl = context.getArgument("mrl", String.class);
    if (this.isLoadingSound(audience)) {
      return SINGLE_SUCCESS;
    }

    audience.sendMessage(Locale.CREATE_RESOURCEPACK.build());

    this.handleAudio(audience, mrl)
        .thenRunAsync(this::wrapPack, RESOURCE_WRAPPER_EXECUTOR)
        .thenRun(() -> this.forceResourcepackLoad(audience));

    return SINGLE_SUCCESS;
  }

  private CompletableFuture<Void> handleAudio(final Audience audience, @NotNull final String mrl) {
    return mrl.contains("http")
        ? this.handleWebFile(audience, mrl)
        : this.handleLocalFile(audience, mrl);
  }

  @Contract("_, _ -> new")
  private @NotNull CompletableFuture<Void> handleWebFile(
      @NotNull final Audience audience, @NotNull final String mrl) {
    return CompletableFuture.runAsync(
        () -> this.loadSoundMrl(audience, mrl), RESOURCE_WRAPPER_EXECUTOR);
  }

  private @NotNull CompletableFuture<Void> handleLocalFile(
      @NotNull final Audience audience, @NotNull final String mrl) {
    return CompletableFuture.runAsync(
        () -> this.loadSoundFile(mrl, audience), RESOURCE_WRAPPER_EXECUTOR);
  }

  private void forceResourcepackLoad(@NotNull final Audience audience) {
    this.forcePack();
    audience.sendMessage(
        Locale.FIN_RESOURCEPACK_INIT.build(this.attributes.getLink(), this.attributes.getHash()));
  }

  private int sendResourcepack(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final Component component = Locale.ERR_NO_RESOURCEPACK.build();

    if (handleNull(audience, component, this.attributes.getLink())) {
      return SINGLE_SUCCESS;
    }

    if (handleNull(audience, component, this.attributes.getHash())) {
      return SINGLE_SUCCESS;
    }

    this.forcePack();

    audience.sendMessage(
        Locale.SENT_RESOURCEPACK.build(this.attributes.getLink(), this.attributes.getHash()));

    return SINGLE_SUCCESS;
  }

  private void forcePack() {
    final String url = this.attributes.getLink();
    final byte[] hash = this.attributes.getHash();
    ResourcepackUtils.forceResourcepackLoad(this.plugin.library(), url, hash);
  }

  private boolean isLoadingSound(@NotNull final Audience audience) {
    return handleFalse(
        audience, Locale.ERR_INVALID_AUDIO_STATE.build(), this.attributes.getCompletion().get());
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
