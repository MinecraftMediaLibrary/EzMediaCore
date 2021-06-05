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

package io.github.pulsebeat02.deluxemediaplugin.command.video;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.extractor.AudioExtractionHelper;
import io.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction;
import io.github.pulsebeat02.minecraftmedialibrary.frame.VLCVideoPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitherSetting;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.PackWrapper;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.ResourcepackWrapper;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.hosting.HttpServerDaemon;
import io.github.pulsebeat02.minecraftmedialibrary.utility.PathUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.VideoExtractionUtilities;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> literalNode;
  private final MinecraftVideoAttributes attributes;
  private final VideoBuilder videoBuilder;
  private String resourcepackLink;
  private byte[] hash;

  public VideoCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "video", executor, "deluxemediaplugin.command.video", "");
    attributes = new MinecraftVideoAttributes();
    final LiteralArgumentBuilder<CommandSender> builder = literal(getName());
    builder
        .requires(super::testPermission)
        .then(literal("play").executes(this::playVideo))
        .then(literal("stop").executes(this::stopVideo))
        .then(
            literal("load")
                .then(argument("mrl", StringArgumentType.greedyString()).executes(this::loadVideo))
                .then(literal("resourcepack").executes(this::getResourcepackLink)))
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
                                .executes(this::setDitherAlgorithm)))
                .then(
                    literal("mode")
                        .then(
                            argument("video-mode", StringArgumentType.greedyString())
                                .suggests(this::suggestVideoMode)
                                .executes(this::setVideoMode))))
        .executes(this::displayInformation);
    literalNode = builder.build();
    videoBuilder = new VideoBuilder(plugin.getLibrary(), attributes);
  }

  @Override
  public TextComponent usage() {
    return ChatUtilities.getCommandUsage(
        ImmutableMap.<String, String>builder()
            .put("/video", "Lists the command usage for the video command")
            .put("/video play", "Plays the video")
            .put("/video stop", "Stops the video")
            .put("/video load [url]", "Loads a Youtube link")
            .put("/video load [file]", "Loads a specific video file")
            .put("/video load resourcepack", "Loads the past resourcepack used for the video")
            .put("/video set screen-dimension [width:height]", "Sets the resolution of the screen")
            .put(
                "/video set itemframe-dimension [width:height]",
                "Sets the proper itemframe dimension of the screen")
            .put("/video set dither [algorithm]", "Sets the specific algorithm for dithering")
            .put(
                "/video set starting-map [id]",
                "Sets the starting map id from id to id + 25. (For example 0 - 24)")
            .put(
                "/video set mode [mode]",
                "Sets whether the video should be entity clouds or itemframes")
            .build());
  }

  private CompletableFuture<Suggestions> suggestDitherAlgorithm(
      final CommandContext<CommandSender> context, final SuggestionsBuilder builder) {
    Arrays.stream(DitherSetting.values()).forEach(x -> builder.suggest(x.name()));
    return builder.buildFuture();
  }

  private CompletableFuture<Suggestions> suggestVideoMode(
      final CommandContext<CommandSender> context, final SuggestionsBuilder builder) {
    Arrays.stream(VideoType.values()).forEach(x -> builder.suggest(x.getName()));
    return builder.buildFuture();
  }

  private int getResourcepackLink(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    if (resourcepackLink == null && hash == null) {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text(
                  "Please load a resourcepack first before executing this command!",
                  NamedTextColor.RED)));
      return 1;
    }
    sendResourcepackFile();
    audience.sendMessage(
        ChatUtilities.formatMessage(
            Component.text(
                String.format("Sent Resourcepack URL! (%s)", resourcepackLink),
                NamedTextColor.GOLD)));
    return 1;
  }

  private int setVideoMode(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    final String mode = context.getArgument("video-mode", String.class);
    final TextComponent component;
    final VideoType type = VideoType.fromString(mode);
    if (type != null) {
      attributes.setVideoType(type);
      component =
          TextComponent.ofChildren(
              Component.text("Set video mode to ", NamedTextColor.GOLD),
              Component.text(mode, NamedTextColor.AQUA));
    } else {
      component =
          Component.text(String.format("Could not find video mode %s", mode), NamedTextColor.RED);
    }
    audience.sendMessage(component);
    return 1;
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

      // Set the dither algorithm for the video attributes
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

    // Set the starting map of the video player
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

    // Set the itemframe dimensions of the video player
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

      // Set the screen dimensions (in pixels) of the video player
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
    final Path file = attributes.getFile();
    if (file == null && !youtube) {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text("There isn't a video currently playing!", NamedTextColor.RED)));
    } else {
      final YoutubeExtraction extractor = attributes.getExtractor();
      audience.sendMessage(
          Component.text("=====================================", NamedTextColor.AQUA));
      try {
        audience.sendMessage(
            youtube
                ? Component.join(
                    Component.newline(),
                    Component.text(
                        String.format("Title: %s", extractor.getVideoTitle()), NamedTextColor.GOLD),
                    Component.text(
                        String.format("Author: %s", extractor.getAuthor()), NamedTextColor.GOLD),
                    Component.text(
                        String.format("Rating: %s", extractor.getVideoRating()),
                        NamedTextColor.GOLD),
                    TextComponent.ofChildren(
                        Component.text("Video Identifier: ", NamedTextColor.GOLD),
                        Component.text(extractor.getVideoId(), NamedTextColor.RED)))
                : Component.join(
                    Component.newline(),
                    Component.text(
                        String.format("Video Name: %s", PathUtilities.getName(file)),
                        NamedTextColor.GOLD),
                    Component.text(
                        String.format("Size: %d Kilobytes", Files.size(file) / 1024),
                        NamedTextColor.GOLD)));
      } catch (final IOException e) {
        e.printStackTrace();
      }
      audience.sendMessage(
          Component.text("=====================================", NamedTextColor.AQUA));
    }
    return 1;
  }

  private int stopVideo(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    audience.sendMessage(Component.text("Stopped the Video!", NamedTextColor.GOLD));

    // Stop the media player
    attributes.getPlayer().stop(Bukkit.getOnlinePlayers());
    return 1;
  }

  private int playVideo(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    final Audience audience = getPlugin().getAudiences().sender(sender);
    final boolean youtube = attributes.isYoutube();
    final Path file = attributes.getFile();
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
              Component.text("The video is still being processed!", NamedTextColor.RED)));
      return 1;
    }

    final YoutubeExtraction extractor = attributes.getExtractor();
    audience.sendMessage(
        ChatUtilities.formatMessage(
            youtube
                ? Component.text(
                    String.format("Starting Video on URL: %s", extractor.getUrl()),
                    NamedTextColor.GOLD)
                : Component.text(
                    String.format("Starting Video on File: %s", PathUtilities.getName(file)),
                    NamedTextColor.GOLD)));
    final MediaLibrary library = getPlugin().getLibrary();

    final VLCVideoPlayer player = attributes.getPlayer();
    if (player != null && player.isPlaying()) {
      player.stop(Bukkit.getOnlinePlayers());
    }

    // Check if the library is using vlcj
    if (library.isVlcj()) {

      // Get the video type
      final VideoType type = attributes.getVideoType();
      switch (type) {
        case ITEMFRAME:

          // If the mode is set to itemframes/maps
          // Set the player to be a new map integrated player
          attributes.setPlayer(videoBuilder.createMapPlayer());
          break;

        case ARMOR_STAND:

          // If the mode is set to an area effect cloud
          // Check if the sender is an instanceof a Player
          if (sender instanceof Player) {

            // Set the player to be a new cloud integrated player
            attributes.setPlayer(videoBuilder.createEntityPlayer((Player) sender));
          } else {
            audience.sendMessage(
                Component.text(
                    "You must be an player to execute this command!", NamedTextColor.RED));
          }
          break;

        case CHATBOX:
          // If the mode is set to a chatbox
          // Set the player to be a chat player
          attributes.setPlayer(videoBuilder.createChatBoxPlayer());
          break;

        case SCOREBOARD:
          // If the mode is set to a scoreboard
          // Set the player to be a scoreboard player
          attributes.setPlayer(videoBuilder.createScoreboardPlayer());
          break;

        case DEBUG_HIGHLIGHTS:
          // If the mode is set to debug highlights
          // Check if the sender is an instanceof a Player
          if (sender instanceof Player) {

            // Set the player to be a debug highlights player
            attributes.setPlayer(videoBuilder.createBlockHighlightPlayer((Player) sender));
          } else {
            audience.sendMessage(
                Component.text(
                    "You must be an in-game player to execute this command!", NamedTextColor.RED));
          }
          break;
      }
    } else {
      audience.sendMessage(
          Component.text("VLC isn't enabled! Cannot play videos!", NamedTextColor.RED));
      return 1;
    }

    // Start the player and play the sound to all online players
    attributes.getPlayer().start(Bukkit.getOnlinePlayers());
    return 1;
  }

  private int loadVideo(@NotNull final CommandContext<CommandSender> context) {
    final DeluxeMediaPlugin plugin = getPlugin();
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    final String mrl = context.getArgument("mrl", String.class);
    final String folderPath = String.format("%s/mml/", plugin.getDataFolder().getAbsolutePath());
    final AtomicBoolean atomicBoolean = attributes.getCompletion();
    if (!VideoExtractionUtilities.getYoutubeID(mrl).isPresent()) {

      // This means we have a file
      final Path f = Paths.get(folderPath).resolve(mrl);

      // Check file existence
      if (Files.exists(f)) {

        // Set status to false
        atomicBoolean.set(false);

        // Set youtube to false, extractor to null, etc because we are using an actual video file.
        attributes.setYoutube(false);
        attributes.setExtractor(null);
        attributes.setFile(f);

        CompletableFuture.runAsync(
            () -> {
              final Path audioPath = Paths.get(folderPath, "custom.ogg");

              new AudioExtractionHelper(
                      plugin.getEncoderConfiguration().getSettings(), Paths.get(mrl), audioPath)
                  .extract();

              // Get the resourcepack wrapper from the extractor
              final PackWrapper wrapper = ResourcepackWrapper.of(plugin.getLibrary(), audioPath);

              // Build the resourcepack
              wrapper.buildResourcePack();

              // Send the resourcepack to all players on the server
              sendResourcepack(plugin.getHttpConfiguration().getDaemon(), audience, wrapper);

              // Send success message
              audience.sendMessage(
                  ChatUtilities.formatMessage(
                      Component.text(
                          String.format("Successfully loaded video %s", mrl),
                          NamedTextColor.GOLD)));

              // Set completed
              atomicBoolean.set(true);
            });

      } else if (mrl.startsWith("http://")) {
        audience.sendMessage(
            ChatUtilities.formatMessage(
                Component.text(
                    String.format("Link %s is not a valid Youtube video link!", mrl),
                    NamedTextColor.RED)));
      } else {
        audience.sendMessage(
            ChatUtilities.formatMessage(
                Component.text(
                    String.format("File %s cannot be found!", PathUtilities.getName(f)),
                    NamedTextColor.RED)));
      }
    } else {

      // This means we have a Youtube link
      atomicBoolean.set(false);

      // Set the attributes of a Youtube Video to be true
      attributes.setYoutube(true);

      CompletableFuture.runAsync(
          () -> {

            // Create an extractor out of the Youtube link
            final YoutubeExtraction extractor =
                new YoutubeExtraction(
                    mrl, Paths.get(folderPath), plugin.getEncoderConfiguration().getSettings());

            // Extract the audio of the URL
            extractor.extractAudio();

            // Set the file to be the video file of the extraction
            attributes.setFile(extractor.getVideo());

            // Set the extractor
            attributes.setExtractor(extractor);

            // Get the resourcepack wrapper from the extractor
            final PackWrapper wrapper = ResourcepackWrapper.of(plugin.getLibrary(), extractor);

            // Build the resourcepack
            wrapper.buildResourcePack();

            // Send the resourcepack to all players on the server
            sendResourcepack(plugin.getHttpConfiguration().getDaemon(), audience, wrapper);

            // Send success message
            audience.sendMessage(
                ChatUtilities.formatMessage(
                    Component.text(
                        String.format("Successfully loaded video %s", mrl), NamedTextColor.GOLD)));

            // Set completed
            atomicBoolean.set(true);
          });
    }
    return 1;
  }

  private void sendResourcepack(
      @Nullable final HttpServerDaemon provider,
      @NotNull final Audience audience,
      @NotNull final PackWrapper wrapper) {
    if (provider != null) {

      // Generates a url given by the HTTP server for a file
      resourcepackLink = provider.generateUrl(wrapper.getPath());
      hash = VideoExtractionUtilities.createHashSHA(Paths.get(wrapper.getPath()));

      // Send the resourcepack url to all players on the server
      sendResourcepackFile();

    } else {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text(
                  "You have HTTP set false by default. You cannot "
                      + "play Youtube videos without a daemon",
                  NamedTextColor.RED)));
    }
  }

  private void sendResourcepackFile() {
    for (final Player p : Bukkit.getOnlinePlayers()) {
      p.setResourcePack(resourcepackLink, hash);
    }
  }

  @Override
  public LiteralCommandNode<CommandSender> getCommandNode() {
    return literalNode;
  }
}
