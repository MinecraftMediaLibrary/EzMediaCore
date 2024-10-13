/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.deluxemediaplugin.config;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.ezmediacore.ffmpeg.AudioAttributes;
import io.github.pulsebeat02.ezmediacore.ffmpeg.AudioConfiguration;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public final class EncoderConfiguration extends ConfigurationProvider<AudioConfiguration> {

  private AudioConfiguration settings;

  public EncoderConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, "configuration/encoder.yml");
  }

  @Override
  public void serialize() throws IOException {
    final FileConfiguration configuration = this.getFileConfiguration();
    configuration.set("bitrate", this.settings.getBitrate());
    configuration.set("channels", this.settings.getChannels());
    configuration.set("sampling-rate", this.settings.getSamplingRate());
    configuration.set("volume", this.settings.getVolume());
    this.saveConfig();
  }

  @Override
  public @NotNull AudioConfiguration deserialize() {
    final FileConfiguration configuration = this.getFileConfiguration();
    this.settings =
        AudioAttributes.ofAudioAttributes(
            "libvorbis",
            0,
            configuration.getInt("bitrate"),
            configuration.getInt("channels"),
            configuration.getInt("sampling-rate"),
            configuration.getInt("volume"));
    return this.settings;
  }
}
