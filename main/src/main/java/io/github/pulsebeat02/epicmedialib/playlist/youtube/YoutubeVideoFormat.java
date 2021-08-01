package io.github.pulsebeat02.epicmedialib.playlist.youtube;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import io.github.pulsebeat02.epicmedialib.utility.ImmutableDimension;
import org.jetbrains.annotations.NotNull;

import static com.github.kiulian.downloader.model.videos.quality.VideoQuality.hd1080;
import static com.github.kiulian.downloader.model.videos.quality.VideoQuality.hd1440;
import static com.github.kiulian.downloader.model.videos.quality.VideoQuality.hd2160;
import static com.github.kiulian.downloader.model.videos.quality.VideoQuality.hd2880p;
import static com.github.kiulian.downloader.model.videos.quality.VideoQuality.hd720;
import static com.github.kiulian.downloader.model.videos.quality.VideoQuality.highres;
import static com.github.kiulian.downloader.model.videos.quality.VideoQuality.large;
import static com.github.kiulian.downloader.model.videos.quality.VideoQuality.medium;
import static com.github.kiulian.downloader.model.videos.quality.VideoQuality.noVideo;
import static com.github.kiulian.downloader.model.videos.quality.VideoQuality.small;
import static com.github.kiulian.downloader.model.videos.quality.VideoQuality.tiny;
import static com.github.kiulian.downloader.model.videos.quality.VideoQuality.unknown;
import static io.github.pulsebeat02.epicmedialib.playlist.youtube.VideoQuality.HD1080;
import static io.github.pulsebeat02.epicmedialib.playlist.youtube.VideoQuality.HD1440;
import static io.github.pulsebeat02.epicmedialib.playlist.youtube.VideoQuality.HD2160;
import static io.github.pulsebeat02.epicmedialib.playlist.youtube.VideoQuality.HD2880P;
import static io.github.pulsebeat02.epicmedialib.playlist.youtube.VideoQuality.HD720;
import static io.github.pulsebeat02.epicmedialib.playlist.youtube.VideoQuality.HIGH_RES;
import static io.github.pulsebeat02.epicmedialib.playlist.youtube.VideoQuality.LARGE;
import static io.github.pulsebeat02.epicmedialib.playlist.youtube.VideoQuality.MEDIUM;
import static io.github.pulsebeat02.epicmedialib.playlist.youtube.VideoQuality.NO_VIDEO;
import static io.github.pulsebeat02.epicmedialib.playlist.youtube.VideoQuality.SMALL;
import static io.github.pulsebeat02.epicmedialib.playlist.youtube.VideoQuality.TINY;
import static io.github.pulsebeat02.epicmedialib.playlist.youtube.VideoQuality.UNKNOWN;

public final class YoutubeVideoFormat implements VideoFormat {

  private static final BiMap<
          com.github.kiulian.downloader.model.videos.quality.VideoQuality, VideoQuality>
      VIDEO_FORMATS;

  static {
    VIDEO_FORMATS =
        HashBiMap.create(
            ImmutableMap
                .<com.github.kiulian.downloader.model.videos.quality.VideoQuality, VideoQuality>
                    builder()
                .put(unknown, UNKNOWN)
                .put(noVideo, NO_VIDEO)
                .put(tiny, TINY)
                .put(small, SMALL)
                .put(medium, MEDIUM)
                .put(large, LARGE)
                .put(hd720, HD720)
                .put(hd1080, HD1080)
                .put(hd1440, HD1440)
                .put(hd2160, HD2160)
                .put(hd2880p, HD2880P)
                .put(highres, HIGH_RES)
                .build());
  }

  private final com.github.kiulian.downloader.model.videos.formats.VideoFormat format;
  private final ImmutableDimension dimension;

  YoutubeVideoFormat(
      @NotNull final com.github.kiulian.downloader.model.videos.formats.VideoFormat format) {
    this.format = format;
    this.dimension = new ImmutableDimension(format.width(), format.height());
  }

  protected static @NotNull BiMap<
          com.github.kiulian.downloader.model.videos.quality.VideoQuality, VideoQuality>
      getVideoFormatMappings() {
    return VIDEO_FORMATS;
  }

  @Override
  public int getFPS() {
    return this.format.fps();
  }

  @Override
  public @NotNull String getQualityLabel() {
    return this.format.qualityLabel();
  }

  @Override
  public @NotNull VideoQuality getQuality() {
    return VIDEO_FORMATS.get(this.format.videoQuality());
  }

  protected @NotNull com.github.kiulian.downloader.model.videos.formats.VideoFormat getFormat() {
    return this.format;
  }

  @Override
  public @NotNull ImmutableDimension getDimensions() {
    return this.dimension;
  }
}
