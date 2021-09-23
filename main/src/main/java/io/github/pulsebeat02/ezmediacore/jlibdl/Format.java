package io.github.pulsebeat02.ezmediacore.jlibdl;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

public final class Format {

  @SerializedName("acodec")
  @Expose
  private String acodec;

  @SerializedName("ext")
  @Expose
  private String ext;

  @SerializedName("format")
  @Expose
  private String format;

  @SerializedName("format_id")
  @Expose
  private String formatId;

  @SerializedName("fps")
  @Expose
  private double fps;

  @SerializedName("http_headers")
  @Expose
  private FormatHttpHeaders httpHeaders;

  @SerializedName("manifest_url")
  @Expose
  private String manifestUrl;

  @SerializedName("preference")
  @Expose
  private Object preference;

  @SerializedName("protocol")
  @Expose
  private String protocol;

  @SerializedName("tbr")
  @Expose
  private double tbr;

  @SerializedName("url")
  @Expose
  private String url;

  @SerializedName("vcodec")
  @Expose
  private String vcodec;

  @SerializedName("height")
  @Expose
  private int height;

  @SerializedName("width")
  @Expose
  private int width;

  public @Nullable String getAcodec() {
    return this.acodec;
  }

  public @Nullable String getExt() {
    return this.ext;
  }

  public @Nullable String getFormat() {
    return this.format;
  }

  public @Nullable String getFormatId() {
    return this.formatId;
  }

  public double getFps() {
    return this.fps;
  }

  public @Nullable FormatHttpHeaders getHttpHeaders() {
    return this.httpHeaders;
  }

  public @Nullable String getManifestUrl() {
    return this.manifestUrl;
  }

  public @Nullable Object getPreference() {
    return this.preference;
  }

  public @Nullable String getProtocol() {
    return this.protocol;
  }

  public double getTbr() {
    return this.tbr;
  }

  public @Nullable String getUrl() {
    return this.url;
  }

  public @Nullable String getVcodec() {
    return this.vcodec;
  }

  public int getHeight() {
    return this.height;
  }

  public int getWidth() {
    return this.width;
  }
}
