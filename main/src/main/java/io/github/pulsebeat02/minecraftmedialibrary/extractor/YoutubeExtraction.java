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

package io.github.pulsebeat02.minecraftmedialibrary.extractor;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.VideoDetails;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.github.pulsebeat02.minecraftmedialibrary.ffmpeg.FFmpegAudioExtractionHelper;
import io.github.pulsebeat02.minecraftmedialibrary.json.GsonHandler;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import io.github.pulsebeat02.minecraftmedialibrary.utility.VideoExtractionUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Youtube extraction class used to extract audio from video files. Very useful for extraction media
 * from a specific link.
 */
public class YoutubeExtraction implements VideoExtractor {

  private final ExtractionConfiguration configuration;
  private final String url;
  private final Path directory;
  private VideoDetails details;
  private Path video;
  private Path audio;

  /**
   * Instantiates a new YoutubeExtraction.
   *
   * @param url the url
   * @param directory the directory
   * @param settings the settings
   */
  public YoutubeExtraction(
      @NotNull final String url,
      @NotNull final Path directory,
      @NotNull final ExtractionConfiguration settings) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "Youtube URL cannot be empty null!");
    this.url = url;
    this.directory = directory;
    configuration = settings;
  }

  /**
   * Downloads the video from the link provided.
   *
   * @return video file
   */
  @Override
  @NotNull
  public Path downloadVideo() {
    onVideoDownload();
    final Optional<String> videoID = VideoExtractionUtilities.getYoutubeID(url);
    Logger.info(String.format("Downloading Video at URL (%s)", url));
    if (videoID.isPresent()) {
      try {
        final YoutubeVideo ytVideo = new YoutubeDownloader().getVideo(videoID.get());
        details = ytVideo.details();
        video =
            ytVideo
                .download(ytVideo.videoWithAudioFormats().get(0), directory.toFile(), "video", true)
                .toPath();
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
  public Path extractAudio() {
    if (video == null) {
      downloadVideo();
    }
    onAudioExtraction();
    Logger.info(String.format("Extracting Audio from Video File (%s)", video.toAbsolutePath()));
    audio = Paths.get(String.format("%s/audio.ogg", directory));
    new FFmpegAudioExtractionHelper(configuration, video, audio).extract();
    return audio;
  }

  /** Called when the video has started to downloaded. */
  @Override
  public void onVideoDownload() {}

  /** Called when the audio is being extracted from the video. */
  @Override
  public void onAudioExtraction() {}

  /**
   * Checks if two YoutubeExtraction classes are equal (in properties) with the exception of files.
   *
   * @param obj the other object
   * @return whether the two objects are equal in properties with the exception of files
   */
  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof YoutubeExtraction)) {
      return false;
    }
    final YoutubeExtraction extraction = (YoutubeExtraction) obj;
    return configuration.equals(extraction.getConfiguration())
        && url.equals(extraction.getUrl())
        && directory.equals(extraction.getDirectory())
        && details.equals(extraction.getDetails());
  }

  /**
   * Returns a String of the YoutubeExtraction.
   *
   * @return the stringified version of the instance
   */
  @Override
  public String toString() {
    return GsonHandler.getGson().toJson(this);
  }

  /**
   * Gets directory.
   *
   * @return the directory
   */
  public Path getDirectory() {
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
  public Path getVideo() {
    return video;
  }

  /**
   * Gets audio.
   *
   * @return the audio
   */
  public Path getAudio() {
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
   * Gets the extraction configuration
   *
   * @return the extraction configuration
   */
  public ExtractionConfiguration getConfiguration() {
    return configuration;
  }
}
