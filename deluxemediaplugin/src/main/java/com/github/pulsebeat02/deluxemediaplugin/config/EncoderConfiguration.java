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

package com.github.pulsebeat02.deluxemediaplugin.config;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.github.pulsebeat02.minecraftmedialibrary.extractor.ExtractionSetting;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class EncoderConfiguration extends AbstractConfiguration {

  private ExtractionSetting settings;

  public EncoderConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, "encoder.yml");
  }

  @Override
  void deserialize() {

    // Deserialize the audio encoder settings
    final FileConfiguration configuration = getFileConfiguration();
    configuration.set("bitrate", settings.getBitrate());
    configuration.set("channels", settings.getChannels());
    configuration.set("sampling-rate", settings.getSamplingRate());
    configuration.set("volume", settings.getVolume());
    saveConfig();
  }

  @Override
  void serialize() {

    // Read the encoder settings
    final FileConfiguration configuration = getFileConfiguration();

    /*

    Get the bitrate at which the audio should have
    (Ex: 160000 for decent quality)

    */
    final int bitrate = configuration.getInt("bitrate");

    /*

    Get the number of channels the audio should have
    (Ex: 1 for mono-audio, 2 for natural hearing or audio from both sides of the speaker)

    */
    final int channels = configuration.getInt("channels");

    /*

    Get the sampling rate the audio should have
    (Ex: 44100 samples per second)

    */
    final int samplingRate = configuration.getInt("sampling-rate");

    /*

    Get the volume the audio should be
    (Ex: 48 for normal)

    */
    final int volume = configuration.getInt("volume");

    // Create a new audio extraction configuration to be used
    settings = new ExtractionSetting(bitrate, channels, samplingRate, volume);
  }

  public ExtractionSetting getSettings() {
    return settings;
  }
}
