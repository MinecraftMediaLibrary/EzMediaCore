package com.github.pulsebeat02.deluxemediaplugin.command.video;

import com.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction;
import com.github.pulsebeat02.minecraftmedialibrary.frame.VideoPlayer;
import com.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitherHolder;
import com.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitherSetting;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class MinecraftVideoAttributes {

  private final AtomicBoolean completion;
  private DitherHolder dither;
  private YoutubeExtraction extractor;
  private VideoPlayer player;
  private File file;
  private boolean youtube;
  private boolean maps;

  private int frameWidth;
  private int frameHeight;
  private int screenWidth;
  private int screenHeight;
  private int startingMap;

  public MinecraftVideoAttributes() {
    dither = DitherSetting.SIERRA_FILTER_LITE_DITHER.getHolder();
    frameWidth = 5;
    frameHeight = 5;
    screenWidth = 640;
    screenHeight = 360;
    completion = new AtomicBoolean(false);
    maps = false;
  }

  public DitherHolder getDither() {
    return dither;
  }

  public void setDither(final DitherHolder dither) {
    this.dither = dither;
  }

  public YoutubeExtraction getExtractor() {
    return extractor;
  }

  public void setExtractor(final YoutubeExtraction extractor) {
    this.extractor = extractor;
  }

  public VideoPlayer getPlayer() {
    return player;
  }

  public void setPlayer(final VideoPlayer player) {
    this.player = player;
  }

  public File getFile() {
    return file;
  }

  public void setFile(final File file) {
    this.file = file;
  }

  public boolean isYoutube() {
    return youtube;
  }

  public void setYoutube(final boolean youtube) {
    this.youtube = youtube;
  }

  public int getFrameWidth() {
    return frameWidth;
  }

  public void setFrameWidth(final int frameWidth) {
    this.frameWidth = frameWidth;
  }

  public int getFrameHeight() {
    return frameHeight;
  }

  public void setFrameHeight(final int frameHeight) {
    this.frameHeight = frameHeight;
  }

  public int getScreenWidth() {
    return screenWidth;
  }

  public void setScreenWidth(final int screenWidth) {
    this.screenWidth = screenWidth;
  }

  public int getScreenHeight() {
    return screenHeight;
  }

  public void setScreenHeight(final int screenHeight) {
    this.screenHeight = screenHeight;
  }

  public int getStartingMap() {
    return startingMap;
  }

  public void setStartingMap(final int startingMap) {
    this.startingMap = startingMap;
  }

  public AtomicBoolean getCompletion() {
    return completion;
  }

  public boolean isUsingMaps() {
    return maps;
  }

  public void setMaps(final boolean maps) {
    this.maps = maps;
  }
}
