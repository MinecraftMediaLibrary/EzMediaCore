/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.pulsebeat02.ezmediacore.jlibdl.component;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public final class MediaInfo {

  @SerializedName("acodec")
  @Expose
  private String acodec;

  @SerializedName("description")
  @Expose
  private String description;

  @SerializedName("display_id")
  @Expose
  private String displayId;

  @SerializedName("ext")
  @Expose
  private String ext;

  @SerializedName("extractor")
  @Expose
  private String extractor;

  @SerializedName("extractor_key")
  @Expose
  private String extractorKey;

  @SerializedName("format")
  @Expose
  private String format;

  @SerializedName("format_id")
  @Expose
  private String formatId;

  @SerializedName("formats")
  @Expose
  private List<Format> formats = null;

  @SerializedName("fps")
  @Expose
  private double fps;

  @SerializedName("height")
  @Expose
  private int height;

  @SerializedName("http_headers")
  @Expose
  private MediaHttpHeaders httpHeaders;

  @SerializedName("id")
  @Expose
  private String id;

  @SerializedName("is_live")
  @Expose
  private boolean live;

  @SerializedName("manifest_url")
  @Expose
  private String manifestUrl;

  @SerializedName("playlist")
  @Expose
  private Object playlist;

  @SerializedName("playlist_index")
  @Expose
  private Object playlistIndex;

  @SerializedName("preference")
  @Expose
  private Object preference;

  @SerializedName("protocol")
  @Expose
  private String protocol;

  @SerializedName("requested_subtitles")
  @Expose
  private Object requestedSubtitles;

  @SerializedName("tbr")
  @Expose
  private double tbr;

  @SerializedName("thumbnail")
  @Expose
  private String thumbnail;

  @SerializedName("thumbnails")
  @Expose
  private List<Thumbnail> thumbnails = null;

  @SerializedName("timestamp")
  @Expose
  private int timestamp;

  @SerializedName("title")
  @Expose
  private String title;

  @SerializedName("upload_date")
  @Expose
  private String uploadDate;

  @SerializedName("uploader")
  @Expose
  private String uploader;

  @SerializedName("uploader_id")
  @Expose
  private String uploaderId;

  @SerializedName("url")
  @Expose
  private String url;

  @SerializedName("vcodec")
  @Expose
  private String vcodec;

  @SerializedName("view_count")
  @Expose
  private Object viewCount;

  @SerializedName("webpage_url")
  @Expose
  private String webpageUrl;

  @SerializedName("webpage_url_basename")
  @Expose
  private String webpageUrlBasename;

  @SerializedName("width")
  @Expose
  private int width;

  public @Nullable String getAcodec() {
    return this.acodec;
  }

  public @Nullable String getDescription() {
    return this.description;
  }

  public @Nullable String getDisplayId() {
    return this.displayId;
  }

  public @Nullable String getExt() {
    return this.ext;
  }

  public @Nullable String getExtractor() {
    return this.extractor;
  }

  public @Nullable String getExtractorKey() {
    return this.extractorKey;
  }

  public @Nullable String getFormat() {
    return this.format;
  }

  public @Nullable String getFormatId() {
    return this.formatId;
  }

  public @Nullable List<Format> getFormats() {
    return this.formats;
  }

  public double getFps() {
    return this.fps;
  }

  public int getHeight() {
    return this.height;
  }

  public @Nullable MediaHttpHeaders getHttpHeaders() {
    return this.httpHeaders;
  }

  public @Nullable String getId() {
    return this.id;
  }

  public boolean isLive() {
    return this.live;
  }

  public @Nullable String getManifestUrl() {
    return this.manifestUrl;
  }

  public @Nullable Object getPlaylist() {
    return this.playlist;
  }

  public @Nullable Object getPlaylistIndex() {
    return this.playlistIndex;
  }

  public @Nullable Object getPreference() {
    return this.preference;
  }

  public @Nullable String getProtocol() {
    return this.protocol;
  }

  public @Nullable Object getRequestedSubtitles() {
    return this.requestedSubtitles;
  }

  public double getTbr() {
    return this.tbr;
  }

  public @Nullable String getThumbnail() {
    return this.thumbnail;
  }

  public @Nullable List<Thumbnail> getThumbnails() {
    return this.thumbnails;
  }

  public int getTimestamp() {
    return this.timestamp;
  }

  public @Nullable String getTitle() {
    return this.title;
  }

  public @Nullable String getUploadDate() {
    return this.uploadDate;
  }

  public @Nullable String getUploader() {
    return this.uploader;
  }

  public @Nullable String getUploaderId() {
    return this.uploaderId;
  }

  public @Nullable String getUrl() {
    return this.url;
  }

  public @Nullable String getVcodec() {
    return this.vcodec;
  }

  public @Nullable Object getViewCount() {
    return this.viewCount;
  }

  public @Nullable String getWebpageUrl() {
    return this.webpageUrl;
  }

  public @Nullable String getWebpageUrlBasename() {
    return this.webpageUrlBasename;
  }

  public int getWidth() {
    return this.width;
  }
}
