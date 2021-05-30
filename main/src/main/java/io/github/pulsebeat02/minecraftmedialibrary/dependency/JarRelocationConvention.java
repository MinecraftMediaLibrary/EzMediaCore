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

package io.github.pulsebeat02.minecraftmedialibrary.dependency;

import io.github.pulsebeat02.minecraftmedialibrary.relocation.Relocation;
import org.jetbrains.annotations.NotNull;

/**
 * A special class used to relocate the dependencies of MinecraftMediaLibrary. After installation of
 * the artifacts, MinecraftMediaLibrary will automatically relocate the jars to their respective
 * replacements. This relocation is to ensure that no dependency conflicts will exist with the
 * classpath.
 */
public enum JarRelocationConvention {

  /** VLCJ Jar Relocation Convention */
  VLCJ("uk{}co{}caprica{}vlcj", "io{}github{}pulsebeat02{}vlcj"),

  /** VLCJ Natives Jar Relocation Convention */
  VLCJ_NATIVES("uk{}co{}caprica{}vlcj{}binding", "io{}github{}pulsebeat02{}vlcj{}binding"),

  /** VLCJ NativeStreams Jar Relocation Convention */
  VLCJ_NATIVE_STREAMS(
      "uk{}co{}caprica{}nativestreams", "io{}github{}pulsebeat02{}vlcj{}nativestreams"),

  /** Youtube Downloader Jar Relocation Convention */
  YOUTUBE_DOWNLOADER("com{}github{}kiulian{}downloader", "io{}github{}pulsebeat02{}youtube"),

  /** Jave Core Jar Relocation Convention */
  JAVE_CORE("ws{}schild{}jave", "io{}github{}pulsebeat02{}jave"),

  /** Apache Commons Compression Jar Relocation Convention */
  COMMONS_COMPRESSION("org{}apache{}commons{}compress", "io{}github{}pulsebeat02{}compress"),

  /** Compression Jar Relocation Convention */
  COMPRESSION("org{}rauschig{}jarchivelib", "io{}github{}pulsebeat02{}jarchivelib"),

  /** Compress XZ Jar Relocation Convention */
  XZ("org{}tukaani.xz", "io{}github{}pulsebeat02{}xz"),

  /** ASM Jar Relocation Convention */
  ASM("org{}ow2{}asm", "io{}github{}pulsebeat02{}asm"),

  /** ASM Commons Jar Relocation Convention */
  ASM_COMMONS("org{}ow2{}asm{}commons", "io{}github{}pulsebeat02{}asm{}commons"),

  /** Fast JSON Jar Relocation Convention */
  FAST_JSON("com{}alibaba{}fastjson", "io{}github{}pulsebeat02{}fastjson"),

  /** Spotify Jar Relocation Convention */
  SPOTIFY("com{}wrapper{}spotify", "io{}github{}pulsebeat02{}spotify"),

  /** ByteDeco Jar Relocation Convention */
  JAFFREE("com{}github{}kokorin", "io{}github{}pulsebeat02{}kokorin");

  private final Relocation relocation;

  /**
   * Creates a JarRelocation convention based on arguments.
   *
   * @param before targets
   * @param after replacement
   */
  JarRelocationConvention(@NotNull final String before, @NotNull final String after) {
    relocation = new Relocation(before.replaceAll("\\{}", "."), after.replaceAll("\\{}", "."));
  }

  /**
   * Gets relocation.
   *
   * @return the relocation
   */
  public Relocation getRelocation() {
    return relocation;
  }
}
