/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/2/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.dependency;

import org.jetbrains.annotations.NotNull;

public enum RepositoryDependency {

  /** VLCJ Maven Dependency */
  VLCJ("uk{}co{}caprica", "vlcj", "4{}6{}0", DependencyResolution.MAVEN_DEPENDENCY),

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
  JNA("net{}java{}dev{}jna", "jna", "5{}7{}0", DependencyResolution.MAVEN_DEPENDENCY);

  private final String group;
  private final String artifact;
  private final String version;
  private final DependencyResolution resolution;

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
