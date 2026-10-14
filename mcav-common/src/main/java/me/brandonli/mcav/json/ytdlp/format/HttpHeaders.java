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

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The HTTP headers yt-dlp used to fetch a stream, which a player has to send as well.
 *
 * <p>Gson fills instances from the JSON yt-dlp prints. Values yt-dlp did not report are empty, zero or false; a
 * primitive zero or false does not tell a missing value from a reported one.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @since 1.0.0
 */
@JsonAdapter(HttpHeaders.Deserializer.class)
public final class HttpHeaders {

  @SerializedName("User-Agent")
  private final @Nullable String userAgent;

  @SerializedName("Accept")
  private final @Nullable String accept;

  @SerializedName("Accept-Language")
  private final @Nullable String acceptLanguage;

  @SerializedName("Sec-Fetch-Mode")
  private final @Nullable String secFetchMode;

  private HttpHeaders(final Fields fields) {
    this.userAgent = fields.userAgent();
    this.accept = fields.accept();
    this.acceptLanguage = fields.acceptLanguage();
    this.secFetchMode = fields.secFetchMode();
  }

  /**
   * Returns the {@code User-Agent} header, which names the browser yt-dlp pretended to be, or empty if yt-dlp did
   * not report it.
   *
   * @return the {@code User-Agent} header, which names the browser yt-dlp pretended to be, or empty if yt-dlp did
   * not report it
   * @since 1.0.0
   */
  public Optional<String> getUserAgent() {
    return Optional.ofNullable(this.userAgent);
  }

  /**
   * Returns the {@code Accept} header, which lists the content types the request accepts, or empty if yt-dlp did
   * not report it.
   *
   * @return the {@code Accept} header, which lists the content types the request accepts, or empty if yt-dlp did
   * not report it
   * @since 1.0.0
   */
  public Optional<String> getAccept() {
    return Optional.ofNullable(this.accept);
  }

  /**
   * Returns the {@code Accept-Language} header, which lists the preferred languages, or empty if yt-dlp did not
   * report it.
   *
   * @return the {@code Accept-Language} header, which lists the preferred languages, or empty if yt-dlp did not
   * report it
   * @since 1.0.0
   */
  public Optional<String> getAcceptLanguage() {
    return Optional.ofNullable(this.acceptLanguage);
  }

  /**
   * Returns the {@code Sec-Fetch-Mode} header, which some sites check to tell browsers from other clients, or empty
   * if yt-dlp did not report it.
   *
   * @return the {@code Sec-Fetch-Mode} header, which some sites check to tell browsers from other clients, or empty
   * if yt-dlp did not report it
   * @since 1.0.0
   */
  public Optional<String> getSecFetchMode() {
    return Optional.ofNullable(this.secFetchMode);
  }

  /**
   * Returns whether {@code other} is a set of HTTP headers with the same values.
   *
   * @param other the object to compare with
   * @return whether every value matches
   * @since 1.0.0
   */
  @Override
  public boolean equals(final @Nullable Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof final HttpHeaders httpHeaders)) {
      return false;
    }

    return (
      Objects.equals(this.userAgent, httpHeaders.userAgent) &&
      Objects.equals(this.accept, httpHeaders.accept) &&
      Objects.equals(this.acceptLanguage, httpHeaders.acceptLanguage) &&
      Objects.equals(this.secFetchMode, httpHeaders.secFetchMode)
    );
  }

  /**
   * Returns a hash code built from all four headers.
   *
   * @return the hash code
   * @since 1.0.0
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.userAgent, this.accept, this.acceptLanguage, this.secFetchMode);
  }

  /**
   * Returns a debugging representation with all four headers.
   *
   * @return the representation
   * @since 1.0.0
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("userAgent", this.userAgent)
      .add("accept", this.accept)
      .add("acceptLanguage", this.acceptLanguage)
      .add("secFetchMode", this.secFetchMode)
      .toString();
  }

  static final class Deserializer implements JsonDeserializer<HttpHeaders> {

    @Override
    public HttpHeaders deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context) {
      final @Nullable Fields parsed = context.deserialize(json, Fields.class);
      final Fields fields = requireNonNull(parsed); // checker framework
      return new HttpHeaders(fields);
    }
  }

  private record Fields(
    @SerializedName("User-Agent") @Nullable String userAgent,
    @SerializedName("Accept") @Nullable String accept,
    @SerializedName("Accept-Language") @Nullable String acceptLanguage,
    @SerializedName("Sec-Fetch-Mode") @Nullable String secFetchMode
  ) {}
}
