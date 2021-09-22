package io.github.pulsebeat02.ezmediacore.youtubedl;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

public final class Thumbnail {

  @SerializedName("id")
  @Expose
  private String id;

  @SerializedName("url")
  @Expose
  private String url;

  public @NotNull String getId() {
    return this.id;
  }

  public @NotNull String getUrl() {
    return this.url;
  }
}
