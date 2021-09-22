package io.github.pulsebeat02.ezmediacore.youtubedl;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

public final class MediaHttpHeaders {

  @SerializedName("Accept")
  @Expose
  private String accept;

  @SerializedName("Accept-Charset")
  @Expose
  private String acceptCharset;

  @SerializedName("Accept-Encoding")
  @Expose
  private String acceptEncoding;

  @SerializedName("Accept-Language")
  @Expose
  private String acceptLanguage;

  @SerializedName("User-Agent")
  @Expose
  private String userAgent;

  public @NotNull String getAccept() {
    return this.accept;
  }

  public @NotNull String getAcceptCharset() {
    return this.acceptCharset;
  }

  public @NotNull String getAcceptEncoding() {
    return this.acceptEncoding;
  }

  public @NotNull String getAcceptLanguage() {
    return this.acceptLanguage;
  }

  public @NotNull String getUserAgent() {
    return this.userAgent;
  }
}
