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

package com.github.pulsebeat02.minecraftmedialibrary.extractor;

public interface ConfigurableExtractionSetting {

  /**
   * Checks if two ExtractionSetting objects are directly equal (properties).
   *
   * @param obj the other object
   * @return whether the two are equal in properties
   */
  @Override
  boolean equals(final Object obj);

  @Override
  String toString();

  /**
   * Gets codec.
   *
   * @return the codec
   */
  String getCodec();

  /**
   * Gets output format.
   *
   * @return the output format
   */
  String getOutputFormat();

  /**
   * Gets input format.
   *
   * @return the input format
   */
  String getInputFormat();

  /**
   * Gets bitrate.
   *
   * @return the bitrate
   */
  int getBitrate();

  /**
   * Sets bitrate.
   *
   * @param bitrate the bitrate
   */
  void setBitrate(final int bitrate);

  /**
   * Gets channels.
   *
   * @return the channels
   */
  int getChannels();

  /**
   * Sets channels.
   *
   * @param channels the channels
   */
  void setChannels(final int channels);

  /**
   * Gets sampling rate.
   *
   * @return the sampling rate
   */
  int getSamplingRate();

  /**
   * Sets sampling rate.
   *
   * @param samplingRate the sampling rate
   */
  void setSamplingRate(final int samplingRate);

  /**
   * Gets volume.
   *
   * @return the volume
   */
  int getVolume();

  /**
   * Sets volume.
   *
   * @param volume the volume
   */
  void setVolume(final int volume);
}
