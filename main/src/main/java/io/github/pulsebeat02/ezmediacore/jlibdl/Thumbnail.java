package io.github.pulsebeat02.ezmediacore.jlibdl;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

public final class Thumbnail {

  @SerializedName("id")
  @Expose
  private String id;

  @SerializedName("url")
  @Expose
  private String url;

  public @Nullable String getId() {
    return this.id;
  }

  public @Nullable String getUrl() {
    return this.url;
  }
}
