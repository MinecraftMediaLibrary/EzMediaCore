/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.dependency;

import org.jetbrains.annotations.NotNull;

public enum DependencyInfo {
  VLCJ("uk{}co{}caprica", "vlcj", "4{}7{}1", Repositories.MAVEN),
  VLCJ_NATIVES("uk{}co{}caprica", "vlcj-natives", "4{}5{}0", Repositories.MAVEN),

  YOUTUBE_DOWNLOADER(
      "com{}github{}sealedtx", "java-youtube-downloader", "3{}0{}1", Repositories.JITPACK),
  FAST_JSON("com{}alibaba", "fastjson", "1{}2{}78", Repositories.MAVEN),

  JAFFREE("com{}github{}kokorin{}jaffree", "jaffree", "2021{}05{}31", Repositories.MAVEN),
  JCODEC("org{}jcodec", "jcodec", "0{}2{}5", Repositories.MAVEN),

  COMMONS_COMPRESSION("org{}apache{}commons", "commons-compress", "1{}20", Repositories.MAVEN),
  COMPRESSION("com{}github{}PulseBeat02", "jarchivelib", "master-SNAPSHOT", Repositories.JITPACK),
  XZ("org{}tukaani", "xz", "1{}9", Repositories.MAVEN),

  JNA("net{}java{}dev{}jna", "jna", "5{}9{}0", Repositories.MAVEN),
  JNA_PLATFORM("net{}java{}dev{}jna", "jna-platform", "5{}9{}0", Repositories.MAVEN),

  SPOTIFY("se{}michaelthelin{}spotify", "spotify-web-api-java", "6{}5{}4", Repositories.MAVEN),

  CAFFEINE("com{}github{}ben-manes{}caffeine", "caffeine", "3{}0{}3", Repositories.MAVEN),

  JDA("net{}dv8tion", "JDA", "4{}3{}0_277", Repositories.JDA),
  TROVE4J("net{}sf{}trove4j", "trove4j", "3{}0{}3", Repositories.MAVEN),
  JACKSON_DATABIND(
      "com{}fasterxml{}jackson{}core", "jackson-databind", "2{}13{}0-rc2", Repositories.MAVEN),

  LAVAPLAYER("com{}sedmelluq", "lavaplayer", "1{}3{}78", Repositories.JDA),
  JSOUP("org{}jsoup", "jsoup", "1{}14{}2", Repositories.MAVEN),
  BASE64("net{}iharder", "base64", "2{}3{}9", Repositories.MAVEN);

  private final String group;
  private final String artifact;
  private final String version;
  private final Repositories resolution;

  DependencyInfo(
      @NotNull final String group,
      @NotNull final String artifact,
      @NotNull final String version,
      @NotNull final Repositories resolution) {
    this.group = group.replaceAll("\\{}", ".");
    this.artifact = artifact.replaceAll("\\{}", ".");
    this.version = version.replaceAll("\\{}", ".");
    this.resolution = resolution;
  }

  public String getGroup() {
    return this.group;
  }

  public String getArtifact() {
    return this.artifact;
  }

  public String getVersion() {
    return this.version;
  }

  public Repositories getResolution() {
    return this.resolution;
  }
}
