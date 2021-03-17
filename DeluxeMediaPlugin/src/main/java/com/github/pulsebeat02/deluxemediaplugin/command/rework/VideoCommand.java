package com.github.pulsebeat02.deluxemediaplugin.command.rework;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.github.pulsebeat02.deluxemediaplugin.config.HttpConfiguration;
import com.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction;
import com.github.pulsebeat02.minecraftmedialibrary.resourcepack.ResourcepackWrapper;
import com.github.pulsebeat02.minecraftmedialibrary.resourcepack.hosting.HttpDaemonProvider;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ExtractorUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.AbstractDitherHolder;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.DitherSetting;
import com.github.pulsebeat02.minecraftmedialibrary.video.itemframe.ItemFrameCallback;
import com.github.pulsebeat02.minecraftmedialibrary.video.player.AbstractVideoPlayer;
import com.github.pulsebeat02.minecraftmedialibrary.video.player.VLCJIntegratedPlayer;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class VideoCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> literalNode;
  private final DeluxeMediaPlugin plugin;
  private final AbstractDitherHolder dither;
  private AbstractVideoPlayer player;
  private boolean youtube;
  private YoutubeExtraction extractor;
  private File file;
  private int frameWidth;
  private int frameHeight;
  private int startingMap;
  private CompletableFuture<Void> future;

  public VideoCommand(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final DeluxeMediaPlugin pl,
      @NotNull final TabExecutor executor) {
    super(library, "video", executor, "deluxemediaplugin.command.video", "");
    dither = DitherSetting.SIERRA_FILTER_LITE_DITHER.getHolder();
    plugin = pl;
    final LiteralArgumentBuilder<CommandSender> builder = literal(getName());
    builder
        .requires(super::testPermission)
        .then(literal("start")
                .executes(this::startVideo))
        .then(literal("stop")
                .executes(this::stopVideo))
        .then(literal("load")
                .then(argument("mrl", StringArgumentType.word())
                        .executes(this::loadVideo)))
        .then(literal("set"))
        .executes(this::displayInformation);
    literalNode = builder.build();
  }

  @Override
  public String usage() {
    return "/video, /video [Start | Stop], /video load [Youtube Link | File], "
        + "/video set screen-dimension [Width:Height], /video set dither [Dither Type] "
        + "/video set itemframe-dimension [Width:Height], /video set starting-map [Starting Map ID]";
  }

  private int displayInformation(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    if (player == null) {
      sender.sendMessage(
          ChatUtilities.formatMessage(ChatColor.RED + "There isn't a video currently playing!"));
    } else {
      sender.sendMessage(
          ChatUtilities.formatMessage(ChatColor.AQUA + "====================================="));
      if (youtube) {
        sender.sendMessage(ChatColor.GOLD + "Title: " + extractor.getVideoTitle());
        sender.sendMessage(ChatColor.GOLD + "Author: " + extractor.getAuthor());
        sender.sendMessage(ChatColor.GOLD + "Rating: " + extractor.getVideoRating());
        sender.sendMessage(
            ChatColor.GOLD + "Video Identifier: " + ChatColor.RED + extractor.getVideoId());
      } else {
        sender.sendMessage(ChatColor.GOLD + "Video Name: " + file.getName());
        sender.sendMessage(ChatColor.GOLD + "Size: " + file.getTotalSpace() / 1024 + " Kilobytes");
      }
      sender.sendMessage(
          ChatUtilities.formatMessage(ChatColor.AQUA + "====================================="));
    }
    return 1;
  }

  private int stopVideo(@NotNull final CommandContext<CommandSender> context) {
    context
        .getSource()
        .sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Stopped the Video!"));
    player.stop();
    return 1;
  }

  private int startVideo(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    if (file == null) {
      sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "File or URL not Specified!"));
      return 1;
    }
    if (youtube) {
      sender.sendMessage(
          ChatUtilities.formatMessage(
              ChatColor.GOLD + "Starting Video on URL: " + extractor.getUrl()));
    } else {
      sender.sendMessage(
          ChatUtilities.formatMessage(
              ChatColor.GOLD + "Starting Video on File: " + file.getName()));
    }
    final MinecraftMediaLibrary library = getLibrary();
    final ItemFrameCallback callback =
        new ItemFrameCallback(
            library, null, startingMap, frameWidth, frameHeight, player.getWidth(), 0, dither);
    if (library.isUsingVLCJ()) {
      if (file == null) {
        player =
            new VLCJIntegratedPlayer(
                library, extractor.getUrl(), player.getWidth(), player.getHeight(), callback::send);
      } else {
        player =
            new VLCJIntegratedPlayer(
                library, file, player.getWidth(), player.getHeight(), callback::send);
      }
    }
    player.start();
    return 1;
  }

  private int loadVideo(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    final String mrl = context.getArgument("mrl", String.class);
    final String folderPath = getLibrary().getPlugin().getDataFolder().getAbsolutePath();
    if (ExtractorUtilities.getVideoID(mrl) == null) {
      final File f = new File(folderPath, mrl);
      if (f.exists()) {
        youtube = false;
        extractor = null;
        file = f;
        sender.sendMessage(
            ChatUtilities.formatMessage(
                ChatColor.GOLD + "Successfully loaded video " + f.getName()));
      } else if (mrl.startsWith("http://") || mrl.startsWith("https://")) {
        sender.sendMessage(
            ChatUtilities.formatMessage(
                ChatColor.RED + "Link " + mrl + " is not a valid Youtube video link!"));
      } else {
        sender.sendMessage(
            ChatUtilities.formatMessage(
                ChatColor.RED + "File " + f.getName() + " cannot be found!"));
      }
    } else {
      youtube = true;
      if (future != null && !future.isDone()) {
        future.cancel(true);
        extractor = null;
      }
      extractor =
          new YoutubeExtraction(mrl, folderPath, plugin.getEncoderConfiguration().getSettings());
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
                            new ResourcepackWrapper.Builder()
                                    .setAudio(extractor.getAudio())
                                    .setDescription("Youtube Video: " + extractor.getVideoTitle())
                                    .setPath(configuration.getFileName())
                                    .setPackFormat(6)
                                    .createResourcepackHostingProvider(plugin.getLibrary());
                    wrapper.buildResourcePack();
                    if (finalProvider != null) {
                      final String url = finalProvider.generateUrl(wrapper.getPath());
                      for (final Player p : Bukkit.getOnlinePlayers()) {
                        p.setResourcePack(url);
                      }
                      sender.sendMessage(
                              ChatUtilities.formatMessage(ChatColor.GOLD + "Sending Resourcepack..."));
                    } else {
                      sender.sendMessage(
                              ChatUtilities.formatMessage(
                                      ChatColor.RED
                                              + "You have HTTP set false by default. You cannot "
                                              + "play Youtube videos without a daemon"));
                      future.cancel(true);
                    }
                    sender.sendMessage(
                            ChatUtilities.formatMessage(
                                    ChatColor.GOLD + "Successfully loaded video " + mrl)))
                  });
    }
    return 1;
  }

  @Override
  public LiteralCommandNode<CommandSender> getCommandNode() {
    return literalNode;
  }
}
