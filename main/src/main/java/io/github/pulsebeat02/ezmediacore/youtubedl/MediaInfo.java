package io.github.pulsebeat02.ezmediacore.youtubedl;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import org.jetbrains.annotations.NotNull;

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

  public @NotNull String getAcodec() {
    return this.acodec;
  }

  public @NotNull String getDescription() {
    return this.description;
  }

  public @NotNull String getDisplayId() {
    return this.displayId;
  }

  public @NotNull String getExt() {
    return this.ext;
  }

  public @NotNull String getExtractor() {
    return this.extractor;
  }

  public @NotNull String getExtractorKey() {
    return this.extractorKey;
  }

  public @NotNull String getFormat() {
    return this.format;
  }

  public @NotNull String getFormatId() {
    return this.formatId;
  }

  public @NotNull List<Format> getFormats() {
    return this.formats;
  }

  public double getFps() {
    return this.fps;
  }

  public int getHeight() {
    return this.height;
  }

  public @NotNull MediaHttpHeaders getHttpHeaders() {
    return this.httpHeaders;
  }

  public @NotNull String getId() {
    return this.id;
  }

  public boolean isLive() {
    return this.live;
  }

  public @NotNull String getManifestUrl() {
    return this.manifestUrl;
  }

  public @NotNull Object getPlaylist() {
    return this.playlist;
  }

  public @NotNull Object getPlaylistIndex() {
    return this.playlistIndex;
  }

  public @NotNull Object getPreference() {
    return this.preference;
  }

  public @NotNull String getProtocol() {
    return this.protocol;
  }

  public @NotNull Object getRequestedSubtitles() {
    return this.requestedSubtitles;
  }

  public double getTbr() {
    return this.tbr;
  }

  public @NotNull String getThumbnail() {
    return this.thumbnail;
  }

  public @NotNull List<Thumbnail> getThumbnails() {
    return this.thumbnails;
  }

  public int getTimestamp() {
    return this.timestamp;
  }

  public @NotNull String getTitle() {
    return this.title;
  }

  public @NotNull String getUploadDate() {
    return this.uploadDate;
  }

  public @NotNull String getUploader() {
    return this.uploader;
  }

  public @NotNull String getUploaderId() {
    return this.uploaderId;
  }

  public @NotNull String getUrl() {
    return this.url;
  }

  public @NotNull String getVcodec() {
    return this.vcodec;
  }

  public @NotNull Object getViewCount() {
    return this.viewCount;
  }

  public @NotNull String getWebpageUrl() {
    return this.webpageUrl;
  }

  public @NotNull String getWebpageUrlBasename() {
    return this.webpageUrlBasename;
  }

  public int getWidth() {
    return this.width;
  }
}
