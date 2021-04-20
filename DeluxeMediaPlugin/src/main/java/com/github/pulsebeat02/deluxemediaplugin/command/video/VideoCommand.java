package com.github.pulsebeat02.deluxemediaplugin.command.video;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import com.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction;
import com.github.pulsebeat02.minecraftmedialibrary.resourcepack.ResourcepackWrapper;
import com.github.pulsebeat02.minecraftmedialibrary.resourcepack.hosting.HttpDaemonProvider;
import com.github.pulsebeat02.minecraftmedialibrary.utility.VideoExtractionUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.DitherSetting;
import com.github.pulsebeat02.minecraftmedialibrary.video.itemframe.ItemFrameCallback;
import com.github.pulsebeat02.minecraftmedialibrary.video.player.VLCJIntegratedPlayer;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> literalNode;
  private final MinecraftVideoAttributes attributes;

  public VideoCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "video", executor, "deluxemediaplugin.command.video", "");
    attributes = new MinecraftVideoAttributes();
    final LiteralArgumentBuilder<CommandSender> builder = literal(getName());
    builder
        .requires(super::testPermission)
        .then(literal("start").executes(this::startVideo))
        .then(literal("stop").executes(this::stopVideo))
        .then(
            literal("load")
                .then(argument("mrl", StringArgumentType.greedyString()).executes(this::loadVideo)))
        .then(
            literal("set")
                .then(
                    literal("screen-dimension")
                        .then(
                            argument("screen-dimensions", StringArgumentType.greedyString())
                                .executes(this::setScreenDimension)))
                .then(
                    literal("itemframe-dimension")
                        .then(
                            argument("itemframe-dimensions", StringArgumentType.greedyString())
                                .executes(this::setItemFrameDimension)))
                .then(
                    literal("starting-map")
                        .then(
                            argument("starting-map-id", StringArgumentType.greedyString())
                                .executes(this::setStartingMap)))
                .then(
                    literal("dither")
                        .then(
                            argument("dithering-algorithm", StringArgumentType.greedyString())
                                .suggests(this::suggestDitherAlgorithm)
                                .executes(this::setDitherAlgorithm))))
        .executes(this::displayInformation);
    literalNode = builder.build();
  }

  @Override
  public TextComponent usage() {
    return ChatUtilities.getCommandUsage(
        ImmutableMap.<String, String>builder()
            .put("/video", "Lists the command usage for the video command")
            .put("/video start", "Starts the video")
            .put("/video stop", "Stops the video")
            .put("/video load [url]", "Loads a Youtube link")
            .put("/video load [file]", "Loads a specific video file")
            .put("/video set screen-dimension [width:height]", "Sets the resolution of the screen")
            .put(
                "/video set itemframe-dimension [width:height]",
                "Sets the proper itemframe dimension of the screen")
            .put("/video set dither [algorithm]", "Sets the specific algorithm for dithering")
            .put(
                "/video set starting-map [id]",
                "Sets the starting map id from id to id + 25. (For example 0 - 24)")
            .build());
  }

  private CompletableFuture<Suggestions> suggestDitherAlgorithm(
      final CommandContext<CommandSender> context, final SuggestionsBuilder builder) {
    Arrays.stream(DitherSetting.values()).forEach(x -> builder.suggest(x.name()));
    return builder.buildFuture();
  }

  private int setDitherAlgorithm(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    final String algorithm = context.getArgument("dithering-algorithm", String.class);
    final DitherSetting setting = DitherSetting.fromString(algorithm);
    final TextComponent component;
    if (setting == null) {
      component =
          Component.text(
              String.format("Could not find dither type %s", algorithm), NamedTextColor.RED);
    } else {
      component =
          TextComponent.ofChildren(
              Component.text("Set dither type to ", NamedTextColor.GOLD),
              Component.text(algorithm, NamedTextColor.AQUA));
      attributes.setDither(setting.getHolder());
    }
    audience.sendMessage(ChatUtilities.formatMessage(component));
    return 1;
  }

  private int setStartingMap(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    final OptionalLong id =
        ChatUtilities.checkMapBoundaries(
            audience, context.getArgument("starting-map-id", String.class));
    if (!id.isPresent()) {
      return 1;
    }
    attributes.setStartingMap((int) id.getAsLong());
    audience.sendMessage(
        ChatUtilities.formatMessage(
            Component.text(
                String.format("Set starting-map on id %d", attributes.getStartingMap()),
                NamedTextColor.GOLD)));
    return 1;
  }

  private int setItemFrameDimension(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    final Optional<int[]> opt =
        ChatUtilities.checkDimensionBoundaries(
            audience, context.getArgument("itemframe-dimensions", String.class));
    if (!opt.isPresent()) {
      return 1;
    }
    final int[] dims = opt.get();
    attributes.setFrameWidth(dims[0]);
    attributes.setFrameHeight(dims[1]);
    audience.sendMessage(
        ChatUtilities.formatMessage(
            Component.text(
                String.format(
                    "Set itemframe map dimensions to %s:%s (width:height)", dims[0], dims[1]),
                NamedTextColor.GOLD)));
    return 1;
  }

  private int setScreenDimension(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    final Optional<int[]> opt =
        ChatUtilities.checkDimensionBoundaries(
            audience, context.getArgument("screen-dimensions", String.class));
    if (!opt.isPresent()) {
      return 1;
    }
    final TextComponent component;
    if (attributes.getPlayer() != null) {
      final int[] dims = opt.get();
      attributes.setScreenWidth(dims[0]);
      attributes.setScreenHeight(dims[1]);
      component =
          Component.text(
              String.format("Set screen dimensions to %d:%d (width:height)", dims[0], dims[1]),
              NamedTextColor.GOLD);
    } else {
      component =
          Component.text(
              "Please load a video first before executing this command!", NamedTextColor.RED);
    }
    audience.sendMessage(ChatUtilities.formatMessage(component));
    return 1;
  }

  private int displayInformation(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    final boolean youtube = attributes.isYoutube();
    final File file = attributes.getFile();
    if (file == null && !youtube) {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text("There isn't a video currently playing!", NamedTextColor.RED)));
    } else {
      final YoutubeExtraction extractor = attributes.getExtractor();
      audience.sendMessage(
          Component.text("=====================================", NamedTextColor.AQUA));
      audience.sendMessage(
          youtube
              ? Component.join(
                  Component.newline(),
                  Component.text(
                      String.format("Title: %s", extractor.getVideoTitle()), NamedTextColor.GOLD),
                  Component.text(
                      String.format("Author: %s", extractor.getAuthor()), NamedTextColor.GOLD),
                  Component.text(
                      String.format("Rating: %s", extractor.getVideoRating()), NamedTextColor.GOLD),
                  TextComponent.ofChildren(
                      Component.text("Video Identifier: ", NamedTextColor.GOLD),
                      Component.text(extractor.getVideoId(), NamedTextColor.RED)))
              : Component.join(
                  Component.newline(),
                  Component.text(
                      String.format("Video Name: %s", file.getName()), NamedTextColor.GOLD),
                  Component.text(
                      String.format("Size: %d Kilobytes", file.getTotalSpace() / 1024),
                      NamedTextColor.GOLD)));
      audience.sendMessage(
          Component.text("=====================================", NamedTextColor.AQUA));
    }
    return 1;
  }

  private int stopVideo(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    audience.sendMessage(Component.text("Stopped the Video!", NamedTextColor.GOLD));
    attributes.getPlayer().stop();
    return 1;
  }

  private int startVideo(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    final boolean youtube = attributes.isYoutube();
    final File file = attributes.getFile();
    if (file == null && !youtube) {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text("File and URL not Specified!", NamedTextColor.RED)));
      return 1;
    }
    final AtomicBoolean atomicBoolean = attributes.getCompletion();
    if (!atomicBoolean.get()) {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text("The video is still being downloaded!", NamedTextColor.RED)));
      return 1;
    }
    atomicBoolean.set(false);
    final YoutubeExtraction extractor = attributes.getExtractor();
    audience.sendMessage(
        ChatUtilities.formatMessage(
            youtube
                ? Component.text(
                    String.format("Starting Video on URL: %s", extractor.getUrl()),
                    NamedTextColor.GOLD)
                : Component.text(
                    String.format("Starting Video on File: %s", file.getName()),
                    NamedTextColor.GOLD)));
    final MinecraftMediaLibrary library = getPlugin().getLibrary();
    if (library.isUsingVLCJ()) {
      attributes.setPlayer(
          VLCJIntegratedPlayer.builder()
              .setUrl(attributes.getFile().getAbsolutePath())
              .setWidth(attributes.getScreenWidth())
              .setHeight(attributes.getScreenHeight())
              .setCallback(
                  ItemFrameCallback.builder()
                          .setViewers(null)
                          .setMap(attributes.getStartingMap())
                          .setWidth(attributes.getFrameWidth())
                          .setHeight(attributes.getFrameHeight())
                          .setVideoWidth(attributes.getScreenWidth())
                          .setDelay(0)
                          .setDitherHolder(attributes.getDither())
                          .createItemFrameCallback(library)
                      ::send)
              .createVLCJIntegratedPlayer(library));
    }
    attributes.getPlayer().start(Bukkit.getOnlinePlayers());
    return 1;
  }

  private int loadVideo(@NotNull final CommandContext<CommandSender> context) {
    final DeluxeMediaPlugin plugin = getPlugin();
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    final String mrl = context.getArgument("mrl", String.class);
    final String folderPath = String.format("%s/mml/", plugin.getDataFolder().getAbsolutePath());
    if (!VideoExtractionUtilities.getVideoID(mrl).isPresent()) {
      final File f = new File(folderPath, mrl);
      final TextComponent component;
      if (f.exists()) {
        attributes.setYoutube(false);
        attributes.setExtractor(null);
        attributes.setFile(f);
        component =
            Component.text(
                String.format("Successfully loaded video %s", f.getName()), NamedTextColor.GOLD);
      } else if (mrl.startsWith("http://")) {
        component =
            Component.text(
                String.format("Link %s is not a valid Youtube video link!", mrl),
                NamedTextColor.RED);
      } else {
        component =
            Component.text(
                String.format("File %s cannot be found!", f.getName()), NamedTextColor.RED);
      }
      audience.sendMessage(ChatUtilities.formatMessage(component));
    } else {
      attributes.setYoutube(true);
      final YoutubeExtraction extractor =
          new YoutubeExtraction(mrl, folderPath, plugin.getEncoderConfiguration().getSettings());
      extractor.extractAudio();
      attributes.setFile(extractor.getVideo());
      attributes.setExtractor(extractor);
      CompletableFuture.runAsync(extractor::downloadVideo)
          .thenRunAsync(
              () ->
                  sendResourcepack(
                      plugin.getHttpConfiguration().getDaemon(),
                      audience,
                      buildResourcepack(extractor, plugin)))
          .thenRunAsync(
              () ->
                  audience.sendMessage(
                      ChatUtilities.formatMessage(
                          Component.text(
                              String.format("Successfully loaded video %s", mrl),
                              NamedTextColor.GOLD))))
          .whenCompleteAsync((t, throwable) -> attributes.getCompletion().set(true));
    }
    return 1;
  }

  private ResourcepackWrapper buildResourcepack(
      @NotNull final YoutubeExtraction extractor, @NotNull final DeluxeMediaPlugin plugin) {
    final ResourcepackWrapper wrapper = ResourcepackWrapper.of(extractor, plugin.getLibrary());
    wrapper.buildResourcePack();
    return wrapper;
  }

  private void sendResourcepack(
      @Nullable final HttpDaemonProvider provider,
      @NotNull final Audience audience,
      @NotNull final ResourcepackWrapper wrapper) {
    if (provider != null) {
      final String url = provider.generateUrl(wrapper.getPath());
      for (final Player p : Bukkit.getOnlinePlayers()) {
        p.setResourcePack(url);
      }
    } else {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text(
                  "You have HTTP set false by default. You cannot "
                      + "play Youtube videos without a daemon",
                  NamedTextColor.RED)));
    }
  }

  @Override
  public LiteralCommandNode<CommandSender> getCommandNode() {
    return literalNode;
  }
}
