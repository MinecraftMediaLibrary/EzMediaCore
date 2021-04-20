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

package com.github.pulsebeat02.minecraftmedialibrary.extractor;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.VideoDetails;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.VideoExtractionUtilities;
import org.jetbrains.annotations.NotNull;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.process.ProcessLocator;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Youtube extraction class used to extract audio from video files. Very useful for extraction media
 * from a specific link.
 */
public class YoutubeExtraction implements VideoExtractor {

  private final Encoder encoder;
  private final EncodingAttributes attrs;
  private final ProcessLocator ffmpegLocator;

  private final String url;
  private final String directory;
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
    ffmpegLocator = new FFmpegLocation();
    encoder = new Encoder(ffmpegLocator);
    final AudioAttributes attributes = new AudioAttributes();
    attributes.setCodec(settings.getCodec());
    attributes.setBitRate(settings.getBitrate());
    attributes.setChannels(settings.getChannels());
    attributes.setSamplingRate(settings.getSamplingRate());
    attributes.setVolume(settings.getVolume());
    attrs = new EncodingAttributes();
    attrs.setInputFormat(settings.getInputFormat());
    attrs.setOutputFormat(settings.getOutputFormat());
    attrs.setAudioAttributes(attributes);
  }

  /**
   * Downloads the video from the link provided.
   *
   * @return video file
   */
  @Override
  @NotNull
  public File downloadVideo() {
    onVideoDownload();
    final Optional<String> videoID = VideoExtractionUtilities.getVideoID(url);
    Logger.info(String.format("Downloading Video at URL (%s)", url));
    if (videoID.isPresent()) {
      try {
        final YoutubeVideo ytVideo = new YoutubeDownloader().getVideo(videoID.get());
        details = ytVideo.details();
        video =
            ytVideo.download(
                ytVideo.videoWithAudioFormats().get(0), new File(directory), "video", true);
        Logger.info(String.format("Successfully Downloaded Video at URL: (%s)", url));
      } catch (final IOException | YoutubeException e) {
        Logger.info(String.format("Could not Download Video at URL!: (%s)", url));
        e.printStackTrace();
      }
    }
    return video;
  }

  /**
   * Extracts the audio from the video file provided.
   *
   * @return audio file
   */
  @Override
  @NotNull
  public File extractAudio() {
    if (video == null) {
      downloadVideo();
    }
    onAudioExtraction();
    Logger.info(String.format("Extracting Audio from Video File (%s)", video.getAbsolutePath()));
    audio = new File(String.format("%s/audio.ogg", directory));
    try {
      encoder.encode(new MultimediaObject(video, ffmpegLocator), audio, attrs);
      Logger.info(
          String.format(
              "Successfully Extracted Audio from Video File! (Target: %s)",
              audio.getAbsolutePath()));
    } catch (final EncoderException e) {
      Logger.error(
          String.format(
              "Couldn't Extract Audio from Video File! (Video: %s)", video.getAbsolutePath()));
      e.printStackTrace();
    }
    return audio;
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

  /**
   * Gets the audio/video encoder.
   *
   * @return the encoder
   */
  public Encoder getEncoder() {
    return encoder;
  }

  /**
   * Gets the encoding attributes.
   *
   * @return the encoding attributes
   */
  public EncodingAttributes getAttrs() {
    return attrs;
  }

  /**
   * Gets the process locator.
   *
   * @return the ffmpeg locator
   */
  public ProcessLocator getFfmpegLocator() {
    return ffmpegLocator;
  }
}
