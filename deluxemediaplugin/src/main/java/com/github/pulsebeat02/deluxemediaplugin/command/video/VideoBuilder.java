package com.github.pulsebeat02.deluxemediaplugin.command.video;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.frame.chat.ChatCallback;
import com.github.pulsebeat02.minecraftmedialibrary.frame.chat.ChatIntegratedPlayer;
import com.github.pulsebeat02.minecraftmedialibrary.frame.entity.EntityCallback;
import com.github.pulsebeat02.minecraftmedialibrary.frame.entity.EntityIntegratedPlayer;
import com.github.pulsebeat02.minecraftmedialibrary.frame.entity.ScreenEntityType;
import com.github.pulsebeat02.minecraftmedialibrary.frame.highlight.BlockHighlightCallback;
import com.github.pulsebeat02.minecraftmedialibrary.frame.highlight.BlockHighlightPlayer;
import com.github.pulsebeat02.minecraftmedialibrary.frame.map.MapDataCallback;
import com.github.pulsebeat02.minecraftmedialibrary.frame.map.MapIntegratedPlayer;
import com.github.pulsebeat02.minecraftmedialibrary.frame.scoreboard.ScoreboardCallback;
import com.github.pulsebeat02.minecraftmedialibrary.frame.scoreboard.ScoreboardIntegratedPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VideoBuilder {

  private final MinecraftMediaLibrary library;
  private final MinecraftVideoAttributes attributes;

  public VideoBuilder(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final MinecraftVideoAttributes attributes) {
    this.library = library;
    this.attributes = attributes;
  }

  public MapIntegratedPlayer createMapPlayer() {
    return MapIntegratedPlayer.builder()
        .setUrl(attributes.getFile().getAbsolutePath())
        .setWidth(attributes.getScreenWidth())
        .setHeight(attributes.getScreenHeight())
        .setCallback(
            MapDataCallback.builder()
                .setViewers(null)
                .setMap(attributes.getStartingMap())
                .setItemframeWidth(attributes.getFrameWidth())
                .setItemframeHeight(attributes.getFrameHeight())
                .setVideoWidth(attributes.getScreenWidth())
                .setDelay(0)
                .setDitherHolder(attributes.getDither())
                .build(library))
        .build(library);
  }

  public EntityIntegratedPlayer createEntityCloudPlayer(@NotNull final Player sender) {
    return EntityIntegratedPlayer.builder()
        .setUrl(attributes.getFile().getAbsolutePath())
        .setWidth(attributes.getScreenWidth())
        .setHeight(attributes.getScreenHeight())
        .setCallback(
            EntityCallback.builder()
                .setViewers(null)
                .setEntityWidth(attributes.getScreenWidth())
                .setEntityHeight(attributes.getScreenHeight())
                .setDelay(40)
                .setLocation(sender.getLocation())
                .setType(ScreenEntityType.AREA_EFFECT_CLOUD)
                .build(library))
        .build(library);
  }

  public ChatIntegratedPlayer createChatBoxPlayer() {
    return ChatIntegratedPlayer.builder()
        .setUrl(attributes.getFile().getAbsolutePath())
        .setWidth(attributes.getScreenWidth())
        .setHeight(attributes.getScreenHeight())
        .setCallback(
            ChatCallback.builder()
                .setViewers(null)
                .setChatWidth(attributes.getScreenWidth())
                .setChatHeight(attributes.getScreenHeight())
                .setDelay(40)
                .build(library))
        .build(library);
  }

  public ScoreboardIntegratedPlayer createScoreboardPlayer() {
    return ScoreboardIntegratedPlayer.builder()
        .setUrl(attributes.getFile().getAbsolutePath())
        .setWidth(attributes.getScreenWidth())
        .setHeight(attributes.getScreenHeight())
        .setCallback(
            ScoreboardCallback.builder()
                .setViewers(null)
                .setScoreboardWidth(attributes.getScreenWidth())
                .setScoreboardHeight(attributes.getScreenHeight())
                .setDelay(40)
                .build(library))
        .build(library);
  }

  public BlockHighlightPlayer createBlockHighlightPlayer(@NotNull final Player sender) {
    return BlockHighlightPlayer.builder()
        .setUrl(attributes.getFile().getAbsolutePath())
        .setWidth(attributes.getScreenWidth())
        .setHeight(attributes.getScreenHeight())
        .setCallback(
            BlockHighlightCallback.builder()
                .setViewers(null)
                .setHighlightWidth(attributes.getScreenWidth())
                .setHighlightHeight(attributes.getScreenHeight())
                .setDelay(40)
                .setLocation(sender.getLocation())
                .build(library))
        .build(library);
  }
}
