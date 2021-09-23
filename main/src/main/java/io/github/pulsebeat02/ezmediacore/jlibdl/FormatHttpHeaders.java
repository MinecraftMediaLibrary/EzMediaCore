package io.github.pulsebeat02.ezmediacore.jlibdl;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

public final class FormatHttpHeaders {

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

  public @Nullable String getAccept() {
    return this.accept;
  }

  public @Nullable String getAcceptCharset() {
    return this.acceptCharset;
  }

  public @Nullable String getAcceptEncoding() {
    return this.acceptEncoding;
  }

  public @Nullable String getAcceptLanguage() {
    return this.acceptLanguage;
  }

  public @Nullable String getUserAgent() {
    return this.userAgent;
  }
}
