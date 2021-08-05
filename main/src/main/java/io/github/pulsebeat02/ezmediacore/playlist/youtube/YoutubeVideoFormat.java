package io.github.pulsebeat02.ezmediacore.playlist.youtube;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.pulsebeat02.ezmediacore.utility.ImmutableDimension;
import java.util.Map;
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
import static io.github.pulsebeat02.ezmediacore.playlist.youtube.VideoQuality.HD1080;
import static io.github.pulsebeat02.ezmediacore.playlist.youtube.VideoQuality.HD1440;
import static io.github.pulsebeat02.ezmediacore.playlist.youtube.VideoQuality.HD2160;
import static io.github.pulsebeat02.ezmediacore.playlist.youtube.VideoQuality.HD2880P;
import static io.github.pulsebeat02.ezmediacore.playlist.youtube.VideoQuality.HD720;
import static io.github.pulsebeat02.ezmediacore.playlist.youtube.VideoQuality.HIGH_RES;
import static io.github.pulsebeat02.ezmediacore.playlist.youtube.VideoQuality.LARGE;
import static io.github.pulsebeat02.ezmediacore.playlist.youtube.VideoQuality.MEDIUM;
import static io.github.pulsebeat02.ezmediacore.playlist.youtube.VideoQuality.NO_VIDEO;
import static io.github.pulsebeat02.ezmediacore.playlist.youtube.VideoQuality.SMALL;
import static io.github.pulsebeat02.ezmediacore.playlist.youtube.VideoQuality.TINY;
import static io.github.pulsebeat02.ezmediacore.playlist.youtube.VideoQuality.UNKNOWN;
import static java.util.Map.entry;

public final class YoutubeVideoFormat implements VideoFormat {

  private static final BiMap<
          com.github.kiulian.downloader.model.videos.quality.VideoQuality, VideoQuality>
      VIDEO_FORMATS;

  static {
    VIDEO_FORMATS =
        HashBiMap.create(
            Map.ofEntries(
                entry(unknown, UNKNOWN),
                entry(noVideo, NO_VIDEO),
                entry(tiny, TINY),
                entry(small, SMALL),
                entry(medium, MEDIUM),
                entry(large, LARGE),
                entry(hd720, HD720),
                entry(hd1080, HD1080),
                entry(hd1440, HD1440),
                entry(hd2160, HD2160),
                entry(hd2880p, HD2880P),
                entry(highres, HIGH_RES)));
  }

  private final com.github.kiulian.downloader.model.videos.formats.VideoFormat format;
  private final ImmutableDimension dimension;

  YoutubeVideoFormat(
      @NotNull final com.github.kiulian.downloader.model.videos.formats.VideoFormat format) {
    this.format = format;
    this.dimension = ImmutableDimension.of(format.width(), format.height());
  }

  static @NotNull BiMap<
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

  @NotNull
  com.github.kiulian.downloader.model.videos.formats.VideoFormat getFormat() {
    return this.format;
  }

  @Override
  public @NotNull ImmutableDimension getDimensions() {
    return this.dimension;
  }
}
