package io.github.pulsebeat02.deluxemediaplugin.command.video;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleNull;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.deluxemediaplugin.utility.nullability.Nill;
import io.github.pulsebeat02.ezmediacore.ffmpeg.EnhancedExecution;
import io.github.pulsebeat02.ezmediacore.utility.misc.Try;
import net.kyori.adventure.audience.Audience;
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

    if (this.handleProcessing(audience, extractor)) {
      return SINGLE_SUCCESS;
    }

    this.setupCancelledAttributes(extractor);

    audience.sendMessage(Locale.CANCELLED_VIDEO_PROCESSING.build());

    return SINGLE_SUCCESS;
  }

  private boolean handleProcessing(
      @NotNull final Audience audience, @Nullable final EnhancedExecution extractor) {
    return handleNull(audience, Locale.ERR_CANCELLATION_VIDEO_PROCESSING.build(), extractor);
  }

  private void setupCancelledAttributes(@Nullable final EnhancedExecution extractor) {
    Nill.ifNot(extractor, () -> this.cancelExtractor(extractor));
    Nill.ifNot(this.config.getTask(), this::cancelTask);
  }

  private void cancelTask() {
    this.config.getTask().cancel(true);
    this.config.setTask(null);
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
