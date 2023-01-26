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
package io.github.pulsebeat02.deluxemediaplugin.command.video;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleNull;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import io.github.pulsebeat02.deluxemediaplugin.utility.nullability.Nill;
import io.github.pulsebeat02.ezmediacore.ffmpeg.EnhancedExecution;
import io.github.pulsebeat02.ezmediacore.utility.misc.Try;
import java.util.concurrent.CancellationException;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class VideoCancelProcessingCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;

  public VideoCancelProcessingCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ScreenConfig config) {
    this.plugin = plugin;
    this.config = config;
    this.node =
        this.literal("cancel-processing")
            .requires(has("deluxemediaplugin.command.video.cancelprocessing"))
            .executes(this::cancelProcessing)
            .build();
  }

  private int cancelProcessing(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final EnhancedExecution extractor = this.config.getExtractor();

    if (this.handleProcessing(audience)) {
      return SINGLE_SUCCESS;
    }

    this.setupCancelledAttributes(extractor);

    audience.sendMessage(Locale.CANCEL_VIDEO_PROCESSING.build());

    return SINGLE_SUCCESS;
  }

  private boolean handleProcessing(@NotNull final Audience audience) {
    final Component component = Locale.NO_PROCESSING_VIDEO.build();
    return handleNull(audience, component, this.config.getTask());
  }

  private void setupCancelledAttributes(@Nullable final EnhancedExecution extractor) {
    Nill.ifNot(extractor, () -> this.cancelExtractor(extractor));
    Nill.ifNot(this.config.getTask(), this::cancelTask);
  }

  private void cancelTask() {
    this.forceCancel();
    this.config.setTask(null);
  }

  private void forceCancel() {
    try {
      this.config.getTask().cancel(true);
    } catch (final CancellationException ignored) {
    }
  }

  private void cancelExtractor(@NotNull final EnhancedExecution extractor) {
    Try.closeable(extractor);
    this.config.setExtractor(null);
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
