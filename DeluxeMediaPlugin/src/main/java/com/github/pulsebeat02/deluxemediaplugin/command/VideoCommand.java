package com.github.pulsebeat02.deluxemediaplugin.command;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.github.pulsebeat02.deluxemediaplugin.config.HttpConfiguration;
import com.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction;
import com.github.pulsebeat02.minecraftmedialibrary.resourcepack.ResourcepackWrapper;
import com.github.pulsebeat02.minecraftmedialibrary.resourcepack.hosting.HttpDaemonProvider;
import com.github.pulsebeat02.minecraftmedialibrary.utility.VideoExtractionUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.DitherHolder;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.DitherSetting;
import com.github.pulsebeat02.minecraftmedialibrary.video.itemframe.ItemFrameCallback;
import com.github.pulsebeat02.minecraftmedialibrary.video.player.VLCJIntegratedPlayer;
import com.github.pulsebeat02.minecraftmedialibrary.video.player.VideoPlayerBase;
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

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;

public class VideoCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> literalNode;
  private DitherHolder dither;
  private VideoPlayerBase player;
  private boolean youtube;
  private YoutubeExtraction extractor;
  private File file;

  private int frameWidth;
  private int frameHeight;
  private int screenWidth;
  private int screenHeight;

  private int startingMap;
  private CompletableFuture<Void> future;

  public VideoCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "video", executor, "deluxemediaplugin.command.video", "");
    dither = DitherSetting.SIERRA_FILTER_LITE_DITHER.getHolder();
    frameWidth = 5;
    frameHeight = 5;
    screenWidth = 640;
    screenHeight = 360;
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
      component = Component.text("Could not find dither type " + algorithm, NamedTextColor.RED);
    } else {
      component =
          TextComponent.ofChildren(
              Component.text("Set dither type to ", NamedTextColor.GOLD),
              Component.text(algorithm, NamedTextColor.AQUA));
      dither = setting.getHolder();
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
    startingMap = (int) id.getAsLong();
    audience.sendMessage(
        ChatUtilities.formatMessage(
            Component.text("Set starting-map on id " + startingMap, NamedTextColor.GOLD)));
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
    frameWidth = dims[0];
    frameHeight = dims[1];
    audience.sendMessage(
        ChatUtilities.formatMessage(
            Component.text(
                String.format(
                    "Set itemframe map dimensions to %s:%s (width:height)",
                    frameWidth, frameHeight),
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
    if (player != null) {
      final int[] dims = opt.get();
      screenWidth = dims[0];
      screenHeight = dims[1];
      component =
          Component.text(
              String.format(
                  "Set screen dimensions to %d:%d (width:height)", screenWidth, screenHeight),
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
    if (file == null && !youtube) {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text("There isn't a video currently playing!", NamedTextColor.RED)));
    } else {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text("=====================================", NamedTextColor.AQUA)));
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
                  Component.text("Video Name: " + file.getName(), NamedTextColor.GOLD),
                  Component.text(
                      String.format("Size: %d Kilobytes", file.getTotalSpace() / 1024),
                      NamedTextColor.GOLD)));
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text("=====================================", NamedTextColor.AQUA)));
    }
    return 1;
  }

  private int stopVideo(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    audience.sendMessage(Component.text("Stopped the Video!", NamedTextColor.GOLD));
    player.stop();
    return 1;
  }

  private int startVideo(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    if (file == null && !youtube) {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text("File and URL not Specified!", NamedTextColor.RED)));
      return 1;
    }
    audience.sendMessage(
        ChatUtilities.formatMessage(
            youtube
                ? Component.text(
                    "Starting Video on URL: " + extractor.getUrl(), NamedTextColor.GOLD)
                : Component.text(
                    "Starting Video on File: " + file.getName(), NamedTextColor.GOLD)));
    final MinecraftMediaLibrary library = getPlugin().getLibrary();
    if (library.isUsingVLCJ()) {
      player =
          VLCJIntegratedPlayer.builder()
              .setUrl(extractor.getUrl())
              .setWidth(screenWidth)
              .setHeight(screenHeight)
              .setCallback(
                  ItemFrameCallback.builder()
                          .setViewers(null)
                          .setMap(startingMap)
                          .setWidth(frameWidth)
                          .setHeight(frameHeight)
                          .setVideoWidth(screenWidth)
                          .setDelay(0)
                          .setDitherHolder(dither)
                          .createItemFrameCallback(library)
                      ::send)
              .createVLCJIntegratedPlayer(library);
    }
    player.start();
    return 1;
  }

  private int loadVideo(@NotNull final CommandContext<CommandSender> context) {
    final DeluxeMediaPlugin plugin = getPlugin();
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    final String mrl = context.getArgument("mrl", String.class);
    final String folderPath = plugin.getDataFolder().getAbsolutePath();
    if (!VideoExtractionUtilities.getVideoID(mrl).isPresent()) {
      final File f = new File(folderPath, mrl);
      final TextComponent component;
      if (f.exists()) {
        youtube = false;
        extractor = null;
        file = f;
        component = Component.text("Successfully loaded video " + f.getName(), NamedTextColor.GOLD);
      } else if (mrl.startsWith("http://") || mrl.startsWith("https://")) {
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
      youtube = true;
      if (future != null && !future.isDone()) {
        future.cancel(true);
      }
      extractor =
          new YoutubeExtraction(mrl, folderPath, plugin.getEncoderConfiguration().getSettings());
      extractor.extractAudio();
      file = null;
      final HttpConfiguration configuration = plugin.getHttpConfiguration();
      HttpDaemonProvider provider = null;
      if (configuration.isEnabled()) {
        provider = configuration.getDaemon();
        if (!provider.getDaemon().isRunning()) {
          provider.startServer();
        }
      }
      final HttpDaemonProvider finalProvider = provider;
      future =
          CompletableFuture.runAsync(() -> extractor.downloadVideo())
              .thenRunAsync(
                  () -> {
                    final ResourcepackWrapper wrapper =
                        ResourcepackWrapper.builder()
                            .setAudio(extractor.getAudio())
                            .setDescription("Youtube Video: " + extractor.getVideoTitle())
                            .setPath(
                                String.format(
                                    "%s/http/resourcepack.zip",
                                    plugin.getDataFolder().getAbsolutePath()))
                            .setPackFormat(6)
                            .createResourcepackHostingProvider(plugin.getLibrary());
                    wrapper.buildResourcePack();
                    if (finalProvider != null) {
                      final String url = finalProvider.generateUrl(wrapper.getPath());
                      for (final Player p : Bukkit.getOnlinePlayers()) {
                        p.setResourcePack(url);
                      }
                      audience.sendMessage(
                          ChatUtilities.formatMessage(
                              Component.text("Sending Resourcepack...", NamedTextColor.GOLD)));
                    } else {
                      audience.sendMessage(
                          ChatUtilities.formatMessage(
                              Component.text(
                                  "You have HTTP set false by default. You cannot "
                                      + "play Youtube videos without a daemon",
                                  NamedTextColor.RED)));
                      future.cancel(true);
                    }
                    audience.sendMessage(
                        ChatUtilities.formatMessage(
                            Component.text(
                                String.format("Successfully loaded video %s", mrl),
                                NamedTextColor.GOLD)));
                  });
    }
    return 1;
  }

  @Override
  public LiteralCommandNode<CommandSender> getCommandNode() {
    return literalNode;
  }
}
