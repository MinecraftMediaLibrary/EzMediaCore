/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.extractor;

import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.nio.file.Path;

/** An audio extraction helper that helps extract audio from video files. */
public class AudioExtractionHelper implements AudioExtractionContext {

  private static final Encoder ENCODER;
  private static final FFmpegLocation FFMPEG_LOCATION;

  static {
    FFMPEG_LOCATION = new FFmpegLocation();
    ENCODER = new Encoder(FFMPEG_LOCATION);
  }

  private final Path input;
  private final Path output;
  private final EncodingAttributes attrs;

  /**
   * Instantiates a new AudioExtractionHelper.
   *
   * @param settings the settings
   * @param input the input
   * @param output the output
   */
  public AudioExtractionHelper(
      @NotNull final ExtractionConfiguration settings,
      @NotNull final Path input,
      @NotNull final Path output) {
    this.input = input;
    this.output = output;
    final AudioAttributes attributes = new AudioAttributes();
    attributes.setCodec(settings.getCodec());
    attributes.setBitRate(settings.getBitrate());
    attributes.setChannels(settings.getChannels());
    attributes.setSamplingRate(settings.getSamplingRate());
    attributes.setVolume(settings.getVolume());
    attrs = new EncodingAttributes();
    attrs.setInputFormat(settings.getInputFormat());
    attrs.setOutputFormat(settings.getOutputFormat());
    attrs.setAudioAttributes(attributes);
  }

  @Override
  public void extract() {
    try {
      ENCODER.encode(new MultimediaObject(input.toFile(), FFMPEG_LOCATION), output.toFile(), attrs);
      Logger.info(
          String.format(
              "Successfully Extracted Audio from Video File! (Target: %s)",
              output.toAbsolutePath()));
    } catch (final EncoderException e) {
      Logger.error(String.format("Couldn't Extract Audio from Video File! (Video: %s)", output));
      e.printStackTrace();
    }
  }

  /**
   * Checks if the current instance and object are equal.
   *
   * @param obj the other object
   * @return whether the current instance and other object are equal
   */
  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof AudioExtractionHelper)) {
      return false;
    }
    final AudioExtractionHelper extraction = (AudioExtractionHelper) obj;
    return input.equals(extraction.getInput())
        && output.equals(extraction.getOutput())
        && attrs.equals(extraction.getEncodingAttributes());
  }

  @Override
  public Path getInput() {
    return input;
  }

  @Override
  public Path getOutput() {
    return output;
  }

  /**
   * Gets the encoding attributes.
   *
   * @return the encoding attributes
   */
  protected EncodingAttributes getEncodingAttributes() {
    return attrs;
  }
}
