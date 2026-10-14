/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.brandonli.mcav.json.ytdlp.format;

import java.util.ArrayList;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The metadata yt-dlp prints for a video with {@code --dump-json}: the details of the video, every available stream
 * in {@link #formats}, and the fields of the stream yt-dlp would download by default, which is often a merge of the
 * best video and the best audio stream. The fields mirror the JSON and are filled by Gson; fields yt-dlp did not
 * report are null or zero.
 *
 * <p>This is a mutable Gson data model, not a validated media object. Nullable fields may be null when absent;
 * primitive fields default to zero or false, which does not distinguish missing values from reported ones.
 * Instances and their nested lists are not synchronized; copy or coordinate them before concurrent mutation.
 *
 * <p>This class is mutable and not thread-safe.
 *
 * @since 1.0.0
 */
public class UrlParseDump {

  /**
   * The identifier of the video on its site, such as the YouTube video ID.
   *
   * @since 1.0.0
   */
  public @Nullable String id;

  /**
   * The title of the video.
   *
   * @since 1.0.0
   */
  public @Nullable String title;

  /**
   * Every stream of the video, listed from worst to best. Pick one with a
   * {@link me.brandonli.mcav.json.ytdlp.strategy.FormatStrategy}.
   *
   * @since 1.0.0
   */
  public @Nullable ArrayList<Format> formats;

  /**
   * The thumbnails of the video in their available sizes.
   *
   * @since 1.0.0
   */
  public @Nullable ArrayList<Thumbnail> thumbnails;

  /**
   * The URL of the best thumbnail of the video.
   *
   * @since 1.0.0
   */
  public @Nullable String thumbnail;

  /**
   * The description of the video.
   *
   * @since 1.0.0
   */
  public @Nullable String description;

  /**
   * The identifier of the channel that published the video.
   *
   * @since 1.0.0
   */
  public @Nullable String channel_id;

  /**
   * The URL of the channel that published the video.
   *
   * @since 1.0.0
   */
  public @Nullable String channel_url;

  /**
   * The length of the video in seconds, which yt-dlp can report with a fraction, or zero for a running live stream.
   *
   * @since 1.0.0
   */
  public double duration;

  /**
   * The number of views.
   *
   * @since 1.0.0
   */
  public long view_count;

  /**
   * The average rating of the video, or null if the site does not rate videos.
   *
   * @since 1.0.0
   */
  public @Nullable Object average_rating;

  /**
   * The minimum age required to watch the video, or zero if there is none.
   *
   * @since 1.0.0
   */
  public int age_limit;

  /**
   * The canonical URL of the page of the video.
   *
   * @since 1.0.0
   */
  public @Nullable String webpage_url;

  /**
   * The categories the video is listed in.
   *
   * @since 1.0.0
   */
  public @Nullable ArrayList<String> categories;

  /**
   * The tags of the video.
   *
   * @since 1.0.0
   */
  public @Nullable ArrayList<String> tags;

  /**
   * Whether the video can be played in a player embedded in another page.
   *
   * @since 1.0.0
   */
  public boolean playable_in_embed;

  /**
   * Whether the video is live, such as {@code not_live}, {@code is_live}, or {@code was_live}.
   *
   * @since 1.0.0
   */
  public @Nullable String live_status;

  /**
   * The release time in seconds since the Unix epoch, or null if unknown.
   *
   * @since 1.0.0
   */
  public @Nullable Object release_timestamp;

  /**
   * The fields yt-dlp sorted the streams by, such as {@code quality} and {@code res}.
   *
   * @since 1.0.0
   */
  public @Nullable ArrayList<String> _format_sort_fields;

  /**
   * The number of comments.
   *
   * @since 1.0.0
   */
  public long comment_count;

  /**
   * The chapters of the video, or null if it has none.
   *
   * @since 1.0.0
   */
  public @Nullable Object chapters;

  /**
   * The replay heat map of the video, or null if the site does not report one.
   *
   * @since 1.0.0
   */
  public @Nullable ArrayList<Heatmap> heatmap;

  /**
   * The number of likes.
   *
   * @since 1.0.0
   */
  public long like_count;

  /**
   * The name of the channel that published the video.
   *
   * @since 1.0.0
   */
  public @Nullable String channel;

  /**
   * The number of followers of the channel.
   *
   * @since 1.0.0
   */
  public long channel_follower_count;

  /**
   * Whether the site verified the channel.
   *
   * @since 1.0.0
   */
  public boolean channel_is_verified;

  /**
   * The name of the uploader.
   *
   * @since 1.0.0
   */
  public @Nullable String uploader;

  /**
   * The identifier of the uploader, such as {@code @example}.
   *
   * @since 1.0.0
   */
  public @Nullable String uploader_id;

  /**
   * The URL of the page of the uploader.
   *
   * @since 1.0.0
   */
  public @Nullable String uploader_url;

  /**
   * The upload date in the form {@code YYYYMMDD}.
   *
   * @since 1.0.0
   */
  public @Nullable String upload_date;

  /**
   * The upload time in seconds since the Unix epoch, or zero if unknown.
   *
   * @since 1.0.0
   */
  public int timestamp;

  /**
   * Who can watch the video, such as {@code public}, {@code unlisted}, or {@code private}.
   *
   * @since 1.0.0
   */
  public @Nullable String availability;

  /**
   * The URL yt-dlp was given, before redirects and normalization.
   *
   * @since 1.0.0
   */
  public @Nullable String original_url;

  /**
   * The last path segment of {@link #webpage_url}, such as {@code watch}.
   *
   * @since 1.0.0
   */
  public @Nullable String webpage_url_basename;

  /**
   * The domain of {@link #webpage_url}, such as {@code youtube.com}.
   *
   * @since 1.0.0
   */
  public @Nullable String webpage_url_domain;

  /**
   * The name of the yt-dlp extractor that resolved the page, such as {@code youtube}.
   *
   * @since 1.0.0
   */
  public @Nullable String extractor;

  /**
   * The key of the yt-dlp extractor that resolved the page, such as {@code Youtube}.
   *
   * @since 1.0.0
   */
  public @Nullable String extractor_key;

  /**
   * The title of the playlist the video was resolved from, or null for a single video.
   *
   * @since 1.0.0
   */
  public @Nullable Object playlist;

  /**
   * The position of the video in its playlist, or null for a single video.
   *
   * @since 1.0.0
   */
  public @Nullable Object playlist_index;

  /**
   * The identifier of the video as the site displays it.
   *
   * @since 1.0.0
   */
  public @Nullable String display_id;

  /**
   * The complete title of the video, which yt-dlp never shortens.
   *
   * @since 1.0.0
   */
  public @Nullable String fulltitle;

  /**
   * The length of the video as text, such as {@code 3:32}.
   *
   * @since 1.0.0
   */
  public @Nullable String duration_string;

  /**
   * The year the video was released, or null if unknown.
   *
   * @since 1.0.0
   */
  public @Nullable Object release_year;

  /**
   * Whether the video is a live stream that is still running.
   *
   * @since 1.0.0
   */
  public boolean is_live;

  /**
   * Whether the video is the recording of a live stream that has ended.
   *
   * @since 1.0.0
   */
  public boolean was_live;

  /**
   * The subtitles yt-dlp would download, or null because the library requests none.
   *
   * @since 1.0.0
   */
  public @Nullable Object requested_subtitles;

  /**
   * Whether any stream of the video is protected by DRM, or null if unknown.
   *
   * @since 1.0.0
   */
  public @Nullable Object _has_drm;

  /**
   * The time yt-dlp resolved the page, in seconds since the Unix epoch.
   *
   * @since 1.0.0
   */
  public int epoch;

  /**
   * The audio sample rate of the default stream in hertz, or zero if unknown.
   *
   * @since 1.0.0
   */
  public int asr;

  /**
   * The exact size of the default stream in bytes, or null if unknown.
   *
   * @since 1.0.0
   */
  public @Nullable Object filesize;

  /**
   * The identifier of the default stream, such as {@code 137+251} for a merged video and audio stream.
   *
   * @since 1.0.0
   */
  public @Nullable String format_id;

  /**
   * A short readable note about the default stream, such as {@code 1080p+medium}.
   *
   * @since 1.0.0
   */
  public @Nullable String format_note;

  /**
   * How strongly yt-dlp prefers the source of the default stream, where higher is better.
   *
   * @since 1.0.0
   */
  public int source_preference;

  /**
   * The frame rate of the default stream in frames per second.
   *
   * @since 1.0.0
   */
  public double fps;

  /**
   * The number of audio channels of the default stream.
   *
   * @since 1.0.0
   */
  public int audio_channels;

  /**
   * The height of the default stream in pixels.
   *
   * @since 1.0.0
   */
  public int height;

  /**
   * The quality rank of the default stream, where higher is better.
   *
   * @since 1.0.0
   */
  public double quality;

  /**
   * Whether the default stream is protected by DRM.
   *
   * @since 1.0.0
   */
  public boolean has_drm;

  /**
   * The average total bitrate of the default stream in kilobits per second.
   *
   * @since 1.0.0
   */
  public double tbr;

  /**
   * The size of the default stream in bytes as estimated from its bitrate and duration, or zero if unknown.
   *
   * @since 1.0.0
   */
  public long filesize_approx;

  /**
   * The direct URL of the default stream, or null if it merges several streams.
   *
   * @since 1.0.0
   */
  public @Nullable String url;

  /**
   * The width of the default stream in pixels.
   *
   * @since 1.0.0
   */
  public int width;

  /**
   * The language code of the audio of the default stream, such as {@code en}, or null if unknown.
   *
   * @since 1.0.0
   */
  public @Nullable String language;

  /**
   * How strongly yt-dlp prefers the language of the default stream, where higher is better.
   *
   * @since 1.0.0
   */
  public int language_preference;

  /**
   * How strongly yt-dlp prefers the default stream, or null for no preference.
   *
   * @since 1.0.0
   */
  public @Nullable Object preference;

  /**
   * The file extension of the default stream, such as {@code mp4}.
   *
   * @since 1.0.0
   */
  public @Nullable String ext;

  /**
   * The video codec of the default stream, such as {@code avc1.640028}, or {@code none}.
   *
   * @since 1.0.0
   */
  public @Nullable String vcodec;

  /**
   * The audio codec of the default stream, such as {@code opus}, or {@code none}.
   *
   * @since 1.0.0
   */
  public @Nullable String acodec;

  /**
   * The dynamic range of the default stream, such as {@code SDR}, or null if it has no video.
   *
   * @since 1.0.0
   */
  public @Nullable String dynamic_range;

  /**
   * Hints for downloading the default stream, or null if there are none.
   *
   * @since 1.0.0
   */
  public @Nullable DownloaderOptions downloader_options;

  /**
   * The protocol of the default stream, such as {@code https+https} for a merge of two streams.
   *
   * @since 1.0.0
   */
  public @Nullable String protocol;

  /**
   * The resolution of the default stream as text, such as {@code 1920x1080}.
   *
   * @since 1.0.0
   */
  public @Nullable String resolution;

  /**
   * The width of the default stream divided by its height.
   *
   * @since 1.0.0
   */
  public double aspect_ratio;

  /**
   * The HTTP headers that requests for the default stream must send, or null if no headers were reported.
   *
   * @since 1.0.0
   */
  public @Nullable HttpHeaders http_headers;

  /**
   * The video extension of the default stream, such as {@code mp4}, or {@code none}.
   *
   * @since 1.0.0
   */
  public @Nullable String video_ext;

  /**
   * The audio extension of the default stream, such as {@code m4a}, or {@code none}.
   *
   * @since 1.0.0
   */
  public @Nullable String audio_ext;

  /**
   * The average video bitrate of the default stream in kilobits per second, or null if unknown.
   *
   * @since 1.0.0
   */
  public @Nullable Object vbr;

  /**
   * The average audio bitrate of the default stream in kilobits per second, or null if unknown.
   *
   * @since 1.0.0
   */
  public @Nullable Object abr;

  /**
   * A readable description of the default stream, such as {@code 137 - 1920x1080 (1080p)+251 - audio only}.
   *
   * @since 1.0.0
   */
  public @Nullable String format;

  /**
   * The file name yt-dlp would save the default stream as.
   *
   * @since 1.0.0
   */
  public @Nullable String _filename;

  /**
   * The file name yt-dlp would save the default stream as, the same value as {@link #_filename}.
   *
   * @since 1.0.0
   */
  public @Nullable String filename;

  /**
   * The kind of result, such as {@code video} or {@code playlist}.
   *
   * @since 1.0.0
   */
  public @Nullable String _type;

  /**
   * The version of yt-dlp that produced the metadata.
   *
   * @since 1.0.0
   */
  public @Nullable Version _version;

  /**
   * Creates an empty dump in which every field is null or zero. Gson creates dumps this way before it fills them.
   *
   * @since 1.0.0
   */
  public UrlParseDump() {}
}
