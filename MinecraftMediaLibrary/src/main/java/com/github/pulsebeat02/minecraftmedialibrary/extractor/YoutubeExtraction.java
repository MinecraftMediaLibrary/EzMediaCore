/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/11/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.extractor;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.VideoDetails;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ExtractorUtilities;
import org.jetbrains.annotations.NotNull;
import ws.schild.jave.AudioAttributes;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.EncodingAttributes;
import ws.schild.jave.MultimediaObject;

import java.io.File;
import java.io.IOException;

public class YoutubeExtraction implements AbstractVideoExtractor {

  private final String url;
  private final String directory;
  private final ExtractionSetting settings;
  private VideoDetails details;
  private File video;
  private File audio;

  /**
   * Instantiates a new YoutubeExtraction.
   *
   * @param url the url
   * @param directory the directory
   * @param settings the settings
   */
  public YoutubeExtraction(
      @NotNull final String url,
      @NotNull final String directory,
      @NotNull final ExtractionSetting settings) {
    this.url = url;
    this.directory = directory;
    this.settings = settings;
  }

  /**
   * Downloads the video from the link provided.
   *
   * @return video file
   */
  @Override
  public File downloadVideo() {
    onVideoDownload();
    File videoFile = null;
    final YoutubeDownloader downloader = new YoutubeDownloader();
    final String ID = ExtractorUtilities.getVideoID(url);
    Logger.info("Downloading Video at URL (" + url + ")");
    if (ID != null) {
      try {
        final YoutubeVideo video = downloader.getVideo(ID);
        details = video.details();
        videoFile =
            video.download(
                video.videoWithAudioFormats().get(0), new File(directory), "video", true);
        Logger.info("Successfully Downloaded Video at URL: (" + url + ")");
      } catch (final IOException | YoutubeException e) {
        Logger.info("Could not Download Video at URL!: (" + url + ")");
        e.printStackTrace();
      }
    }
    return videoFile;
  }

  /**
   * Extracts the audio from the video file provided.
   *
   * @return audio file
   */
  @Override
  public File extractAudio() {
    if (video == null) {
      downloadVideo();
    }
    onAudioExtraction();
    Logger.info("Extracting Audio from Video File (" + video.getAbsolutePath() + ")");
    final File sound = new File(directory + "/audio.ogg");
    final AudioAttributes attributes = new AudioAttributes();
    attributes.setCodec(settings.getCodec());
    attributes.setBitRate(settings.getBitrate());
    attributes.setChannels(settings.getChannels());
    attributes.setSamplingRate(settings.getSamplingRate());
    attributes.setVolume(settings.getVolume());
    final EncodingAttributes attrs = new EncodingAttributes();
    attrs.setFormat(settings.getFormat());
    attrs.setAudioAttributes(attributes);
    final Encoder encoder = new Encoder();
    try {
      encoder.encode(new MultimediaObject(video), sound, attrs);
      Logger.info(
          "Successfully Extracted Audio from Video File! (Target: "
              + audio.getAbsolutePath()
              + ")");
    } catch (final EncoderException e) {
      Logger.error(
          "Couldn't Extract Audio from Video File! (Video: " + video.getAbsolutePath() + ")");
      e.printStackTrace();
    }
    return sound;
  }

  /** Called when the video has started to downloaded. */
  @Override
  public void onVideoDownload() {}

  /** Called when the audio is being extracted from the video. */
  @Override
  public void onAudioExtraction() {}

  /**
   * Gets directory.
   *
   * @return the directory
   */
  public String getDirectory() {
    return directory;
  }

  /**
   * Gets details.
   *
   * @return the details
   */
  public VideoDetails getDetails() {
    return details;
  }

  /**
   * Gets video.
   *
   * @return the video
   */
  public File getVideo() {
    return video;
  }

  /**
   * Gets audio.
   *
   * @return the audio
   */
  public File getAudio() {
    return audio;
  }

  /**
   * Gets url.
   *
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Gets author.
   *
   * @return the author
   */
  public String getAuthor() {
    return details.author();
  }

  /**
   * Gets video title.
   *
   * @return the video title
   */
  public String getVideoTitle() {
    return details.title();
  }

  /**
   * Gets video description.
   *
   * @return the video description
   */
  public String getVideoDescription() {
    return details.description();
  }

  /**
   * Gets video id.
   *
   * @return the video id
   */
  public String getVideoId() {
    return details.videoId();
  }

  /**
   * Gets video rating.
   *
   * @return the video rating
   */
  public int getVideoRating() {
    return details.averageRating();
  }

  /**
   * Gets viewer count.
   *
   * @return the viewer count
   */
  public long getViewerCount() {
    return details.viewCount();
  }

  /**
   * Is live boolean.
   *
   * @return the boolean
   */
  public boolean isLive() {
    return details.isLive();
  }

  /**
   * Is live content boolean.
   *
   * @return the boolean
   */
  public boolean isLiveContent() {
    return details.isLiveContent();
  }
}
