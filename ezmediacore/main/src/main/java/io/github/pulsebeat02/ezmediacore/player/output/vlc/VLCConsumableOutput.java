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
package io.github.pulsebeat02.ezmediacore.player.output.vlc;

import io.github.pulsebeat02.ezmediacore.player.output.ConsumableOutput;


import java.util.Optional;
import java.util.function.Consumer;

public final class VLCConsumableOutput implements ConsumableOutput {

  private Consumer<Optional<int[]>> rgbSamples;
  private Consumer<Optional<byte[]>> audioSamples;

  public VLCConsumableOutput( final Consumer<Optional<int[]>> rgbSamples,  final Consumer<Optional<byte[]>> audioSamples) {
    this.rgbSamples = rgbSamples;
    this.audioSamples = audioSamples;
  }
  @Override
  public  VLCFrame getRaw() {
    return null; // not used
  }

  @Override
  public void consume( final VLCFrame frame) {
    this.rgbSamples.accept(frame.getRGBSamples());
    this.audioSamples.accept(frame.getAudioSamples());
  }

  @Override
  public void setVideoConsumer( final Consumer<Optional<int[]>> consumer) {
    this.rgbSamples = consumer;
  }

  @Override
  public void setAudioConsumer( final Consumer<Optional<byte[]>> consumer) {
    this.audioSamples = consumer;
  }

  @Override
  public  Consumer<Optional<int[]>> getVideoConsumer() {
    return this.rgbSamples;
  }

  @Override
  public  Consumer<Optional<byte[]>> getAudioConsumer() {
    return this.audioSamples;
  }
}
