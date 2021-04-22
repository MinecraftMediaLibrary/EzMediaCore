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

package com.github.pulsebeat02.minecraftmedialibrary.dependency;

import org.jetbrains.annotations.NotNull;

/**
 * This is an enum used to store the dependencies MinecraftMediaLibrary will use during the runtime.
 * Dependencies are specified using a group id, an artifact id, a version, and a specific resolution
 * to be chosen. Such a resolution would include examples such as Maven or Jitpack and other
 * solutions.
 */
public enum RepositoryDependency {

  /** VLCJ Maven Dependency */
  VLCJ("uk{}co{}caprica", "vlcj", "4{}7{}1", DependencyResolution.MAVEN_DEPENDENCY),

  /** VLCJ Natives Maven Dependency */
  VLCJ_NATIVES("uk{}co{}caprica", "vlcj-natives", "4{}1{}0", DependencyResolution.MAVEN_DEPENDENCY),

  /** Youtube Downloader Maven Dependency */
  YOUTUBE_DOWNLOADER(
      "com{}github{}sealedtx",
      "java-youtube-downloader",
      "2{}4{}6",
      DependencyResolution.JITPACK_DEPENDENCY),

  /** Jave Core Maven Dependency */
  JAVE_CORE("ws{}schild", "jave-core", "3{}0{}1", DependencyResolution.MAVEN_DEPENDENCY),

  /** Apache Commons Compression Maven Dependency */
  COMMONS_COMPRESSION(
      "org{}apache{}commons", "commons-compress", "1{}20", DependencyResolution.MAVEN_DEPENDENCY),

  /** Compression Maven Dependency */
  COMPRESSION(
      "com{}github{}PulseBeat02",
      "jarchivelib",
      "master-SNAPSHOT",
      DependencyResolution.JITPACK_DEPENDENCY),

  /** Compression XZ Maven Dependency */
  XZ("org{}tukaani", "xz", "1{}0", DependencyResolution.MAVEN_DEPENDENCY),

  /** ASM Maven Dependency */
  ASM("org{}ow2{}asm", "asm", "9{}1", DependencyResolution.MAVEN_DEPENDENCY),

  /** ASM Commons Maven Dependency */
  ASM_COMMONS("org{}ow2{}asm", "asm-commons", "9{}1", DependencyResolution.MAVEN_DEPENDENCY),

  /** JNA Maven Dependency */
  JNA("net{}java{}dev{}jna", "jna", "5{}7{}0", DependencyResolution.MAVEN_DEPENDENCY),

  /** Fast JSON Maven Dependency */
  FAST_JSON("com{}alibaba", "fastjson", "1{}2{}73", DependencyResolution.MAVEN_DEPENDENCY);

  private final String group;
  private final String artifact;
  private final String version;
  private final DependencyResolution resolution;

  /**
   * Instantiates a RepositoryDependency
   *
   * @param group dependency group
   * @param artifact dependency artifact
   * @param version dependency version
   * @param resolution dependency resolution
   */
  RepositoryDependency(
      @NotNull final String group,
      @NotNull final String artifact,
      @NotNull final String version,
      @NotNull final DependencyResolution resolution) {
    this.group = group.replaceAll("\\{}", ".");
    this.artifact = artifact.replaceAll("\\{}", ".");
    this.version = version.replaceAll("\\{}", ".");
    this.resolution = resolution;
  }

  /**
   * Gets group.
   *
   * @return the group
   */
  public String getGroup() {
    return group;
  }

  /**
   * Gets artifact.
   *
   * @return the artifact
   */
  public String getArtifact() {
    return artifact;
  }

  /**
   * Gets version.
   *
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Gets dependency resolution.
   *
   * @return the resolution
   */
  public DependencyResolution getResolution() {
    return resolution;
  }
}
