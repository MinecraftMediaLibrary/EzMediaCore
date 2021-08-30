package io.github.pulsebeat02.ezmediacore.libshout;

/**
 * desc...
 *
 * @author caorong
 */
public enum MimeType {
  mp3("audio/mpeg"), ogg("application/ogg"), audioWebm("audio/webm"), videoWebm("video/webm");

  private final String contentType;

  MimeType(final String s) {
    this.contentType = s;
  }

  public String getContentType() {
    return this.contentType;
  }
}
